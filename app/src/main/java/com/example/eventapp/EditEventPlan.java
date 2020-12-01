package com.example.eventapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class EditEventPlan extends AppCompatActivity {
    EditText mName, mDescription, mStartTime, mEndTime;
    String eventName;
    FirebaseFirestore database;
    EventPlan eventPlan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event_plan);

        String eventId = getIntent().getStringExtra("eventId");
        eventName = getIntent().getStringExtra("eventName");
        String eventPlanId = getIntent().getStringExtra("eventPlanId");
        if (eventId == null || eventPlanId == null) {
            Toast.makeText(EditEventPlan.this, "Nie znaleziono planu wydarzenia",
                    Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        database = FirebaseFirestore.getInstance();

        //#region view elements
        mName = findViewById(R.id.nameElement);
        mDescription = findViewById(R.id.descriptionElement);
        mStartTime = findViewById(R.id.startTimeElement);
        mEndTime = findViewById(R.id.endTimeElement);
        //#endregion

        // get eventPlan data
        DocumentReference docRef = database.collection("eventsPlans").document(eventPlanId);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        eventPlan = document.toObject(EventPlan.class);
                        eventPlan.EventId = eventId;
                        eventPlan.Id = eventPlanId;
                        setInputData();
                    } else {
                        Toast.makeText(EditEventPlan.this, "Nie udało się pobrać wydarzenia",
                                Toast.LENGTH_SHORT).show();
                        redirect();
                    }
                } else {
                    Toast.makeText(EditEventPlan.this, "Nie udało się pobrać wydarzenia",
                            Toast.LENGTH_SHORT).show();
                    redirect();
                }
            }
        });

        //#region time pickers
        Calendar cldr = Calendar.getInstance(new Locale("pl"));

        mStartTime.setInputType(InputType.TYPE_NULL);
        mStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int hour = cldr.get(Calendar.HOUR_OF_DAY);
                int minutes = cldr.get(Calendar.MINUTE);
                // time picker dialog
                TimePickerDialog picker = new TimePickerDialog(EditEventPlan.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker tp, int sHour, int sMinute) {
                                cldr.set(Calendar.HOUR_OF_DAY, sHour);
                                cldr.set(Calendar.MINUTE, sMinute);
                                mStartTime.setText(String.format("%02d:%02d", sHour, sMinute));
                            }
                        }, hour, minutes, true);
                picker.show();
            }
        });

        mEndTime.setInputType(InputType.TYPE_NULL);
        mEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int hour = cldr.get(Calendar.HOUR_OF_DAY);
                int minutes = cldr.get(Calendar.MINUTE);
                // time picker dialog
                TimePickerDialog picker = new TimePickerDialog(EditEventPlan.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker tp, int sHour, int sMinute) {
                                cldr.set(Calendar.HOUR_OF_DAY, sHour);
                                cldr.set(Calendar.MINUTE, sMinute);
                                mEndTime.setText(String.format("%02d:%02d", sHour, sMinute));
                            }
                        }, hour, minutes, true);
                picker.show();
            }
        });
        //#endregion

        //#region buttons listener
        Button saveEventPlan = findViewById(R.id.saveEventPlan);
        Button removeEventPlan = findViewById(R.id.removeEventPlan);
        Button cancelEditPlan = findViewById(R.id.cancelEditPlan);
        saveEventPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDataFromInput();

                if (!validateData()) {
                    Toast.makeText(EditEventPlan.this, "Uzupełnij wszystkie pola",
                            Toast.LENGTH_SHORT).show();
                    return;
                } else if (!validateTime()) {
                    Toast.makeText(EditEventPlan.this, "Podano zły czas wydarzenia",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                Date date = new Date(System.currentTimeMillis());

                Map<String, Object> plan = new HashMap<>();
                plan.put("name", eventPlan.Name);
                plan.put("description", eventPlan.Description);
                plan.put("startTime", eventPlan.StartTime);
                plan.put("endTime", eventPlan.EndTime);
                plan.put("eventId", eventPlan.EventId);
                plan.put("addedDate", formatter.format(date));

                database.collection("eventsPlans").document(eventPlan.Id)
                        .update(plan)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mName.setText("");
                                mDescription.setText("");
                                mEndTime.setText("");
                                mStartTime.setText("");

                                Toast.makeText(EditEventPlan.this, "Plan zapisano pomyślnie",
                                        Toast.LENGTH_SHORT).show();

                                redirect();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(EditEventPlan.this, "Nie udało się zapisać planu wydarzenia",
                                        Toast.LENGTH_SHORT).show();
                                Log.d("TAG", "onFailure:  " + e.toString());
                            }
                        });
            }
        });

        removeEventPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                database.collection("eventsPlans").document(eventPlan.Id)
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(EditEventPlan.this, "Usunięto plan wydarzenia",
                                        Toast.LENGTH_SHORT).show();

                                redirect();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(EditEventPlan.this, "Nie udało się usunąć planu wydarzenia",
                                        Toast.LENGTH_SHORT).show();
                                Log.d("TAG", "onFailure:  " + e.toString());
                            }
                        });
            }
        });

        cancelEditPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                redirect();
            }
        });
        //#endregion
    }

    private void setInputData() {
        mName.setText(eventPlan.Name);
        mDescription.setText(eventPlan.Description);
        mStartTime.setText(eventPlan.StartTime);
        mEndTime.setText(eventPlan.EndTime);
    }

    private void getDataFromInput() {
        eventPlan.Name = mName.getText().toString().trim();
        eventPlan.Description = mDescription.getText().toString().trim();
        eventPlan.StartTime = mStartTime.getText().toString().trim();;
        eventPlan.EndTime = mEndTime.getText().toString().trim();;
    }

    private Boolean validateData() {
        if (TextUtils.isEmpty(eventPlan.Name) || TextUtils.isEmpty(eventPlan.Description)
                || TextUtils.isEmpty(eventPlan.StartTime) || TextUtils.isEmpty(eventPlan.EndTime)) {
            return false;
        }

        return true;
    }

    private Boolean validateTime() {
        SimpleDateFormat format = new SimpleDateFormat("hh:mm");
        Date startTime = null;
        Date endTime = null;

        try {
            startTime = format.parse(eventPlan.StartTime);
            endTime = format.parse(eventPlan.EndTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return startTime.after(endTime) ? false : true;
    }

    private void redirect() {
        Intent i = new Intent(getApplicationContext(), ShowEventPlans.class);
        i.putExtra("eventId", eventPlan.EventId);
        i.putExtra("eventName", eventName);
        startActivity(i);
        finish();
    }
}