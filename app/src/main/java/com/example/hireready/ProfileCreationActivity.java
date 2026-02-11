package com.example.hireready;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileCreationActivity extends AppCompatActivity {

    private static final String TAG = "ProfileCreationActivity";

    private ChipGroup marketChipGroup;
    private AutoCompleteTextView rolesAutoComplete;
    private RadioGroup experienceRadioGroup;
    private Button saveProfileButton;
    private TextInputLayout otherRoleLayout;
    private TextInputEditText otherRoleEditText;

    private FirebaseFirestore db;

    private String name, uid, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_creation);

        db = FirebaseFirestore.getInstance();

        // Get the name, UID, and email from the intent
        name = getIntent().getStringExtra("NAME");
        uid = getIntent().getStringExtra("UID");
        email = getIntent().getStringExtra("EMAIL");

        marketChipGroup = findViewById(R.id.cgMarket);
        rolesAutoComplete = findViewById(R.id.atvRoles);
        experienceRadioGroup = findViewById(R.id.rgExperience);
        saveProfileButton = findViewById(R.id.btnSaveProfile);
        otherRoleLayout = findViewById(R.id.tilOtherRole);
        otherRoleEditText = findViewById(R.id.etOtherRole);

        // Populate Job Role Dropdown
        ArrayAdapter<CharSequence> jobRoleAdapter = ArrayAdapter.createFromResource(this,
                R.array.job_roles, R.layout.dropdown_item);
        rolesAutoComplete.setAdapter(jobRoleAdapter);

        rolesAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedRole = (String) parent.getItemAtPosition(position);
                if (selectedRole.equals("Other")) {
                    otherRoleLayout.setVisibility(View.VISIBLE);
                } else {
                    otherRoleLayout.setVisibility(View.GONE);
                }
            }
        });

        saveProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1. Validate all fields first
                int selectedMarketId = marketChipGroup.getCheckedChipId();
                String selectedRole = rolesAutoComplete.getText().toString();
                int selectedExperienceId = experienceRadioGroup.getCheckedRadioButtonId();

                if (selectedMarketId == -1 || selectedRole.isEmpty() || selectedExperienceId == -1) {
                    Toast.makeText(ProfileCreationActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (uid == null) {
                    Toast.makeText(ProfileCreationActivity.this, "Error: User ID is missing. Cannot save profile.", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "UID is null. Profile cannot be saved.");
                    return;
                }

                String finalRole = selectedRole;
                if (selectedRole.equals("Other")) {
                    finalRole = otherRoleEditText.getText().toString();
                    if (finalRole.isEmpty()) {
                        Toast.makeText(ProfileCreationActivity.this, "Please specify your job role", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                // 2. Prepare the data and start the background save
                Chip selectedMarketChip = findViewById(selectedMarketId);
                RadioButton selectedExperienceButton = findViewById(selectedExperienceId);

                Map<String, Object> user = new HashMap<>();
                user.put("name", name);
                user.put("email", email);
                user.put("targetMarket", selectedMarketChip.getText().toString());
                user.put("role", finalRole);
                user.put("experienceLevel", selectedExperienceButton.getText().toString());

                db.collection("users").document(uid)
                        .set(user)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Profile data saved successfully in the background."))
                        .addOnFailureListener(e -> Log.w(TAG, "Error saving profile data in the background.", e));

                Log.d(TAG, "Validation passed. Navigating to main screen immediately.");
                Intent intent = new Intent(ProfileCreationActivity.this, BrowsePacksActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}
