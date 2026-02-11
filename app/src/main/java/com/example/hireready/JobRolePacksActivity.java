package com.example.hireready;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class JobRolePacksActivity extends AppCompatActivity {

    private RecyclerView jobListingsRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_role_packs);

        jobListingsRecyclerView = findViewById(R.id.job_listings_recycler_view);
        jobListingsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<JobListing> jobListings = new ArrayList<>();
        jobListings.add(new JobListing("Software Engineer", "Tech Solutions Inc.", "San Francisco, CA"));
        jobListings.add(new JobListing("Product Manager", "Innovate Ltd.", "New York, NY"));
        jobListings.add(new JobListing("UX/UI Designer", "Creative Designs", "Austin, TX"));
        jobListings.add(new JobListing("Data Scientist", "Analytics Pro", "Boston, MA"));
        jobListings.add(new JobListing("DevOps Engineer", "Cloud Services Co.", "Remote"));

        JobListingsAdapter adapter = new JobListingsAdapter(jobListings);
        jobListingsRecyclerView.setAdapter(adapter);
    }

    private static class JobListingsAdapter extends RecyclerView.Adapter<JobListingsAdapter.ViewHolder> {

        private final List<JobListing> jobListings;

        public JobListingsAdapter(List<JobListing> jobListings) {
            this.jobListings = jobListings;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_job_listing, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            JobListing job = jobListings.get(position);
            holder.jobTitle.setText(job.getTitle());
            holder.jobCompany.setText(job.getCompany());
            holder.jobLocation.setText(job.getLocation());

            holder.applyButton.setOnClickListener(v -> {
                Toast.makeText(v.getContext(), "Applied to " + job.getTitle(), Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() {
            return jobListings.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView jobTitle, jobCompany, jobLocation;
            Button applyButton;

            ViewHolder(View view) {
                super(view);
                jobTitle = view.findViewById(R.id.job_title_text);
                jobCompany = view.findViewById(R.id.job_company_text);
                jobLocation = view.findViewById(R.id.job_location_text);
                applyButton = view.findViewById(R.id.apply_button);
            }
        }
    }
}
