package com.example.eventapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.widget.Toast.LENGTH_SHORT;
import static com.google.common.collect.Iterables.size;

public class FavouritesEvents extends AppCompatActivity {
    ProgressBar progressBar;
    TextView noFav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites_events);
        noFav = findViewById(R.id.noFav);
        progressBar = findViewById(R.id.progressBarFavEvents);
        progressBar.setVisibility(View.VISIBLE);

        ListView eventsList = findViewById(R.id.favouritesList);
        ArrayList<Event> events = new ArrayList<Event>();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference favorites = database.collection("favorites").document(userId);
        favorites.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentFav = task.getResult();
                    if (documentFav.exists()) {
                        List<String> favList = new ArrayList<>();
                        Map<String, Object> map = task.getResult().getData();
                        for (Map.Entry<String, Object> entry : map.entrySet()) {
                            favList.add(entry.getKey());
                        }

                        if (size(favList) > 0) {
                            //#region get event data
                            FirebaseFirestore.getInstance().collection("events")
                                    .whereIn(FieldPath.documentId(), favList)
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                            events.clear();
                                            if (!value.isEmpty()) {
                                                for (DocumentSnapshot snapshot : value) {
                                                    Event event = new Event();
                                                    event.Coordinates = snapshot.getString("coordinates");
                                                    event.Name = snapshot.getString("name");
                                                    event.EventDate = snapshot.getString("dataTime").split(" ")[0];;
                                                    event.EventTime = snapshot.getString("dataTime").split(" ")[1];;
                                                    event.EventId = snapshot.getId();
                                                    event.LocationWithoutPostalCode(getApplicationContext(), event.Coordinates);
                                                    events.add(event);
                                                }
                                            }

                                            EventsAdapter eventsAdapter = new EventsAdapter(events, getApplicationContext());
                                            eventsAdapter.notifyDataSetChanged();
                                            eventsList.setAdapter(eventsAdapter);
                                            progressBar.setVisibility(View.GONE);
                                            if (size(events) == 0) {
                                                noFav();
                                            }

                                            // Show event
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
                            //#endregion
                        } else {
                            noFav();
                        }
                    } else {
                        noFav();
                    }
                }
            }
        });
    }

    private void noFav()
    {
        progressBar.setVisibility(View.GONE);
        noFav.setVisibility(View.VISIBLE);
    }
}