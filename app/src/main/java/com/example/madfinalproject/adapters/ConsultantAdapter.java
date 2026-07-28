package com.example.madfinalproject.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton; // 🔥 Naya import add ho gaya
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.madfinalproject.R;
import com.example.madfinalproject.models.Consultant;

import java.util.List;

public class ConsultantAdapter
        extends RecyclerView.Adapter<ConsultantAdapter.ConsultantViewHolder> {

    // Interfaces — callback ke liye
    public interface OnChatClickListener { void onChat(Consultant consultant); }
    public interface OnCallClickListener { void onCall(Consultant consultant); }

    private final Context             context;
    private final List<Consultant>    consultantList;
    private final OnChatClickListener chatListener;
    private final OnCallClickListener callListener;

    public ConsultantAdapter(
            Context             context,
            List<Consultant>    consultantList,
            OnChatClickListener chatListener,
            OnCallClickListener callListener
    ) {
        this.context        = context;
        this.consultantList = consultantList;
        this.chatListener   = chatListener;
        this.callListener   = callListener;
    }

    @NonNull
    @Override
    public ConsultantViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_consultant, parent, false);
        return new ConsultantViewHolder(v);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ConsultantViewHolder holder, int position) {

        Consultant c = consultantList.get(position);

        holder.tvConsultantName.setText(c.getName());
        holder.tvExpertise.setText(c.getExpertise());
        holder.tvRating.setText(String.valueOf(c.getRating()));

        // ── Profile Photo ──
        if (c.getPhotoUrl() != null && !c.getPhotoUrl().isEmpty()) {
            Glide.with(context)
                    .load(c.getPhotoUrl())
                    .circleCrop()
                    .placeholder(R.drawable.ic_university)
                    .into(holder.ivConsultantProfile);
        }

        // ── Online Status Dot ──
        int statusColor = c.isOnline()
                ? Color.parseColor("#4CAF50")   // Green
                : Color.parseColor("#9E9E9E");  // Gray
        holder.viewStatus.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(statusColor));

        // ── Call button disable if offline ──
        holder.btnCall.setAlpha(c.isOnline() ? 1.0f : 0.5f);

        // ── Buttons ──
        holder.btnChat.setOnClickListener(v -> {
            if (chatListener != null) chatListener.onChat(c);
        });

        holder.btnCall.setOnClickListener(v -> {
            if (callListener != null) callListener.onCall(c);
        });
    }

    @Override
    public int getItemCount() { return consultantList.size(); }

    // ── ViewHolder ──
    public static class ConsultantViewHolder extends RecyclerView.ViewHolder {
        ImageView ivConsultantProfile;
        TextView  tvConsultantName, tvExpertise, tvRating;
        View      viewStatus;

        // 🔥 Yahan Button ki jagah ImageButton aa gaya hai
        ImageButton btnChat, btnCall;

        public ConsultantViewHolder(@NonNull View v) {
            super(v);
            ivConsultantProfile = v.findViewById(R.id.ivConsultantProfile);
            tvConsultantName    = v.findViewById(R.id.tvConsultantName);
            tvExpertise         = v.findViewById(R.id.tvExpertise);
            tvRating            = v.findViewById(R.id.tvRating);
            viewStatus          = v.findViewById(R.id.viewStatus);
            btnChat             = v.findViewById(R.id.btnChat);
            btnCall             = v.findViewById(R.id.btnCall);
        }
    }
}