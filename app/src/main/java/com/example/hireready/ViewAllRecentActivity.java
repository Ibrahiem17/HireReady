package com.example.hireready;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ViewAllRecentActivity extends AppCompatActivity {

    private static final String TAG = "ViewAllRecentActivity";

    private RecyclerView allRecentRecyclerView;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all_recent);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        allRecentRecyclerView = findViewById(R.id.all_recent_recycler_view);
        allRecentRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            fetchAllRecentSessions(uid);
        }
    }

    private void fetchAllRecentSessions(String uid) {
        db.collection("users").document(uid).collection("userSessions")
                .orderBy("timeStamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<UserSession> recentSessions = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            UserSession session = document.toObject(UserSession.class);
                            session.setSessionId(document.getId());
                            recentSessions.add(session);
                        }
                        allRecentRecyclerView.setAdapter(new RecentSessionAdapter(recentSessions));
                    } else {
                        Log.d(TAG, "Error getting recent sessions: ", task.getException());
                    }
                });
    }

    private class RecentSessionAdapter extends RecyclerView.Adapter<RecentSessionAdapter.ViewHolder> {
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
                intent.putExtra("sessionId", session.getSessionId()); // 🔥 ONLY THIS
                v.getContext().startActivity(intent);
            });

            holder.itemView.setOnLongClickListener(v -> {
                new AlertDialog.Builder(v.getContext())
                        .setTitle("Delete Record")
                        .setMessage("Are you sure you want to delete this record?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            deleteSession(session, position);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
            });
        }

        private void deleteSession(UserSession session, int position) {
            String sessionId = session.getSessionId();
            if (sessionId == null) {
                Log.e(TAG, "Session ID is null, cannot delete.");
                Toast.makeText(getApplicationContext(), "Error: Session ID not found.", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                String uid = currentUser.getUid();
                db.collection("users").document(uid).collection("userSessions").document(sessionId)
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "DocumentSnapshot successfully deleted!");
                            sessions.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, sessions.size());
                            Toast.makeText(getApplicationContext(), "Session deleted.", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Log.w(TAG, "Error deleting document", e);
                            Toast.makeText(getApplicationContext(), "Failed to delete session.", Toast.LENGTH_SHORT).show();
                        });
            }
        }


        @Override
        public int getItemCount() {
            return sessions.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
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
