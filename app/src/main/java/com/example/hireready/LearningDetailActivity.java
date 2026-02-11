package com.example.hireready;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.List;
import java.util.Map;

public class LearningDetailActivity extends AppCompatActivity {

    private static final String TAG = "LearningDetailActivity";
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learning_detail);

        db = FirebaseFirestore.getInstance();

        TextView titleText = findViewById(R.id.title_text);
        TextView skillsText = findViewById(R.id.skills_text);
        TextView tipsText = findViewById(R.id.tips_text);
        TextView questionsText = findViewById(R.id.questions_text);

        String documentId = getIntent().getStringExtra("documentId");
        if (documentId == null || documentId.isEmpty()) {
            Toast.makeText(this, "Learning section not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("learning_sections").document(documentId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "Document data: " + documentSnapshot.getData());

                        String title = documentSnapshot.getString("title");
                        titleText.setText(title);

                        List<String> skills = (List<String>) documentSnapshot.get("skills");
                        if (skills != null && !skills.isEmpty()) {
                            StringBuilder sb = new StringBuilder();
                            for (String skill : skills) {
                                sb.append("- ").append(skill).append("\n");
                            }
                            skillsText.setText(sb.toString());
                        } else {
                            skillsText.setText("No skills listed.");
                        }

                        List<String> interviewTips = (List<String>) documentSnapshot.get("interview_tips");
                        if (interviewTips != null && !interviewTips.isEmpty()) {
                            StringBuilder sb = new StringBuilder();
                            for (String tip : interviewTips) {
                                sb.append("- ").append(tip).append("\n");
                            }
                            tipsText.setText(sb.toString());
                        } else {
                            tipsText.setText("No tips available.");
                        }


                        List<String> commonQuestions = (List<String>) documentSnapshot.get("common_questions");
                        Map<String, String> answers = (Map<String, String>) documentSnapshot.get("answers");
                        if (commonQuestions != null && !commonQuestions.isEmpty() && answers != null) {
                            StringBuilder questionsAndAnswers = new StringBuilder();
                            for (String question : commonQuestions) {
                                questionsAndAnswers.append("Q: ").append(question).append("\n");
                                String answer = answers.get(question);
                                questionsAndAnswers.append("A: ").append(answer != null ? answer : "No answer provided.").append("\n\n");
                            }
                            questionsText.setText(questionsAndAnswers.toString());
                        } else {
                            questionsText.setText("No questions and answers available.");
                        }

                    } else {
                        Log.d(TAG, "No such document");
                        Toast.makeText(LearningDetailActivity.this, "Learning section not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching document", e);
                    Toast.makeText(LearningDetailActivity.this, "Error fetching data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
