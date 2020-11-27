package com.example.eventapp;

import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ShowAllEvents extends AppCompatActivity {
    RecyclerView recyclerView;
    MyAdapter adapter;
    DatabaseReference reference;
    ArrayList<Event> list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_all_events);
        recyclerView = (RecyclerView)findViewById(R.id.showAllEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        list = new ArrayList<Event>();

        reference = FirebaseDatabase.getInstance().getReference().child("events");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()){
                    Event e = dataSnapshot1.getValue(Event.class);
                    list.add(e);
                }

                adapter = new MyAdapter(ShowAllEvents.this, list);
                recyclerView.setAdapter(adapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ShowAllEvents.this, "Opss i didn't again", Toast.LENGTH_SHORT).show();
            }
        });
    }
}