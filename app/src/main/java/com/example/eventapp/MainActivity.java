package com.example.eventapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    final int callbackId = 42;

    TextView nick,email,phone, verifyMsg, headerName;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userId;
    Button resendCode, changeProfileBtn, eventPanelBtn, showAllEventsBtn;
    Toolbar toolbar;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission(callbackId, Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR);
        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.nav_view);
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

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

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
                headerName = findViewById(R.id.headerName);
                headerName.setText(Ulogin);
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.ic_home:
                startActivity(new Intent(getApplicationContext(), ShowAllEvents.class));
                break;
            case R.id.ic_person:
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                break;
            case R.id.ic_add:
                startActivity(new Intent(getApplicationContext(), AddEvent.class));
                break;
            case R.id.ic_settings:
                startActivity(new Intent(getApplicationContext(), EditProfile.class));
                break;
            case R.id.ic_favourite:
                startActivity(new Intent(getApplicationContext(), FavouritesEvents.class));
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else{
            super.onBackPressed();
        }
        super.onBackPressed();
    }

    public void logout(View v){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(),Login.class));
        finish();
    }

    private void checkPermission(int callbackId, String... permissionsId) {
        boolean permissions = true;
        for (String p : permissionsId) {
            permissions = permissions && ContextCompat.checkSelfPermission(this, p) == PERMISSION_GRANTED;
        }

        if (!permissions)
            ActivityCompat.requestPermissions(this, permissionsId, callbackId);
    }
}