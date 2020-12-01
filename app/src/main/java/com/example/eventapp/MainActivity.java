package com.example.eventapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    TextView nick,email,phone, verifyMsg;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userId;
    Button resendCode, changeProfileBtn, eventPanelBtn, showAllEventsBtn;

    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        phone = findViewById(R.id.profileNumber);
        nick = findViewById(R.id.profilName);
        email = findViewById(R.id.profileEmail);

        changeProfileBtn = findViewById(R.id.changeProfile);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        resendCode = findViewById(R.id.verifyBtn);
        verifyMsg = findViewById(R.id.verificatiomMsg);
        eventPanelBtn = findViewById(R.id.addEventPanel);
        showAllEventsBtn = findViewById(R.id.showAllEventsBtn);

        userId = fAuth.getCurrentUser().getUid();
        FirebaseUser user = fAuth.getCurrentUser();

        DocumentReference documentReference = fStore.collection("users").document(userId);
        documentReference.addSnapshotListener(this, (documentSnapshot, e) -> {
            if (documentSnapshot.exists()) {
                String Uphone = documentSnapshot.getString("phone");
                String Ulogin = documentSnapshot.getString("nick");
                String Uemail = documentSnapshot.getString("email");
                this.user = new User(Ulogin, Uemail, Uphone);
                phone.setText(Uphone);
                nick.setText(Ulogin);
                email.setText(Uemail);
            }
        });


        if(!user.isEmailVerified()){
            resendCode.setVisibility(View.VISIBLE);
            verifyMsg.setVisibility(View.VISIBLE);


            resendCode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(view.getContext(), "Wysłano link weryfikacyjny na podany adres e-mail!", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("tag", "Nie udało się wysłać emaila " + e.getMessage());
                        }
                    });
                }
            });

        }
        //buttons
        changeProfileBtn.setOnClickListener(v ->  {
            Intent i = new Intent(v.getContext(), EditProfile.class);
            i.putExtra("User", this.user);
            startActivity(i);
        });
        eventPanelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPref = getSharedPreferences("event", Context.MODE_PRIVATE);
                SharedPreferences.Editor sharedPrefEditor = sharedPref.edit();
                sharedPrefEditor.clear();
                sharedPrefEditor.commit();
                
                startActivity(new Intent(getApplicationContext(),AddEvent.class));
            }
        });
         showAllEventsBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 startActivity(new Intent(getApplicationContext(),ShowAllEvents.class));
             }
         });
    }

    public void logout(View v){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(),Login.class));
        finish();
    }
}