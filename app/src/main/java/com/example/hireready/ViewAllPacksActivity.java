package com.example.hireready;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ViewAllPacksActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView allPacksRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all_packs);

        db = FirebaseFirestore.getInstance();
        allPacksRecyclerView = findViewById(R.id.all_packs_recycler_view);
        allPacksRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        db.collection("questionPacks").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<QuestionPack> questionPacks = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    questionPacks.add(document.toObject(QuestionPack.class));
                }
                allPacksRecyclerView.setAdapter(new AllPacksAdapter(questionPacks));
            }
        });
    }

    private static class AllPacksAdapter extends RecyclerView.Adapter<AllPacksAdapter.ViewHolder> {

        private final List<QuestionPack> packs;

        public AllPacksAdapter(List<QuestionPack> packs) {
            this.packs = packs;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pack_card_grid, parent, false);
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
}
