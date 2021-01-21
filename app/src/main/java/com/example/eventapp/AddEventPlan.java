package com.example.eventapp;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
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

public class AddEventPlan extends AppCompatActivity {
    EditText mName, mDescription, mStartTime, mEndTime;
    String eventName;

    EventPlan eventPlan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event_plan);

        String eventId = getIntent().getStringExtra("eventId");
        eventName = getIntent().getStringExtra("eventName");
        if (eventId == null) {
            Toast.makeText(AddEventPlan.this, "Nie znaleziono wydarzenia",
                    Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        eventPlan = new EventPlan();
        eventPlan.EventId = eventId;

        //#region view elements
        mName = findViewById(R.id.nameElement);
        mDescription = findViewById(R.id.descriptionElement);
        mStartTime = findViewById(R.id.startTimeElement);
        mEndTime = findViewById(R.id.endTimeElement);
        //#endregion

        //#region time pickers
        Calendar cldr = Calendar.getInstance(new Locale("pl"));

        mStartTime.setInputType(InputType.TYPE_NULL);
        mStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int hour = cldr.get(Calendar.HOUR_OF_DAY);
                int minutes = cldr.get(Calendar.MINUTE);
                // time picker dialog
                TimePickerDialog picker = new TimePickerDialog(AddEventPlan.this,
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
                TimePickerDialog picker = new TimePickerDialog(AddEventPlan.this,
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

        //#region add buttons
        Button mAddAndAddNextElementBtn = findViewById(R.id.addAndAddNextBtn);
        Button mAddAndExitBtn = findViewById(R.id.addAndExitBtn);
        Button mCancelAddingPlanBtn = findViewById(R.id.cancelAddingPlan);

        mAddAndExitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createEventPlan(true);
            }
        });

        mAddAndAddNextElementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createEventPlan(false);
            }
        });

        mCancelAddingPlanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), ShowEventPlans.class);
                i.putExtra("eventId", eventPlan.EventId);
                i.putExtra("eventName", eventName);
                startActivity(i);
                finish();
            }
        });
        //#endregion
    }

    private Boolean createEventPlan(Boolean addNext) {
        getDataFromInput();

        if (!validateData()) {
            Toast.makeText(AddEventPlan.this, "Uzupełnij wszystkie pola",
                    Toast.LENGTH_SHORT).show();
            return false;
        } else if (!validateTime()) {
            Toast.makeText(AddEventPlan.this, "Podano zły czas wydarzenia",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference reference = database.collection("eventsPlans").document();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = new Date(System.currentTimeMillis());

        Map<String, Object> plan = new HashMap<>();
        plan.put("name", eventPlan.Name);
        plan.put("description", eventPlan.Description);
        plan.put("startTime", eventPlan.StartTime);
        plan.put("endTime", eventPlan.EndTime);
        plan.put("eventId", eventPlan.EventId);
        plan.put("addedDate", formatter.format(date));

        reference.set(plan).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mName.setText("");
                mDescription.setText("");
                mEndTime.setText("");
                mStartTime.setText("");

                Toast.makeText(AddEventPlan.this, "Element wydarzenia został dodany",
                        Toast.LENGTH_SHORT).show();

                if (addNext) {
                    Intent i = new Intent(getApplicationContext(), ShowEventPlans.class);
                    i.putExtra("eventId", eventPlan.EventId);
                    i.putExtra("eventName", eventName);
                    startActivity(i);
                    finish();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddEventPlan.this, "Nie udało się dodać planu wydarzenia",
                        Toast.LENGTH_SHORT).show();
            }
        });

        return true;
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

}