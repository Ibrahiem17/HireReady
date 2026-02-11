package com.example.hireready;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText nameInput, emailInput, passwordInput, confirmPasswordInput;
    private Button primaryActionButton;
    private TextView toggleModeButton;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        primaryActionButton = findViewById(R.id.btnPrimaryAction);
        toggleModeButton = findViewById(R.id.btnToggleMode);

        primaryActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameInput.getText().toString().trim();
                String email = emailInput.getText().toString().trim();
                String password = passwordInput.getText().toString().trim();
                String confirmPassword = confirmPasswordInput.getText().toString().trim();

                if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(SignupActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else if (!password.equals(confirmPassword)) {
                    Toast.makeText(SignupActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                } else {
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        if (user != null) {
                                            String uid = user.getUid();
                                            Intent intent = new Intent(SignupActivity.this, ProfileCreationActivity.class);
                                            intent.putExtra("NAME", name);
                                            intent.putExtra("UID", uid);
                                            intent.putExtra("EMAIL", email);
                                            startActivity(intent);
                                            finish();
                                        }
                                    } else {
                                        String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                                        String errorMessage;
                                        switch (errorCode) {
                                            case "ERROR_INVALID_EMAIL":
                                                errorMessage = "The email address is badly formatted.";
                                                break;
                                            case "ERROR_EMAIL_ALREADY_IN_USE":
                                                errorMessage = "The email address is already in use by another account.";
                                                break;
                                            case "ERROR_WEAK_PASSWORD":
                                                errorMessage = "The password is too weak.";
                                                break;
                                            default:
                                                errorMessage = "Authentication failed.";
                                        }
                                        Toast.makeText(SignupActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                }
            }
        });

        toggleModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
