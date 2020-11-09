package com.example.eventapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfile extends AppCompatActivity {

    public static final String TAG = "TAG";
    public User user;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirebaseUser fUser;

    Button cancelEditProfileBtn, saveProfileBtn;
    EditText profileLogin, profileEmail, profilePhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        cancelEditProfileBtn = findViewById(R.id.cancelEditProfile);

        user = (User) getIntent().getSerializableExtra("User");

        this.firebaseInit();
        this.setInputData();

        saveProfileBtn = findViewById(R.id.saveProfile);

        // listeners
        saveProfileBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                String login = profileLogin.getText().toString();
                String email = profileEmail.getText().toString();
                String phone = profilePhone.getText().toString();

                if (email.isEmpty() || login.isEmpty() || phone.isEmpty()) {
                    Toast.makeText(EditProfile.this, "Uzupełnij wszystkie pola", Toast.LENGTH_SHORT).show();
                    return;
                } else if (phone.length() != 9) {
                    Toast.makeText(EditProfile.this, "Numer telefonu powinien mieć 9 znaków", Toast.LENGTH_SHORT).show();
                    return;
                }

                fUser.updateEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        DocumentReference docRef = fStore.collection("users").document(fUser.getUid());
                        Map<String, Object> edited = new HashMap<>();
                        edited.put("email", email);
                        edited.put("nick", login);
                        edited.put("phone", phone);

                        docRef.update(edited).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(EditProfile.this, "Dane zostały zmienione", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                finish();
                            }
                        });


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditProfile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


        cancelEditProfileBtn.setOnClickListener(v -> {
            startActivity(new Intent(v.getContext(), MainActivity.class));
        });
    }

    private void firebaseInit()
    {
        this.fAuth = FirebaseAuth.getInstance();
        this.fStore = FirebaseFirestore.getInstance();
        this.fUser = fAuth.getCurrentUser();
    }

    private void setInputData()
    {
        profileLogin = findViewById(R.id.profileLogin);
        profileEmail = findViewById(R.id.profileEmail);
        profilePhone = findViewById(R.id.profilePhone);

        profileLogin.setText(user.Login);
        profileEmail.setText(user.Email);
        profilePhone.setText(user.Phone);
    }
}