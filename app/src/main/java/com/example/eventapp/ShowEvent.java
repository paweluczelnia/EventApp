package com.example.eventapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import static android.widget.Toast.LENGTH_SHORT;

public class ShowEvent extends AppCompatActivity implements OnMapReadyCallback {
    TextView evTitle, evLocation, evDate, evTime, evTicket,ticket;
    Button showAllEv, showPlan, goToEditEvent;
    FirebaseFirestore database;
    Event ev;
    Boolean isEventOwner = false;
    ToggleButton favBtn;
    String userID;
    private GoogleMap gMap;

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
        favBtn = findViewById(R.id.addToFavBtn);
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
                if(task.isSuccessful()){
                    DocumentSnapshot documentFav = task.getResult();
                    if(documentFav.exists()){
                        String evId = documentFav.getString(eventId);
//                        Toast.makeText(ShowEvent.this, evId, LENGTH_SHORT).show();
                        favBtn.setChecked(true);
                        if(evId == null){
                            favBtn.setChecked(false);
                        }
                    }
                }
            }
        });

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
                            if (userID.equals(ev.AuthorId)) {
                                goToEditEvent.setVisibility(View.VISIBLE);
                                isEventOwner = true;
                            }

                            // Set map marker
                            updateMarker();
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
            @Override
            public void onClick(View view) {
                database.collection("favorites").document(userID);
                if(favBtn.isChecked()) {
                    Map<String, Object> fav = new HashMap<>();
                    DocumentReference documentReference = database.collection("favorites").document(userID);
                    documentReference.update(eventId, userID);
                    Toast.makeText(ShowEvent.this, "Dodano do ulubionych", LENGTH_SHORT).show();
                }else{
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
}