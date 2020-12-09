package com.example.eventapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class ShowEvent extends AppCompatActivity {
    TextView evTitle, evLocation, evDate, evTime, evTicket,ticket;
    Button showAllEv, showPlan, goToEditEvent;
    DatabaseReference databaseReference, favreference;
    FirebaseFirestore database;
    Boolean favChecker = false;
    Event ev;
    Boolean isEventOwner = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_event);
        evTicket = findViewById(R.id.shEvTicket);
        evTitle = findViewById(R.id.shEvTitle);
        evLocation = findViewById(R.id.shEvLocation);
        evDate = findViewById(R.id.shEvData);
        evTime = findViewById(R.id.shEvStartTime);
        showAllEv = findViewById(R.id.shEvBackToAll);
        showPlan = findViewById(R.id.shEvShowPlanBtn);
        goToEditEvent = findViewById(R.id.goToEditEvent);

        //#region get event data
        database = FirebaseFirestore.getInstance();
        String eventId = getIntent().getStringExtra("eventId");
        if (eventId != null) {
            DocumentReference docRef = database.collection("events").document(eventId);

            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            long ticket = document.getLong("ticket");
                            ev = new Event();
                            ev.Ticket = (int)ticket;
                            ev.Coordinates = document.getString("coordinates");
                            ev.Name = document.getString("name");
                            ev.AuthorId = document.getString("authorId");
                            ev.EventDate = document.getString("dataTime").split(" ")[0];
                            ev.EventTime = document.getString("dataTime").split(" ")[1];
                            ev.EventId = eventId;
                            ev.CalculateLocation(getApplicationContext(), ev.Coordinates);

                            evTitle.setText(ev.getName());
                            evLocation.setText(ev.getLocationName());

                            if (ev.Ticket == 1) {
                                evTicket.setText("Wydarzenie biletowane");
                            } else {
                                evTicket.setText("Wydarzenie nie jest biletowane");
                            }

                            evDate.setText(ev.getEventDate());
                            evTime.setText(ev.getEventTime());

                            // validate if auth user is event owner
                            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            if (userID.equals(ev.AuthorId)) {
                                goToEditEvent.setVisibility(View.VISIBLE);
                                isEventOwner = true;
                            }
                        } else {
                            Toast.makeText(ShowEvent.this, "Nie udało się pobrać wydarzenia",
                                    Toast.LENGTH_SHORT).show();
                            redirect();
                        }
                    } else {
                        Toast.makeText(ShowEvent.this, "Nie udało się pobrać wydarzenia",
                                Toast.LENGTH_SHORT).show();
                        redirect();
                    }
                }
            });
        }
        //#endregion

        //#region buttons listener
        showAllEv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), ShowAllEvents.class));
            }
        });
        showPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), ShowEventPlans.class);
                i.putExtra("eventId", ev.EventId);
                i.putExtra("eventName", ev.Name);
                i.putExtra("editMode", isEventOwner);
                startActivity(i);
            }
        });

        goToEditEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), EditEvent.class);
                i.putExtra("eventId", ev.getEventId());
                startActivity(i);
            }
        });
        //#endregion
    }

    private void redirect() {
        startActivity(new Intent(getApplicationContext(), ShowAllEvents.class));
        finish();
    }
}