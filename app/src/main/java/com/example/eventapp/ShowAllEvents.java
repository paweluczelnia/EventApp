package com.example.eventapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ShowAllEvents extends AppCompatActivity{
    ListView eventsList;
    FirebaseFirestore database;
    ArrayList<Event> events;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_all_events);
        progressBar = findViewById(R.id.progressBarEvList);
        progressBar.setVisibility(View.VISIBLE);
        eventsList = findViewById(R.id.eventsList);

        events = new ArrayList<Event>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        database = FirebaseFirestore.getInstance();

        database.collection("events")
                .orderBy("dataTime", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        events.clear();
                        if (!value.isEmpty()) {
                            for (DocumentSnapshot snapshot : value) {
                                String date = snapshot.getString("dataTime").split(" ")[0];
                                String time = snapshot.getString("dataTime").split(" ")[1];
                                try {
                                    Date d = dateFormat.parse(date + " " + time);
                                    Date currentDate = new Date();
                                    if (currentDate.before(d)) {
                                        Event event = new Event();
                                        event.Coordinates = snapshot.getString("coordinates");
                                        event.Name = snapshot.getString("name");
                                        event.EventDate = date;
                                        event.EventTime = time;
                                        event.EventId = snapshot.getId();
                                        event.LocationWithoutPostalCode(getApplicationContext(), event.Coordinates);
                                        events.add(event);
                                    }

                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                            }
                        }

                        EventsAdapter eventsAdapter = new EventsAdapter(events, getApplicationContext());
                        eventsAdapter.notifyDataSetChanged();
                        eventsList.setAdapter(eventsAdapter);
                        progressBar.setVisibility(View.GONE);
                        // Edit plan
                        eventsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                Event ev = events.get(position);
                                Intent i = new Intent(getApplicationContext(), ShowEvent.class);
                                i.putExtra("eventId", ev.getEventId());
                                startActivity(i);
                            }
                        });
                    }
                });
    }
}