package com.example.hireready;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BrowsePacksActivity extends AppCompatActivity {

    private static final String TAG = "BrowsePacksActivity";

    private RecyclerView predefinedPacksRecyclerView, recentRecyclerView;
    private TextView welcomeText, viewAllButton, viewAllPredefinedPacksButton;
    private ImageView settingsButton;
    private TextInputEditText jobDescriptionText;
    private Button jobRoleButton, learnButton;


    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_packs);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        welcomeText = findViewById(R.id.welcome_text);
        settingsButton = findViewById(R.id.settings_button);
        predefinedPacksRecyclerView = findViewById(R.id.predefined_packs_recycler_view);
        recentRecyclerView = findViewById(R.id.recent_recycler_view);
        jobDescriptionText = findViewById(R.id.job_description_text);
        Button askAnythingButton = findViewById(R.id.generate_interview_button);
        jobRoleButton = findViewById(R.id.job_role_button);
        learnButton = findViewById(R.id.learn_button);
        viewAllButton = findViewById(R.id.view_all_button);
        viewAllPredefinedPacksButton = findViewById(R.id.view_all_predefined_packs_button);

        View.OnClickListener openChatbotListener = v -> {
            Intent intent = new Intent(BrowsePacksActivity.this, ChatbotActivity.class);
            startActivity(intent);
        };

        jobDescriptionText.setOnClickListener(openChatbotListener);
        askAnythingButton.setOnClickListener(openChatbotListener);

        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(BrowsePacksActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        jobRoleButton.setOnClickListener(v -> {
            Intent intent = new Intent(BrowsePacksActivity.this, JobRolePacksActivity.class);
            startActivity(intent);
        });

        learnButton.setOnClickListener(v -> {
            Intent intent = new Intent(BrowsePacksActivity.this, LearnActivity.class);
            startActivity(intent);
        });

        viewAllButton.setOnClickListener(v -> {
            Intent intent = new Intent(BrowsePacksActivity.this, ViewAllRecentActivity.class);
            startActivity(intent);
        });

        viewAllPredefinedPacksButton.setOnClickListener(v -> {
            Intent intent = new Intent(BrowsePacksActivity.this, ViewAllPacksActivity.class);
            startActivity(intent);
        });

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            loadUserData(uid);
            fetchRecentSessions(uid);
        } else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }


        predefinedPacksRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        fetchQuestionPacks();
        recentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadUserData(String uid) {
        DocumentReference userRef = db.collection("users").document(uid);
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String name = document.getString("name");
                    welcomeText.setText("Welcome, " + name);
                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }

    private void fetchQuestionPacks() {
        db.collection("questionPacks")
                .limit(5)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<QuestionPack> questionPacks = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            questionPacks.add(document.toObject(QuestionPack.class));
                        }
                        predefinedPacksRecyclerView.setAdapter(new PredefinedPacksAdapter(questionPacks));
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    private void fetchRecentSessions(String uid) {
        db.collection("users")
                .document(uid)
                .collection("userSessions")
                .orderBy("timeStamp", Query.Direction.DESCENDING)
                .limit(3)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<UserSession> recentSessions = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            UserSession session = document.toObject(UserSession.class);
                            session.setSessionId(document.getId());
                            recentSessions.add(session);
                        }

                        recentRecyclerView.setAdapter(
                                new RecentSessionAdapter(recentSessions)
                        );
                    }
                });
    }



    private static class PredefinedPacksAdapter extends RecyclerView.Adapter<PredefinedPacksAdapter.ViewHolder> {
        private final List<QuestionPack> packs;

        public PredefinedPacksAdapter(List<QuestionPack> packs) {
            this.packs = packs;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_interview_pack, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            QuestionPack pack = packs.get(position);
            holder.packTitle.setText(pack.getRole());
            holder.startButton.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), MockInterviewActivity.class);
                intent.putExtra("questions", (Serializable) pack.getQuestions());
                intent.putExtra("packTitle", pack.getRole());
                v.getContext().startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return packs.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView packTitle;
            Button startButton;

            ViewHolder(View view) {
                super(view);
                packTitle = view.findViewById(R.id.pack_title);
                startButton = view.findViewById(R.id.start_button);
            }
        }
    }

    private static class RecentSessionAdapter extends RecyclerView.Adapter<RecentSessionAdapter.ViewHolder> {
        private final List<UserSession> sessions;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

        public RecentSessionAdapter(List<UserSession> sessions) {
            this.sessions = sessions;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent_interview, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            UserSession session = sessions.get(position);

            holder.title.setText(session.getPackTitle());
            holder.tag.setText("Completed");

            if (session.getTimeStamp() != null) {
                holder.timestamp.setText(dateFormat.format(session.getTimeStamp()));
            }

            holder.progressBar.setProgress(session.getScore());
            holder.progressText.setText(session.getScore() + "%");

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), RecentSessionDetailActivity.class);
                intent.putExtra("sessionId", session.getSessionId());
                v.getContext().startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return sessions.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView title, tag, timestamp, progressText;
            ProgressBar progressBar;

            ViewHolder(View view) {
                super(view);
                title = view.findViewById(R.id.interview_title);
                tag = view.findViewById(R.id.interview_tag);
                timestamp = view.findViewById(R.id.interview_timestamp);
                progressBar = view.findViewById(R.id.progress_bar);
                progressText = view.findViewById(R.id.progress_text);
            }
        }
    }
}
