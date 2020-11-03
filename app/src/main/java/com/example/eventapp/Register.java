package com.example.eventapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Register extends AppCompatActivity {
    EditText mNick, mEmail, mPassword, mConfirmPass, mPhone;
    Button mRegisterBtn;
    TextView mLoginBtnText;
    FirebaseAuth fAuth;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mNick = findViewById(R.id.nick);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mConfirmPass = findViewById(R.id.confirm_pass);
        mPhone = findViewById(R.id.telefon);
        mRegisterBtn = findViewById(R.id.register);
        mLoginBtnText = findViewById(R.id.zaloguj_sie_teraz);

        fAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar2);

        if(fAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                String confirm_password = mConfirmPass.getText().toString().trim();
                //mini walidacja
                if(TextUtils.isEmpty(email)){
                    mEmail.setError("Email jest wymagany");
                    return;
                }

                if(TextUtils.isEmpty(password)){
                    mPassword.setError("Uzupełnij hasło");
                    return;
                }

                if(!(password.equals(confirm_password))){
                    mConfirmPass.setError("Hasła muszą byc identyczne");
                    return;
                }

                if(password.length() < 6){
                    mPassword.setError("Hasło musi zawierać więcej niż 6 znaków");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                //zarejestrowanie nowego użytkownika do firebase

                fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(Register.this,"Utworzono nowe konto", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        }else{
                            Toast.makeText(Register.this, "Error!" + task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
        mLoginBtnText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),Login.class));
            }
        });
    }
}