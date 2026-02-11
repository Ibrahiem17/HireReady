package com.example.hireready;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button logoutButton, reminderButton;
    private TimePicker timePicker;
    private FirebaseAuth mAuth;
    private CircleImageView profileImage;
    private ActivityResultLauncher<String> imagePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();

        logoutButton = findViewById(R.id.logout_button);
        reminderButton = findViewById(R.id.reminder_button);
        timePicker = findViewById(R.id.timepicker);
        profileImage = findViewById(R.id.profile_image);

        loadProfileImage();

        imagePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        profileImage.setImageURI(uri);
                        uploadImage(uri);
                    }
                }
        );

        profileImage.setOnClickListener(v -> imagePicker.launch("image/*"));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        101
                );
            }
        }

        reminderButton.setOnClickListener(v -> setReminder());

        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    // 🔹 LOAD PROFILE IMAGE
    private void loadProfileImage() {
        if (mAuth.getCurrentUser() == null) return;

        String userId = mAuth.getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("users") // ✅ CORRECT COLLECTION
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String url = doc.getString("profileImageUrl");
                        if (url != null) {
                            Glide.with(this)
                                    .load(url)
                                    .placeholder(R.drawable.default_avatar)
                                    .into(profileImage);
                        }
                    }
                });
    }

    // 🔹 UPLOAD IMAGE TO FIREBASE STORAGE
    private void uploadImage(Uri imageUri) {

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_LONG).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        StorageReference ref = FirebaseStorage.getInstance()
                .getReference()
                .child("profile_pictures")
                .child(userId)
                .child("profile.jpg"); // ✅ MATCHES RULES

        ref.putFile(imageUri)
                .addOnSuccessListener(task ->
                        ref.getDownloadUrl().addOnSuccessListener(uri ->
                                saveImageUrl(uri.toString())
                        ).addOnFailureListener(e -> {
                            Log.e("DOWNLOAD_URL_ERROR", "Failed to get download URL", e);
                            Toast.makeText(SettingsActivity.this,
                                    "Failed to get download URL: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        })
                )
                .addOnFailureListener(e -> {
                    Log.e("UPLOAD_ERROR", "Upload failed", e);
                    Toast.makeText(this,
                            "Upload failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    // 🔹 SAVE IMAGE URL TO FIRESTORE
    private void saveImageUrl(String url) {

        String userId = mAuth.getCurrentUser().getUid();

        Map<String, Object> data = new HashMap<>();
        data.put("profileImageUrl", url);

        FirebaseFirestore.getInstance()
                .collection("users") // ✅ CORRECT COLLECTION
                .document(userId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e -> {
                    Log.e("FIRESTORE_ERROR", "Failed to save URL", e);
                    Toast.makeText(this,
                            "Failed to save image URL: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    // 🔹 REMINDER (UNCHANGED)
    @SuppressLint("ScheduleExactAlarm")
    private void setReminder() {

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM));
                return;
            }
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
        calendar.set(Calendar.MINUTE, timePicker.getMinute());
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        Intent intent = new Intent(this, MyService.class);

        PendingIntent pendingIntent = PendingIntent.getService(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        }

        Toast.makeText(this, "Reminder set successfully", Toast.LENGTH_SHORT).show();
    }
}
