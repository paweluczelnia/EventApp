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
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
    SharedPreferences sharedPref;
    SharedPreferences.Editor sharedPrefEditor;

    String coordinates;

    Event event;

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPref = getSharedPreferences("event", Context.MODE_PRIVATE);
        sharedPrefEditor = sharedPref.edit();

        setContentView(R.layout.activity_add_event);

        event = new Event();

        //#region view elements
        mEventName = findViewById(R.id.addEventName);
        mDateEvent = findViewById(R.id.addDateEvent);
        mTimeEvent = findViewById(R.id.addTimeEvent);
        mAddEventBtn = findViewById(R.id.addEvent);
        mShowAllEventsBtn = findViewById(R.id.showAllEvents);
        mAddMapMarkerBtn = findViewById(R.id.addLocationOfEvent);
        mAddEventElementBtn = findViewById(R.id.addElementOfEvent);
        mTicketEvent = findViewById(R.id.ticketEvent);
        progressBar = findViewById(R.id.progressBar2);
        TextView eventPlace = findViewById(R.id.eventPlace);
        //#endregion

        final Calendar cldr = Calendar.getInstance(new Locale("pl"));

        //#region datePicker on EditText
        mDateEvent.setInputType(InputType.TYPE_NULL);
        mDateEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                // date picker dialog
                DatePickerDialog picker = new DatePickerDialog(AddEvent.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                cldr.set(year, monthOfYear, dayOfMonth);
                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                                String strDate = format.format(cldr.getTime());
                                //mDateEvent.setText(year + "-" + (monthOfYear + 1) + "-" +  dayOfMonth);
                                mDateEvent.setText(strDate);
                            }
                        }, year, month, day);
                picker.show();
            }
        });
        //#endregion

        //#region timePicker on EditText
        mTimeEvent.setInputType(InputType.TYPE_NULL);
        mTimeEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int hour = cldr.get(Calendar.HOUR_OF_DAY);
                int minutes = cldr.get(Calendar.MINUTE);
                // time picker dialog
                TimePickerDialog picker = new TimePickerDialog(AddEvent.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker tp, int sHour, int sMinute) {
                                cldr.set(Calendar.HOUR_OF_DAY, sHour);
                                cldr.set(Calendar.MINUTE, sMinute);
                                mTimeEvent.setText(String.format("%02d:%02d", sHour, sMinute));
                            }
                        }, hour, minutes, true);
                picker.show();
            }
        });
        //#endregion
        fAuth = FirebaseAuth.getInstance();

        coordinates = getIntent().getStringExtra("coordinates");

        // Set address from coordinates
        if (coordinates != null) {
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

        mAddEventBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDataFromInput();

                if (TextUtils.isEmpty(event.Name) || TextUtils.isEmpty(event.EventDate)
                        || TextUtils.isEmpty(event.EventTime)) {
                    Toast.makeText(AddEvent.this, "Uzupełnij wszystkie pola",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (coordinates == null) {
                    Toast.makeText(AddEvent.this, "Dodaj miejsce wydarzenia",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                userID = fAuth.getCurrentUser().getUid();
                database = FirebaseFirestore.getInstance();

                SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd HH:mm");
                Date date = new Date(System.currentTimeMillis());

                // @TODO jak już będzie obsługa planu wydarzeń, to sam event do klasy, a w nim kolekcja z agendą i później foreach'em po niej i save do firebase
                Map<String, Object> ev = new HashMap<>();
                ev.put("name", event.Name);
                ev.put("dataTime", event.EventDate + " " + event.EventTime);
                ev.put("ticket", event.Ticket);
                ev.put("authorId", userID);
                ev.put("coordinates", coordinates);
                ev.put("addedDate", formatter.format(date));
                database.collection("events")
                        .add(ev)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                sharedPrefEditor.clear();
                                sharedPrefEditor.commit();

                                mEventName.setText("");
                                mDateEvent.setText("");
                                mTimeEvent.setText("");
                                mTicketEvent.setChecked(false);

                                Toast.makeText(AddEvent.this, "Wydarzenie zostało dodane",
                                        Toast.LENGTH_SHORT).show();

                                Intent i = new Intent(getApplicationContext(), EditEvent.class);
                                i.putExtra("eventId", documentReference.getId());
                                startActivity(i);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(AddEvent.this, "Nie udało się zapisać wydarzenia, spróbuj ponownie",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        mAddMapMarkerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), MappAddEvent.class);
                i.putExtra("coordinates", coordinates);
                startActivity(i);
            }
        });

        mAddEventElementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(AddEvent.this, "Najpierw zapisz wydarzenie",
                        Toast.LENGTH_SHORT).show();
                //startActivity(new Intent(getApplicationContext(), AddEventPlan.class));
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        getDataFromInput();
        //@TODO obczaić jak to fajnie przerobić żeby zapisywać obiekt
        sharedPrefEditor.putString("eventName", event.Name);
        sharedPrefEditor.putString("eventDate", event.EventDate);
        sharedPrefEditor.putString("eventTime", event.EventTime);
        sharedPrefEditor.putInt("eventTicket", event.Ticket);
        sharedPrefEditor.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();

        event.Name = sharedPref.getString("eventName", "");
        event.EventDate = sharedPref.getString("eventDate", "");
        event.EventTime = sharedPref.getString("eventTime", "");
        event.Ticket = sharedPref.getInt("eventTicket", 0);

        mEventName.setText(event.Name);
        mDateEvent.setText(event.EventDate);
        mTimeEvent.setText(event.EventTime);
        mTicketEvent.setChecked(event.Ticket == 1 ? true : false);
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
}