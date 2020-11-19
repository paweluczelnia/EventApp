package com.example.eventapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddEvent extends AppCompatActivity {
    EditText mEventName, mDateEvent, mTimeEvent;
    Button mAddEventBtn, mShowAllEventsBtn;
    FloatingActionButton mAddMapMarkerBtn, mAddEventElementBtn;
    CheckBox mTicketEvent;
    ProgressBar progressBar;
    FirebaseAuth fAuth;
    String userID;
    FirebaseFirestore database;
    private static final String TAG = "TAG";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);
        mEventName = findViewById(R.id.addEventName);
        mDateEvent = findViewById(R.id.addDateEvent);
        mTimeEvent = findViewById(R.id.addTimeEvent);
        mAddEventBtn = findViewById(R.id.addEvent);
        mShowAllEventsBtn = findViewById(R.id.showAllEvents);
        mAddMapMarkerBtn = findViewById(R.id.addLocationOfEvent);
        mAddEventElementBtn = findViewById(R.id.addElementOfEvent);
        mTicketEvent = findViewById(R.id.ticketEvent);
        progressBar = findViewById(R.id.progressBar2);
        fAuth = FirebaseAuth.getInstance();


        mAddEventBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String no = "NO";
                String yes = "YES";
                String eventName =  mEventName.getText().toString().trim();
                String dateEvent = mDateEvent.getText().toString().trim();
                String timeEvent = mTimeEvent.getText().toString().trim();
                String ticketEvent;
                if(mTicketEvent.isChecked()){
                                            ticketEvent = yes;
                                        }else{
                                            ticketEvent = no;
                                        }

                userID = fAuth.getCurrentUser().getUid();
                database = FirebaseFirestore.getInstance();
                DocumentReference reference = database.collection("events").document();
                if(TextUtils.isEmpty(eventName) || TextUtils.isEmpty(dateEvent) || TextUtils.isEmpty(timeEvent)){
                Toast.makeText(AddEvent.this, "Uzupełnij wszystkie pola", Toast.LENGTH_SHORT).show();
                return;
                }
                Map<String,Object> event = new HashMap<>();
                event.put("name", eventName);
                event.put("dataTime", dateEvent+" "+timeEvent);
                event.put("ticket", ticketEvent);
                event.put("authorID", userID);
                reference.set(event).addOnSuccessListener((OnSuccessListener) (aVoid)->{
                                Log.d(TAG, "Utworzono wydarzenie przez użytkownika: "+userID);
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure:  " + e.toString());
                    }
                });

                startActivity(new Intent(getApplicationContext(), MainActivity.class));

            }
        });

        mAddMapMarkerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), MappAddEvent.class));

            }
        });

        mAddEventElementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), AddEventPlan.class));
            }
        });



    }
}