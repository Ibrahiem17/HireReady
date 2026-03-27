package com.example.hireready;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class RecentSessionDetailActivity extends AppCompatActivity {

    private TextView packTitleText, scoreText, answersText, feedbackText;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_session_detail);

        packTitleText = findViewById(R.id.pack_title_text);
        scoreText = findViewById(R.id.score_text);
        answersText = findViewById(R.id.answers_text);
        feedbackText = findViewById(R.id.feedback_text);

        db = FirebaseFirestore.getInstance();

        String sessionId = getIntent().getStringExtra("sessionId");
        if (sessionId == null) {
            finish();
            return;
        }

        loadSession(sessionId);
    }

    private void loadSession(String sessionId) {

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        db.collection("users")
                .document(uid)
                .collection("userSessions")
                .document(sessionId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) return;


                    packTitleText.setText(doc.getString("packTitle"));


                    Long score = doc.getLong("score");
                    scoreText.setText(score != null ? score + "%" : "N/A");


                    List<String> answers = (List<String>) doc.get("answers");
                    if (answers != null && !answers.isEmpty()) {
                        StringBuilder ansBuilder = new StringBuilder();
                        for (int i = 0; i < answers.size(); i++) {
                            ansBuilder.append("Q").append(i + 1)
                                    .append(": ")
                                    .append(answers.get(i))
                                    .append("\n\n");
                        }
                        answersText.setText(ansBuilder.toString());
                    } else {
                        answersText.setText("No answers available.");
                    }


                    Map<String, Object> aiFeedback =
                            (Map<String, Object>) doc.get("aiFeedback");

                    if (aiFeedback != null) {
                        StringBuilder fb = new StringBuilder();

                        appendSection(fb, "Strengths",
                                (List<String>) aiFeedback.get("strengths"));

                        appendSection(fb, "Weaknesses",
                                (List<String>) aiFeedback.get("weaknesses"));

                        appendSection(fb, "Improvements",
                                (List<String>) aiFeedback.get("improvements"));

                        feedbackText.setText(fb.toString());
                    } else {
                        feedbackText.setText("Feedback not available.");
                    }
                });
    }

    private void appendSection(StringBuilder builder, String title, List<String> items) {
        if (items == null || items.isEmpty()) return;

        builder.append(title).append(":\n");
        for (String item : items) {
            builder.append("• ").append(item).append("\n");
        }
        builder.append("\n");
    }
}
