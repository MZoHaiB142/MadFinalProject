package com.example.madfinalproject.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.madfinalproject.ExploreActivity;
import com.example.madfinalproject.R;
import com.example.madfinalproject.models.TrendingDestination;

import java.util.ArrayList;
import java.util.List;

public final class TrendingDestinationAdapter
        extends RecyclerView.Adapter<TrendingDestinationAdapter.Holder> {
    private final Context context;
    private List<TrendingDestination> items = new ArrayList<>();

    public TrendingDestinationAdapter(Context context) { this.context = context; }

    public void submit(List<TrendingDestination> values) {
        items = values == null ? new ArrayList<>() : new ArrayList<>(values);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context)
                .inflate(R.layout.item_trending_destination, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        TrendingDestination item = items.get(position);
        holder.country.setText(item.getCountry());
        holder.visaRatio.setText(item.getVisaRatio() + "% visa ratio");
        Glide.with(context).load(item.getImageUrl())
                .placeholder(R.drawable.img).error(R.drawable.img).centerCrop().into(holder.image);
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, ExploreActivity.class);
            intent.putExtra("country_filter", item.getCountry());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static final class Holder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView country;
        final TextView visaRatio;

        Holder(View view) {
            super(view);
            image = view.findViewById(R.id.imgCountry);
            country = view.findViewById(R.id.txtCountry);
            visaRatio = view.findViewById(R.id.txtVisaRatio);
        }
    }
}
