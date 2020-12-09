package com.example.eventapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.appcompat.app.AppCompatActivity;

public class ShowEvent extends AppCompatActivity {
    TextView evTitle, evLocation, evDate, evTime, evTicket,ticket;
    Button showAllEv, showPlan;
    DatabaseReference databaseReference, favreference;
    FirebaseDatabase database;
    Boolean favChecker = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_event);
        evTicket = findViewById(R.id.shEvTicket);
        evTitle = findViewById(R.id.shEvTitle);
        evLocation = findViewById(R.id.shEvLocation);
        evDate = findViewById(R.id.shEvData);
        evTime = findViewById(R.id.shEvStartTime);
        showAllEv = findViewById(R.id.shEvShowPlanBtn);
        showPlan = findViewById(R.id.shEvShowPlanBtn);

        Event ev = new Event();
        evTitle.setText(ev.getName());
        evLocation.setText(ev.getLocationName());
        ticket.setText(ev.getTicket());
        if(ticket.equals("1")){
            evTicket.setText("Wydarzenie biletowane");
        }else{
            evTicket.setText("Wydarzenie nie jest biletowane");
        }
        evDate.setText(ev.getEventDate());
        evTime.setText(ev.getEventTime());

        showAllEv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), ShowAllEvents.class));
            }
        });
        showPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), ShowEventPlans.class));
            }
        });
    }
}