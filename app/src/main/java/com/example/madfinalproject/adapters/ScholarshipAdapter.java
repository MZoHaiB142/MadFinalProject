package com.example.madfinalproject.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // Glide Library Import
import com.example.madfinalproject.R;
import com.example.madfinalproject.models.ScholarshipModel;
import com.example.madfinalproject.scholarships.FavoriteScholarshipRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScholarshipAdapter extends RecyclerView.Adapter<ScholarshipAdapter.ViewHolder> {

    private Context context;
    private List<ScholarshipModel> list;
    private final Set<String> favouriteIds = new HashSet<>();
    private final FavoriteScholarshipRepository favourites = new FavoriteScholarshipRepository();

    public ScholarshipAdapter(Context context, List<ScholarshipModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Card Design Inflate kar rahe hain
        View view = LayoutInflater.from(context).inflate(R.layout.item_scholarship, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScholarshipModel model = list.get(position);
        boolean isFavourite = favouriteIds.contains(model.getId());
        holder.btnFavorite.setText(isFavourite ? "♥" : "♡");
        holder.btnFavorite.setOnClickListener(v -> favourites.toggle(context, model,
                favouriteIds.contains(model.getId()), (favourite, message) -> {
                    if (favourite) favouriteIds.add(model.getId()); else favouriteIds.remove(model.getId());
                    notifyItemChanged(holder.getBindingAdapterPosition());
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                }));

        // 1. Text Data Set Karna
        holder.tvUniversity.setText(model.getUniversity());
        holder.tvCountryName.setText(model.getCountry());
        holder.tvScholarshipName.setText(model.getTitle());

        // Dates (Prefix laga kar)
        holder.tvStartDate.setText("Starts: " + model.getStart_date());
        holder.tvDeadline.setText("Ends: " + model.getDeadline());

        // Funding Badge (Yellow Box)
        // Agar data null ho to default "Scholarship" show karega
        if (model.getAmount() != null && !model.getAmount().isEmpty()) {
            holder.tvAmountBadge.setText(model.getAmount());
        } else {
            holder.tvAmountBadge.setText("See Details");
        }

        // 2. Image Loading (GLIDE KA JAADU 🪄)
        // Ye URL se image utha kar imgUniBackground mein lagayega
        Glide.with(context)
                .load(model.getImage_url()) // Model se URL liya
                .placeholder(R.drawable.img) // Jab tak load na ho ye dikhao
                .error(R.drawable.img) // Agar error aaye to ye dikhao
                .centerCrop()
                .into(holder.imgUniBackground);

        // 3. Button Click Listeners

        // APPLY NOW BUTTON (Opens Browser)
        holder.btnApplyHere.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(model.getLink()));
                context.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(context, "Invalid Link", Toast.LENGTH_SHORT).show();
            }
        });

        // VIEW DETAILS BUTTON
        // (Filhal ye bhi link khol raha hai, baad mein aap isay DetailActivity par le ja sakte hain)
        holder.btnViewDetails.setOnClickListener(v -> {
            Toast.makeText(context, "Opening Details for " + model.getUniversity(), Toast.LENGTH_SHORT).show();
            // Agar Detail Activity banayi ho to yahan Intent lagega
            // Intent i = new Intent(context, DetailActivity.class);
            // context.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
    public void setFavouriteIds(Set<String> ids) { favouriteIds.clear(); if (ids != null) favouriteIds.addAll(ids); notifyDataSetChanged(); }

    // ViewHolder Class (IDs ko connect karta hai)
    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvUniversity, tvCountryName, tvScholarshipName, tvStartDate, tvDeadline, tvAmountBadge;
        ImageView imgUniBackground, imgFlag;
        Button btnViewDetails, btnApplyHere;
        TextView btnFavorite;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // IDs wahi honi chahiyen jo XML mein hain
            tvUniversity = itemView.findViewById(R.id.tvUniversity);
            tvCountryName = itemView.findViewById(R.id.tvCountryName);
            tvScholarshipName = itemView.findViewById(R.id.tvScholarshipName);
            tvStartDate = itemView.findViewById(R.id.tvStartDate);
            tvDeadline = itemView.findViewById(R.id.tvDeadline);
            tvAmountBadge = itemView.findViewById(R.id.tvAmountBadge); // ✅ New Badge ID

            imgUniBackground = itemView.findViewById(R.id.imgUniBackground);
            imgFlag = itemView.findViewById(R.id.imgFlag);

            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnApplyHere = itemView.findViewById(R.id.btnApplyHere);
            btnFavorite = itemView.findViewById(R.id.btnFavoriteScholarship);
        }
    }
}
