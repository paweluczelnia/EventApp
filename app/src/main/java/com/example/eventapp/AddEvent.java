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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddEvent extends AppCompatActivity {
    EditText mEventName, mDateEvent, mTimeEvent;
    Button mAddEventBtn, mShowAllEventsBtn;
    FloatingActionButton mAddMapMarkerBtn, mAddEventElementBtn;
    CheckBox mTicketEvent;
    FirebaseAuth fAuth;
    ProgressBar progressBar;
    String userID;
    FirebaseFirestore fStore;
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
        fStore = FirebaseFirestore.getInstance();


        mAddEventBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String eventName =  mEventName.getText().toString().trim();
                String dateEvent = mDateEvent.getText().toString().trim();
                String timeEvent = mTimeEvent.getText().toString().trim();

                if(TextUtils.isEmpty(eventName) || TextUtils.isEmpty(dateEvent) || TextUtils.isEmpty(timeEvent)){
                Toast.makeText(AddEvent.this, "Uzupełnij wszystkie pola", Toast.LENGTH_SHORT).show();
                return;
                }
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                progressBar.setVisibility(View.VISIBLE);

            }
        });

        mAddMapMarkerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), MappAddEvent.class));
                progressBar.setVisibility(View.VISIBLE);
            }
        });



    }
}