package com.example.madfinalproject.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.madfinalproject.R;
import com.example.madfinalproject.models.SopUniversity;

import java.util.List;

public class SopUniversityAdapter extends RecyclerView.Adapter<SopUniversityAdapter.ViewHolder> {

    private Context context;
    private List<SopUniversity> uniList;
    private int selectedPosition = -1; // Shuru mein koi select nahi hoga

    public SopUniversityAdapter(Context context, List<SopUniversity> uniList) {
        this.context = context;
        this.uniList = uniList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 🔥 Yahan apne XML file ka naam likhein (e.g., R.layout.item_sop_university)
        View view = LayoutInflater.from(context).inflate(R.layout.sop_item_university, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SopUniversity uni = uniList.get(position);

        // 1. Data Set Karna (Aapke IDs ke hisab se)
        holder.tvUniversityName.setText(uni.getName());
        holder.tvLocation.setText(uni.getCity() + ", " + uni.getCountry());

        // 2. Glide se Logo load karwana
        Glide.with(context)
                .load(uni.getLogoUrl())
                .into(holder.ivLogo);

        // 3. Selection aur Radio Button ka Logic
        if (selectedPosition == position) {
            // Agar yeh list item selected hai toh Radio icon ko 'Checked' kar dain
            // Note: Agar aapke paas checked icon hai to usay yahan lagayein,
            // main abhi default android ka check icon laga raha hu
            holder.ivRadio.setImageResource(android.R.drawable.checkbox_on_background);

            // Optional: Selected item ka background thora change karna ho toh
            // holder.itemRoot.setBackgroundResource(R.drawable.bg_university_item_selected);
        } else {
            // Agar selected nahi hai toh apna purana radio_button laga dain
            holder.ivRadio.setImageResource(R.drawable.radio_button);
            holder.itemRoot.setBackgroundResource(R.drawable.bg_university_item);
        }

        // 4. Click Listener (Jab user poore card par click kare)
        holder.itemRoot.setOnClickListener(v -> {
            selectedPosition = holder.getAdapterPosition(); // Nayi position save ki
            notifyDataSetChanged(); // List ko update kiya taake radio button change ho
        });
    }

    @Override
    public int getItemCount() {
        return uniList.size();
    }

    // Yeh function activity mein kaam aayega Next dabane par
    public SopUniversity getSelectedUniversity() {
        if (selectedPosition != -1) {
            return uniList.get(selectedPosition);
        }
        return null;
    }

    // Aapke XML ke mutabiq views ko link karna
    public class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout itemRoot;
        ImageView ivLogo, ivRadio;
        TextView tvUniversityName, tvLocation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Bilkul wahi IDs jo aapne apne XML mein rakhi hain
            itemRoot = itemView.findViewById(R.id.item_root);
            ivLogo = itemView.findViewById(R.id.iv_logo);
            ivRadio = itemView.findViewById(R.id.iv_radio);
            tvUniversityName = itemView.findViewById(R.id.tv_university_name);
            tvLocation = itemView.findViewById(R.id.tv_location);
        }
    }
}