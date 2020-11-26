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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class AddEventPlan extends AppCompatActivity {
    EditText mNameElement, mDescriptionElement, mDateElement, mStartTimeElement, mEndTimeElement;
    Button mAddAndExitBtn, mAddAndAddNextElementBtn;
    FirebaseAuth fAuth;
    String eventID;
    FirebaseFirestore database;
    SharedPreferences sharedPref;
    SharedPreferences.Editor sharedPrefEditor;
    private static final String TAG = "TAG";


    EventElement eventElement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event_plan);

        sharedPref = getSharedPreferences("eventElement", Context.MODE_PRIVATE);
        sharedPrefEditor = sharedPref.edit();

        setContentView(R.layout.activity_add_event_plan);

        eventElement = new EventElement();

        //#region view elements
        mNameElement = findViewById(R.id.nameElement);
        mDescriptionElement = findViewById(R.id.descriptionElement);
        mDateElement = findViewById(R.id.dateElement);
        mStartTimeElement = findViewById(R.id.startTimeElement);
        mEndTimeElement = findViewById(R.id.endTimeElement);
        mAddAndAddNextElementBtn = findViewById(R.id.addAndAddNextBtn);
        mAddAndExitBtn = findViewById(R.id.addAndExitBtn);

        final Calendar cldr = Calendar.getInstance(new Locale("pl"));

        mDateElement.setInputType(InputType.TYPE_NULL);
        mDateElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                // date picker dialog
                DatePickerDialog picker = new DatePickerDialog(AddEventPlan.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                cldr.set(year, monthOfYear, dayOfMonth);
                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                                String strDate = format.format(cldr.getTime());
                                //mDateEvent.setText(year + "-" + (monthOfYear + 1) + "-" +  dayOfMonth);
                                mDateElement.setText(strDate);
                            }
                        }, year, month, day);
                picker.show();
            }
        });

        mStartTimeElement.setInputType(InputType.TYPE_NULL);
        mStartTimeElement.setOnClickListener(new View.OnClickListener() {
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
                                mStartTimeElement.setText(String.format("%02d:%02d", sHour, sMinute));
                            }
                        }, hour, minutes, true);
                picker.show();
            }
        });

        mEndTimeElement.setInputType(InputType.TYPE_NULL);
        mEndTimeElement.setOnClickListener(new View.OnClickListener() {
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
                                mEndTimeElement.setText(String.format("%02d:%02d", sHour, sMinute));
                            }
                        }, hour, minutes, true);
                picker.show();
            }
        });
        mAddAndExitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDataFromInput();
                if(TextUtils.isEmpty(eventElement.EventElementName) || TextUtils.isEmpty(eventElement.EventElementDesc) || TextUtils.isEmpty(eventElement.EventElementDate)
                    || TextUtils.isEmpty(eventElement.EventElementStartTime) || TextUtils.isEmpty(eventElement.EventElementEndTime)){

                    Toast.makeText(AddEventPlan.this, "Uzupełnij wszystkie pola",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                database = FirebaseFirestore.getInstance();
                DocumentReference reference = database.collection("eventsPlans").document();

                SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd HH:mm");
                Date date = new Date(System.currentTimeMillis());

                Map<String, Object> plan = new HashMap<>();
                plan.put("name_element", eventElement.EventElementName);
                plan.put("description", eventElement.EventElementDesc);
                plan.put("date_element", eventElement.EventElementDate);
                plan.put("start_time", eventElement.EventElementStartTime);
                plan.put("end_time", eventElement.EventElementEndTime);
                plan.put("addedDate", formatter.format(date));

                reference.set(plan).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        sharedPrefEditor.clear();
                        sharedPrefEditor.commit();

                        mNameElement.setText("");
                        mDateElement.setText("");
                        mDescriptionElement.setText("");
                        mEndTimeElement.setText("");
                        mStartTimeElement.setText("");

                        Toast.makeText(AddEventPlan.this, "Element wydarzenia został dodany",
                                Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), AddEvent.class));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddEventPlan.this, "Nie udało się dodać elementu wydarzenia",
                                Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onFailure:  " + e.toString());
                    }
                });



            }
        });

        mAddAndAddNextElementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDataFromInput();
                if(TextUtils.isEmpty(eventElement.EventElementName) || TextUtils.isEmpty(eventElement.EventElementDesc) || TextUtils.isEmpty(eventElement.EventElementDate)
                        || TextUtils.isEmpty(eventElement.EventElementStartTime) || TextUtils.isEmpty(eventElement.EventElementEndTime)){

                    Toast.makeText(AddEventPlan.this, "Uzupełnij wszystkie pola",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                database = FirebaseFirestore.getInstance();
                DocumentReference reference = database.collection("eventsPlans").document();

                SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd HH:mm");
                Date date = new Date(System.currentTimeMillis());

                Map<String, Object> plan = new HashMap<>();
                plan.put("name_element", eventElement.EventElementName);
                plan.put("description", eventElement.EventElementDesc);
                plan.put("date_element", eventElement.EventElementDate);
                plan.put("start_time", eventElement.EventElementStartTime);
                plan.put("end_time", eventElement.EventElementEndTime);
                plan.put("addedDate", formatter.format(date));
                reference.set(plan).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        sharedPrefEditor.clear();
                        sharedPrefEditor.commit();

                        mNameElement.setText("");
                        mDateElement.setText("");
                        mDescriptionElement.setText("");
                        mEndTimeElement.setText("");
                        mStartTimeElement.setText("");

                        Toast.makeText(AddEventPlan.this, "Element wydarzenia został dodany",
                                Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), AddEvent.class));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddEventPlan.this, "Nie udało się dodać elementu wydarzenia",
                                Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onFailure:  " + e.toString());
                    }
                });
            }
        });

    }
    private void getDataFromInput() {
        String nameElement = mNameElement.getText().toString().trim();
        String descriptionElement = mDescriptionElement.getText().toString().trim();
        String dateElement = mDateElement.getText().toString().trim();
        String startTime = mStartTimeElement.getText().toString().trim();
        String endTime = mEndTimeElement.getText().toString().trim();

        eventElement.EventElementName = nameElement;
        eventElement.EventElementDesc = descriptionElement;
        eventElement.EventElementDate = dateElement;
        eventElement.EventElementStartTime = startTime;
        eventElement.EventElementEndTime = endTime;
    }
    @Override
    protected void onResume() {
        super.onResume();

        eventElement.EventElementName = sharedPref.getString("nameElement", "");
        eventElement.EventElementDesc = sharedPref.getString("descriptionElement", "");
        eventElement.EventElementDate = sharedPref.getString("dateElement", "");
        eventElement.EventElementStartTime = sharedPref.getString("startTime", "");
        eventElement.EventElementEndTime = sharedPref.getString("endTime", "");

        mNameElement.setText(eventElement.EventElementName);
        mDescriptionElement.setText(eventElement.EventElementDesc);
        mDateElement.setText(eventElement.EventElementDate);
        mStartTimeElement.setText(eventElement.EventElementStartTime);
        mEndTimeElement.setText(eventElement.EventElementEndTime);
        Log.d("SAVE STATE", "onresume ");
    }
    @Override
    protected void onPause(){
        super.onPause();
        getDataFromInput();
        sharedPrefEditor.putString("nameElement", eventElement.EventElementName);
        sharedPrefEditor.putString("descriptionElement", eventElement.EventElementDesc);
        sharedPrefEditor.putString("dateElement", eventElement.EventElementDate);
        sharedPrefEditor.putString("startTime", eventElement.EventElementStartTime);
        sharedPrefEditor.putString("endTime", eventElement.EventElementEndTime);
        sharedPrefEditor.commit();
        Log.d("SAVE STATE", "onpause ");
    }
}