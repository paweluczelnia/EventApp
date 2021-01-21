package com.example.eventapp;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import static android.widget.Toast.LENGTH_SHORT;

public class ShowEvent extends AppCompatActivity implements OnMapReadyCallback {
    TextView evTitle, evLocation, evDate, evTime, evTicket,ticket;
    Button showAllEv, showPlan, goToEditEvent;
    ProgressBar progressBar;
    FirebaseFirestore database;
    Event ev;
    Boolean isEventOwner = false;
    ToggleButton favBtn;
    String userID;
    private GoogleMap gMap;
    Long eventCalendarId = null;
    String endTime = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_event);
        progressBar = findViewById(R.id.progressBarEvDetails);
        progressBar.setVisibility(View.VISIBLE);
        evTicket = findViewById(R.id.shEvTicket);
        evTitle = findViewById(R.id.shEvTitle);
        evLocation = findViewById(R.id.shEvLocation);
        evDate = findViewById(R.id.shEvData);
        evTime = findViewById(R.id.shEvStartTime);
        showAllEv = findViewById(R.id.shEvBackToAll);
        showAllEv.setVisibility(View.GONE);
        showPlan = findViewById(R.id.shEvShowPlanBtn);
        showPlan.setVisibility(View.GONE);
        goToEditEvent = findViewById(R.id.goToEditEvent);
        favBtn = findViewById(R.id.addToFavBtn);
        favBtn.setVisibility(View.GONE);
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_location);
        mapFragment.getMapAsync(this);

        //#region get event data
        database = FirebaseFirestore.getInstance();
        String eventId = getIntent().getStringExtra("eventId");
        DocumentReference docRefFav = database.collection("favorites").document(userID);
        Map<String, Object> favori = new HashMap<>();
        favori.put("UserId", FieldValue.delete());
        docRefFav.update(favori);
        docRefFav.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentFav = task.getResult();
                    if (documentFav.exists()) {
                        eventCalendarId = documentFav.getLong(eventId);
                        favBtn.setChecked(true);
                        if (eventCalendarId == null) {
                            favBtn.setChecked(false);
                        }
                    }
                }
            }
        });

        if (eventId != null) {
            // get last end time from event's plans
            database.collection("eventsPlans").whereEqualTo("eventId", eventId)
                    .orderBy("endTime", Query.Direction.DESCENDING).limit(1).get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        List<String> plans = new ArrayList<>();
                        List<DocumentSnapshot> ds = task.getResult().getDocuments();
                        if (ds.size() > 0) {
                            endTime = task.getResult().getDocuments().get(0).getString("endTime");
                        }
                    }
                }
            });

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
                            if (userID.equals(ev.AuthorId)) {
                                goToEditEvent.setVisibility(View.VISIBLE);
                                isEventOwner = true;
                            }

                            // Set map marker
                            updateMarker();
                            progressBar.setVisibility(View.GONE);
                            showAllEv.setVisibility(View.VISIBLE);
                            showPlan.setVisibility(View.VISIBLE);
                            favBtn.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(ShowEvent.this, "Nie udało się pobrać wydarzenia",
                                    LENGTH_SHORT).show();
                            redirect();
                        }
                    } else {
                        Toast.makeText(ShowEvent.this, "Nie udało się pobrać wydarzenia",
                                LENGTH_SHORT).show();
                        redirect();
                    }
                }
            });
        }
        //#endregion

        //#region buttons listener

        favBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                database.collection("favorites").document(userID);
                if (favBtn.isChecked()) {
                    ContentResolver cr = getContentResolver();
                    ContentValues values = new ContentValues();

                    Integer calId = getCalendarId(getApplicationContext());

                    //#region calculate event datetime
                    String[] date = ev.getEventDate().split("-");
                    Integer year = Integer.parseInt(date[0]);
                    Integer month = Integer.parseInt(date[1]) - 1;
                    Integer day = Integer.parseInt(date[2]);
                    String[] time = ev.getEventTime().split(":");
                    Integer hour = Integer.parseInt(time[0]);
                    Integer minute = Integer.parseInt(time[1]);

                    Calendar beginTime = Calendar.getInstance();
                    beginTime.set(year, month, day, hour, minute);
                    long startMillis = beginTime.getTimeInMillis();

                    Integer calculatedEndHour = hour + 1;
                    Integer calculatedEndMinute = minute;

                    if (endTime != null) {
                        LocalTime start = LocalTime.parse(ev.getEventTime());
                        LocalTime end = LocalTime.parse(endTime);

                        if (end.isAfter(start)) {
                            String[] splitedEndTime = endTime.split(":");
                            calculatedEndHour = Integer.parseInt(splitedEndTime[0]);
                            calculatedEndMinute = Integer.parseInt(splitedEndTime[1]);
                        }
                    }

                    Calendar endTime = Calendar.getInstance();
                    endTime.set(year, month, day, calculatedEndHour, calculatedEndMinute);
                    long endMillis = endTime.getTimeInMillis();

                    //#endregion
                    values.put(CalendarContract.Events.DTSTART, startMillis);
                    values.put(CalendarContract.Events.DTEND, endMillis);
                    values.put(CalendarContract.Events.TITLE, ev.getName());
                    values.put(CalendarContract.Events.EVENT_LOCATION, ev.getLocationName());
                    values.put(CalendarContract.Events.CALENDAR_ID, calId);
                    values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
                    Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);

                    // get the event ID that is the last element in the Uri
                    eventCalendarId = Long.parseLong(uri.getLastPathSegment());

                    // add reminder
                    Integer reminderMinutes = 60*24; // 24h
                    Long currentTime = System.currentTimeMillis();
                    Long difference = startMillis - currentTime;
                    if (difference <= 60 * 60 * 1000 * 24) {
                        int hours = (int) ((difference / (1000*60*60)) % 24);
                        if (hours - 1 > 1) {
                            reminderMinutes = (hours - 1) * 60;
                        } else {
                            int minutes = (int) ((difference / (1000 * 60)) % 60);

                            if (minutes - 5 > 5) {
                                reminderMinutes = minutes - 5;
                            } else {
                                reminderMinutes = minutes;
                            }
                        }
                    }

                    ContentValues valuesR = new ContentValues();
                    valuesR.put(CalendarContract.Reminders.MINUTES, reminderMinutes);
                    valuesR.put(CalendarContract.Reminders.EVENT_ID, eventCalendarId);
                    valuesR.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
                    Uri uriR = cr.insert(CalendarContract.Reminders.CONTENT_URI, valuesR);
                    //#endregion
                    DocumentReference documentReference = database.collection("favorites").document(userID);
                    documentReference.update(eventId, eventCalendarId);
                    Toast.makeText(ShowEvent.this, "Dodano do ulubionych", LENGTH_SHORT).show();
                } else {
                    if (eventCalendarId != null) {
                        ContentResolver cr = getContentResolver();
                        Uri deleteUri = null;
                        deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventCalendarId);
                        int rows = cr.delete(deleteUri, null, null);
                    }

                    Map<String, Object> fav = new HashMap<>();
                    DocumentReference documentReference = database.collection("favorites").document(userID);
                    fav.put(eventId, FieldValue.delete());
                    documentReference.update(fav);
                    Toast.makeText(ShowEvent.this, "Usunięto z ulubionych", LENGTH_SHORT).show();
                }
            }
        });

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
        progressBar.setVisibility(View.GONE);
        startActivity(new Intent(getApplicationContext(), ShowAllEvents.class));
        finish();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        if (ev != null && ev.Coordinates != null) {
            updateMarker();
        }
    }

    private void updateMarker() {
        String[] separatedCoords = ev.Coordinates.split(";");
        LatLng latLng = new LatLng(Double.parseDouble(separatedCoords[0]),
                Double.parseDouble(separatedCoords[1]));

        MarkerOptions options = new MarkerOptions().position(latLng);
        gMap.clear();
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
        gMap.addMarker(options);
    }

    private int getCalendarId(Context context){

        Cursor cursor = null;
        ContentResolver contentResolver = context.getContentResolver();
        Uri calendars = CalendarContract.Calendars.CONTENT_URI;

        String[] EVENT_PROJECTION = new String[] {
                CalendarContract.Calendars._ID,                           // 0
                CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
                CalendarContract.Calendars.OWNER_ACCOUNT,                 // 3
                CalendarContract.Calendars.IS_PRIMARY                     // 4
        };

        int PROJECTION_ID_INDEX = 0;
        int PROJECTION_ACCOUNT_NAME_INDEX = 1;
        int PROJECTION_DISPLAY_NAME_INDEX = 2;
        int PROJECTION_OWNER_ACCOUNT_INDEX = 3;
        int PROJECTION_VISIBLE = 4;

        cursor = contentResolver.query(calendars, EVENT_PROJECTION, null, null, null);

        if (cursor.moveToFirst()) {
            String calName;
            long calId = 0;
            String visible;

            do {
                calName = cursor.getString(PROJECTION_DISPLAY_NAME_INDEX);
                calId = cursor.getLong(PROJECTION_ID_INDEX);
                visible = cursor.getString(PROJECTION_VISIBLE);
                if (visible != null){
                    return (int)calId;
                }
            } while (cursor.moveToNext());

            return (int)calId;
        }
        return 1;
    }
}