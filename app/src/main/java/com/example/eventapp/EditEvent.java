package com.example.eventapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EditEvent extends AppCompatActivity {
    EditText mEventName, mDateEvent, mTimeEvent;
    TextView eventPlace;
    Button mEditEventBtn, mShowAllEventsBtn;
    FloatingActionButton mAddMapMarkerBtn, mEditEventElementBtn;
    CheckBox mTicketEvent;
    FirebaseAuth fAuth;
    FirebaseFirestore database;
    SharedPreferences sharedPref;
    SharedPreferences.Editor sharedPrefEditor;

    Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        event = new Event();

        setContentView(R.layout.activity_edit_event);
        //#region view elements
        mEventName = findViewById(R.id.editEventName);
        mDateEvent = findViewById(R.id.editDateEvent);
        mTimeEvent = findViewById(R.id.editTimeEvent);
        mEditEventBtn = findViewById(R.id.editEvent);
        mShowAllEventsBtn = findViewById(R.id.showAllEvents);
        mAddMapMarkerBtn = findViewById(R.id.editLocationOfEvent);
        mEditEventElementBtn = findViewById(R.id.editElementOfEvent);
        mTicketEvent = findViewById(R.id.ticketEvent);
        eventPlace = findViewById(R.id.eventPlace);
        //#endregion
        
        database = FirebaseFirestore.getInstance();
        //#region get edited event data
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
                            event.Ticket = (int)ticket;
                            event.Coordinates = document.getString("coordinates");
                            event.Name = document.getString("name");
                            event.AuthorId = document.getString("authorID");
                            event.EventDate = document.getString("dataTime").split(" ")[0];
                            event.EventTime = document.getString("dataTime").split(" ")[1];
                            event.EventId = eventId;
                            setInputData();
                            setLocationName(event.Coordinates);
                        } else {
                            Toast.makeText(EditEvent.this, "Nie udało się pobrać wydarzenia",
                                    Toast.LENGTH_SHORT).show();
                            redirect();
                        }
                    } else {
                        Toast.makeText(EditEvent.this, "Nie udało się pobrać wydarzenia",
                                Toast.LENGTH_SHORT).show();
                        redirect();
                    }
                }
            });
        }
        //#endregion

        sharedPref = getSharedPreferences("event", Context.MODE_PRIVATE);
        sharedPrefEditor = sharedPref.edit();

        fAuth = FirebaseAuth.getInstance();

        // Check coordinates from map exists
        String coordinatesFromMap = getIntent().getStringExtra("coordinates");
        if (coordinatesFromMap != null && !coordinatesFromMap.equals(event.Coordinates)) {
            event.Coordinates = coordinatesFromMap;
            setLocationName(coordinatesFromMap);
        }

        mEditEventBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDataFromInput();

                if (TextUtils.isEmpty(event.Name) || TextUtils.isEmpty(event.EventDate)
                        || TextUtils.isEmpty(event.EventTime)) {
                    Toast.makeText(EditEvent.this, "Uzupełnij wszystkie pola",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (event.Coordinates == null) {
                    Toast.makeText(EditEvent.this, "Dodaj miejsce wydarzenia",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd HH:mm");
                Date date = new Date(System.currentTimeMillis());
                if (coordinatesFromMap != null) {
                    event.Coordinates = coordinatesFromMap;
                }
                Map<String, Object> ev = new HashMap<>();
                ev.put("name", event.Name);
                ev.put("dataTime", event.EventDate + " " + event.EventTime);
                ev.put("ticket", event.Ticket);
                ev.put("coordinates", event.Coordinates);
                ev.put("updateDate", formatter.format(date));
                database.collection("events").document(event.EventId)
                        .update(ev)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                sharedPrefEditor.clear();
                                sharedPrefEditor.commit();

                                mEventName.setText("");
                                mDateEvent.setText("");
                                mTimeEvent.setText("");
                                mTicketEvent.setChecked(false);

                                Toast.makeText(EditEvent.this, "Wydarzenie zostało zapisane",
                                        Toast.LENGTH_SHORT).show();

                                Intent i = new Intent(getApplicationContext(), ShowEvent.class);
                                i.putExtra("eventId", event.EventId);
                                startActivity(i);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(EditEvent.this, "Nie udało się zapisać wydarzenia, spróbuj ponownie",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        mAddMapMarkerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (coordinatesFromMap != null) {
                    event.Coordinates = coordinatesFromMap;
                }
                Intent i = new Intent(getApplicationContext(), MappAddEvent.class);
                i.putExtra("coordinates", event.Coordinates);
                i.putExtra("activity", "EditEvent");
                startActivity(i);
            }
        });

        mEditEventElementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), ShowEventPlans.class);
                i.putExtra("eventId", event.EventId);
                i.putExtra("eventName", event.Name);
                startActivity(i);
            }
        });

        mShowAllEventsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),ShowAllEvents.class));
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        getDataFromInput();
        sharedPrefEditor.putString("eventName", event.Name);
        sharedPrefEditor.putString("eventDate", event.EventDate);
        sharedPrefEditor.putString("eventTime", event.EventTime);
        sharedPrefEditor.putInt("eventTicket", event.Ticket);
        sharedPrefEditor.putString("eventId", event.EventId);
        sharedPrefEditor.putString("eventAuthorId", event.AuthorId);
        sharedPrefEditor.putString("eventCoordinates", event.Coordinates);
        sharedPrefEditor.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        event.Name = sharedPref.getString("eventName", "");
        event.EventDate = sharedPref.getString("eventDate", null);
        event.EventTime = sharedPref.getString("eventTime", null);
        event.Ticket = sharedPref.getInt("eventTicket", 0);
        event.EventId = sharedPref.getString("eventId", "");
        event.Coordinates = sharedPref.getString("eventCoordinates", "");
        event.AuthorId = sharedPref.getString("eventAuthorId", "");

        setInputData();
    }

    private void getDataFromInput() {
        String eventName = mEventName.getText().toString().trim();
        String eventDate = mDateEvent.getText().toString().trim();
        String eventTime = mTimeEvent.getText().toString().trim();
        int ticketedEvent =  mTicketEvent.isChecked() ? 1 : 0;

        event.Name = eventName;
        event.EventDate = eventDate;
        event.EventTime = eventTime;
        event.Ticket = ticketedEvent;
    }

    private void setInputData() {
        mEventName.setText(event.Name);
        mDateEvent.setText(event.EventDate);
        mTimeEvent.setText(event.EventTime);
        mTicketEvent.setChecked(event.Ticket == 1 ? true : false);
    }
    private void redirect() {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }

    private void setLocationName(String coordinates) {
        String[] separatedCoords = coordinates.split(";");
        Geocoder gcd = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = gcd.getFromLocation(Double.parseDouble(separatedCoords[0]),
                    Double.parseDouble(separatedCoords[1]), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses != null)
        {
            String address = addresses.get(0).getAddressLine(0);
            String[] separatedAddress = address.split(",");
            address = address.replace(",", System.getProperty("line.separator"));
            if (address != null) {
                String fullAddress = separatedAddress[0] + System.getProperty("line.separator")
                        + separatedAddress[1];
                eventPlace.setText(fullAddress);
            }
        }
    }
}