package com.example.madfinalproject.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.madfinalproject.R;
import com.example.madfinalproject.models.University;
import com.example.madfinalproject.models.UniversityProfileMatch;
import com.example.madfinalproject.ui.UniversityBottomSheets;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExploreAdapter extends RecyclerView.Adapter<ExploreAdapter.UniViewHolder> {

    private Context context;
    private List<University> uniList;
    private Map<String, UniversityProfileMatch> profileMatches = new HashMap<>();

    public ExploreAdapter(Context context, List<University> uniList) {
        this.context = context;
        this.uniList = uniList;
    }

    @NonNull
    @Override
    public UniViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // XML layout name ko yahan confirm karlein
        View view = LayoutInflater.from(context).inflate(R.layout.item_university_explore, parent, false);
        return new UniViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UniViewHolder holder, int position) {
        University uni = uniList.get(position);

        // Basic University Details
        holder.tvName.setText(uni.name);
        holder.tvLocation.setText(uni.location);
        holder.tvRanking.setText(uni.ranking);
        holder.tvAcceptance.setText(uni.acceptanceRate);
        holder.tvFees.setText(uni.fees);

        // --- Nayi Fields (Visa, Scholarship, Match Score) ---

        // Visa Ratio Setting
        if (uni.getVisaRatio() != null) {
            holder.tvVisaRatio.setText("Visa: " + uni.getVisaRatio());
        }

        // Scholarship Count Setting
        int scholarshipCount = uni.getScholarshipCount() > 0
                ? uni.getScholarshipCount() : uni.getScholarships().size();
        holder.tvScholarships.setText(scholarshipCount + " Scholarships");

        UniversityProfileMatch match = profileMatches.get(matchKey(uni));
        if (match == null) {
            holder.tvMatchScore.setText("AI Profile Match: Calculating...");
            holder.tvMatchScore.setOnClickListener(v ->
                    Toast.makeText(context,
                            "Your profile match is still being prepared.",
                            Toast.LENGTH_SHORT).show());
        } else {
            holder.tvMatchScore.setText(
                    "AI Profile Match: " + match.getScore() + "% • "
                            + match.getSuitabilityLabel()
                            + "\nTap for strengths and weak points"
            );
            holder.tvMatchScore.setOnClickListener(v ->
                    UniversityBottomSheets.openProfileMatch(context, uni, match));
        }

        // Image Loading with Glide
        if (uni.imageUrl != null && !uni.imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(uni.imageUrl)
                    .placeholder(R.drawable.img)
                    .into(holder.ivImage);
        }

        // Button Click
        holder.tvScholarships.setOnClickListener(v ->
                UniversityBottomSheets.openScholarships(context, uni));
        holder.btnDetails.setOnClickListener(v ->
                UniversityBottomSheets.openDetails(context, uni));
    }

    @Override
    public int getItemCount() {
        return uniList.size();
    }

    // 🔥 FIXED: Ye method aapke ExploreActivity ke error ko khatam kare ga
    public void updateList(List<University> newList) {
        this.uniList = newList;
        notifyDataSetChanged();
    }

    public void updateProfileMatches(Map<String, UniversityProfileMatch> matches) {
        profileMatches = matches == null
                ? new HashMap<>()
                : new HashMap<>(matches);
        notifyDataSetChanged();
    }

    private String matchKey(University university) {
        if (university == null) return "";
        if (university.id != null && !university.id.trim().isEmpty()) {
            return university.id;
        }
        String name = university.name == null ? "" : university.name;
        String location = university.location == null ? "" : university.location;
        return (name + "|" + location)
                .toLowerCase()
                .replaceAll("[^a-z0-9,;/| ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    public static class UniViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvLocation, tvRanking, tvAcceptance, tvFees;
        TextView tvVisaRatio, tvScholarships, tvMatchScore;
        View btnDetails;

        public UniViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivUniImage);
            tvName = itemView.findViewById(R.id.tvUniName);
            tvLocation = itemView.findViewById(R.id.tvUniLocation);
            tvRanking = itemView.findViewById(R.id.tvRanking);
            tvAcceptance = itemView.findViewById(R.id.tvAcceptance);
            tvFees = itemView.findViewById(R.id.tvFees);

            // Nayi IDs initialize kar di hain
            tvVisaRatio = itemView.findViewById(R.id.tvVisaRatio);
            tvScholarships = itemView.findViewById(R.id.tvScholarshipsCount);
            tvMatchScore = itemView.findViewById(R.id.tvMatchScore);

            btnDetails = itemView.findViewById(R.id.btnViewDetails);
        }
    }
}
