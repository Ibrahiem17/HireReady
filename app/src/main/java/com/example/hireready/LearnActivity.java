package com.example.hireready;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class LearnActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView learningSectionsRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn);

        db = FirebaseFirestore.getInstance();
        learningSectionsRecyclerView = findViewById(R.id.learning_sections_recycler_view);
        learningSectionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        db.collection("learning_sections").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<LearningSection> learningSections = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    LearningSection section = document.toObject(LearningSection.class);
                    section.setDocumentId(document.getId());
                    learningSections.add(section);
                }
                learningSectionsRecyclerView.setAdapter(new LearningSectionsAdapter(learningSections));
            }
        });
    }

    private static class LearningSectionsAdapter extends RecyclerView.Adapter<LearningSectionsAdapter.ViewHolder> {

        private final List<LearningSection> learningSections;

        public LearningSectionsAdapter(List<LearningSection> learningSections) {
            this.learningSections = learningSections;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_learning_section, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            LearningSection section = learningSections.get(position);
            holder.title.setText(section.getTitle());

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), LearningDetailActivity.class);
                intent.putExtra("documentId", section.getDocumentId());
                v.getContext().startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return learningSections.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView title;

            ViewHolder(View view) {
                super(view);
                title = view.findViewById(R.id.learning_section_title);
            }
        }
    }
}
