package com.example.madfinalproject.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.madfinalproject.R;
import com.example.madfinalproject.models.Country;

import java.util.ArrayList;
import java.util.List;

public class CountryAdapter extends RecyclerView.Adapter<CountryAdapter.CountryViewHolder> {

    private List<Country> countryList = new ArrayList<>();
    private int selectedPosition = -1; // Shuru mein koi country select nahi hogi
    private final OnItemClickListener listener;

    // Click sunne ke liye interface
    public interface OnItemClickListener {
        void onItemClick(Country country);
    }

    // Constructor
    public CountryAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    // 🔥 YEH HAI WO FUNCTION JISKA ERROR AA RAHA THA
    public void submitList(List<Country> list) {
        this.countryList = list;
        notifyDataSetChanged(); // List ko update karta hai
    }

    @NonNull
    @Override
    public CountryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Aapke item_country_card.xml ko link karna
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_country_card, parent, false);
        return new CountryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CountryViewHolder holder, int position) {
        Country country = countryList.get(position);

        // Data set karna
        holder.tvFlag.setText(country.flag);
        holder.tvName.setText(country.name);
        holder.tvInfo.setText(country.time + " months • " + country.tagline);

        // Selection aur Highlight Logic (Jo HTML design mein tha)
        if (selectedPosition == position) {
            holder.tvCheck.setVisibility(View.VISIBLE); // Tick mark show karein
            holder.cardRoot.setBackgroundColor(Color.parseColor("#1A4A6CF7")); // Light Blue background
        } else {
            holder.tvCheck.setVisibility(View.GONE); // Tick mark chupayein
            holder.cardRoot.setBackgroundResource(R.drawable.bg_country_card); // Default background
        }

        // Jab kisi country card par click ho
        holder.cardRoot.setOnClickListener(v -> {
            int previousSelected = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            // Purane aur naye card ka color update karne ke liye
            notifyItemChanged(previousSelected);
            notifyItemChanged(selectedPosition);

            // Activity ko batana ke kaunsi country select hui hai
            listener.onItemClick(country);
        });
    }

    @Override
    public int getItemCount() {
        return countryList.size();
    }

    // Views ko dhoondne wali class
    public static class CountryViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout cardRoot;
        TextView tvCheck, tvFlag, tvName, tvInfo;

        public CountryViewHolder(@NonNull View itemView) {
            super(itemView);
            // Aapke XML IDs ke mutabiq
            cardRoot = itemView.findViewById(R.id.card_root);
            tvCheck = itemView.findViewById(R.id.tv_check);
            tvFlag = itemView.findViewById(R.id.tv_flag);
            tvName = itemView.findViewById(R.id.tv_country_name);
            tvInfo = itemView.findViewById(R.id.tv_country_info);
        }
    }
}