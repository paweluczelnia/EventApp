package com.example.eventapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ShowEventPlans extends AppCompatActivity {
    FirebaseFirestore database;
    ListView eventPlans;
    ArrayList<EventPlan> agenda = new ArrayList<>();
    Button backToEditBtn, addPlan, backToEvent;
    String eventId, eventName;
    Boolean editMode = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_event_plans);

        backToEditBtn = findViewById(R.id.backToEdit);
        addPlan = findViewById(R.id.addEventPlan);
        backToEvent = findViewById(R.id.backToEvent);

        eventId = getIntent().getStringExtra("eventId");
        eventName = getIntent().getStringExtra("eventName");
        editMode = getIntent().getBooleanExtra("editMode", true);

        if (!editMode) {
            backToEditBtn.setVisibility(View.GONE);
            addPlan.setVisibility(View.GONE);
            backToEvent.setVisibility(View.VISIBLE);
        }

        if (eventId == null) {
            Toast.makeText(ShowEventPlans.this, "Nie znaleziono wydarzenia",
                    Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        if (eventName != null) {
            TextView eventNameText = findViewById(R.id.eventName);
            eventNameText.setText(eventName);
        }

        eventPlans = findViewById(R.id.eventplans);

        database = FirebaseFirestore.getInstance();

        database.collection("eventsPlans")
                .whereEqualTo("eventId", eventId)
                .orderBy("startTime")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        agenda.clear();
                        if (!value.isEmpty()) {
                            for (DocumentSnapshot snapshot : value) {
                                EventPlan eventPlan = snapshot.toObject(EventPlan.class);
                                eventPlan.Id = snapshot.getId();
                                agenda.add(eventPlan);
                            }
                        }

                        EventPlanAdapter eventPlanAdapter = new EventPlanAdapter(agenda, getApplicationContext());
                        eventPlanAdapter.notifyDataSetChanged();
                        eventPlans.setAdapter(eventPlanAdapter);
                        if (editMode) {
                            // Edit plan
                            eventPlans.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                    EventPlan data = agenda.get(position);
                                    Intent i = new Intent(getApplicationContext(), EditEventPlan.class);
                                    i.putExtra("eventId", eventId);
                                    i.putExtra("eventName", eventName);
                                    i.putExtra("eventPlanId", data.getId());
                                    startActivity(i);
                                }
                            });
                        }
                    }
                });

        //#region buttons
        backToEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), EditEvent.class);
                i.putExtra("eventId", eventId);
                startActivity(i);
            }
        });

        backToEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), ShowEvent.class);
                i.putExtra("eventId", eventId);
                startActivity(i);
            }
        });

        addPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), AddEventPlan.class);
                i.putExtra("eventId", eventId);
                i.putExtra("eventName", eventName);
                startActivity(i);
            }
        });
        //#endregion
    }
}