package com.example.eventapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ShowAllEvents extends AppCompatActivity {
    ListView eventsList;
    FirebaseFirestore database;
    ArrayList<Event> events;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_all_events);

        eventsList = findViewById(R.id.eventsList);

        events = new ArrayList<Event>();

        database = FirebaseFirestore.getInstance();

        //@TODO dodać warunek, żeby pokazywało tylko aktualne wydarzenia
        database.collection("events")
                .orderBy("dataTime", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        events.clear();
                        if (!value.isEmpty()) {
                            for (DocumentSnapshot snapshot : value) {
                                Event event = new Event();
                                event.Coordinates = snapshot.getString("coordinates");
                                event.Name = snapshot.getString("name");
                                event.EventDate = snapshot.getString("dataTime").split(" ")[0];
                                event.EventTime = snapshot.getString("dataTime").split(" ")[1];
                                event.Id = snapshot.getId();
                                event.LocationWithoutPostalCode(getApplicationContext(), event.Coordinates);
                                events.add(event);
                            }
                        }

                        EventsAdapter eventsAdapter = new EventsAdapter(events, getApplicationContext());
                        eventsAdapter.notifyDataSetChanged();
                        eventsList.setAdapter(eventsAdapter);

                        // Edit plan
                        eventsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                Event ev = events.get(position);
                                Log.d("TAG", "Clicked on " + ev.getId());
                                /*Intent i = new Intent(getApplicationContext(), EventDetails.class);
                                i.putExtra("eventId", ev.getId());
                                startActivity(i);*/
                            }
                        });
                    }
                });
    }
}