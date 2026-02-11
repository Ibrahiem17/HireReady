package com.example.hireready;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class FeedbackActivity extends AppCompatActivity {

    private TextView scoreText, aiFeedbackText, userAnswersText;
    private Button doneButton;

    private FirebaseFirestore db;
    private String sessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        scoreText = findViewById(R.id.score_text);
        aiFeedbackText = findViewById(R.id.ai_feedback_text);
        userAnswersText = findViewById(R.id.user_answers_text);
        doneButton = findViewById(R.id.done_button);

        db = FirebaseFirestore.getInstance();

        sessionId = getIntent().getStringExtra("sessionId");

        if (sessionId == null) {
            Toast.makeText(this, "Session not found.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        loadFeedback();

        doneButton.setOnClickListener(v -> navigateToBrowsePacks());
    }

    private void loadFeedback() {
        String uid = FirebaseAuth.getInstance().getUid();

        if (uid == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_LONG).show();
            return;
        }

        db.collection("users")
                .document(uid)
                .collection("userSessions")
                .document(sessionId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        aiFeedbackText.setText("Feedback not available.");
                        return;
                    }

                    Long score = doc.getLong("score");
                    scoreText.setText(score != null ? score + "/100" : "N/A");

                    List<String> questions = (List<String>) doc.get("questions");
                    List<String> answers = (List<String>) doc.get("answers");

                    if (questions != null && answers != null && questions.size() == answers.size()) {
                        StringBuilder userAnswersBuilder = new StringBuilder();
                        for (int i = 0; i < questions.size(); i++) {
                            userAnswersBuilder.append("Q: ").append(questions.get(i)).append("\n");
                            userAnswersBuilder.append("A: ").append(answers.get(i)).append("\n\n");
                        }
                        userAnswersText.setText(userAnswersBuilder.toString());
                    } else {
                        userAnswersText.setText("Your answers are not available.");
                    }

                    Map<String, Object> aiFeedback =
                            (Map<String, Object>) doc.get("aiFeedback");

                    if (aiFeedback == null) {
                        aiFeedbackText.setText("AI feedback not available.");
                        return;
                    }

                    StringBuilder feedbackBuilder = new StringBuilder();

                    appendSection(
                            feedbackBuilder,
                            "Strengths",
                            (List<String>) aiFeedback.get("strengths")
                    );

                    appendSection(
                            feedbackBuilder,
                            "Weaknesses",
                            (List<String>) aiFeedback.get("weaknesses")
                    );

                    appendSection(
                            feedbackBuilder,
                            "Improvements",
                            (List<String>) aiFeedback.get("improvements")
                    );

                    aiFeedbackText.setText(feedbackBuilder.toString());
                })
                .addOnFailureListener(e ->
                        aiFeedbackText.setText("Error loading feedback: " + e.getMessage())
                );
    }

    private void appendSection(
            StringBuilder builder,
            String title,
            List<String> items
    ) {
        if (items == null || items.isEmpty()) return;

        builder.append(title).append(":\n");
        for (String item : items) {
            builder.append("• ").append(item).append("\n");
        }
        builder.append("\n");
    }
    private void navigateToBrowsePacks() {
        Intent intent = new Intent(FeedbackActivity.this, BrowsePacksActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
