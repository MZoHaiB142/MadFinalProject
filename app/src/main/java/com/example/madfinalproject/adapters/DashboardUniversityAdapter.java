package com.example.madfinalproject.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.madfinalproject.R;
import com.example.madfinalproject.models.University;
import com.example.madfinalproject.ui.UniversityBottomSheets;
import java.util.ArrayList;
import java.util.List;

public final class DashboardUniversityAdapter extends RecyclerView.Adapter<DashboardUniversityAdapter.Holder>{private final Context context;private List<University>items=new ArrayList<>();public DashboardUniversityAdapter(Context context){this.context=context;}public void submit(List<University>values){items=new ArrayList<>(values);notifyDataSetChanged();}@NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent,int type){return new Holder(LayoutInflater.from(context).inflate(R.layout.item_dashboard_recommended_university,parent,false));}@Override public void onBindViewHolder(@NonNull Holder h,int position){University u=items.get(position);h.name.setText(u.name==null?"University":u.name);h.location.setText(u.location==null?"":u.location);h.score.setText(u.matchScore+"% Match");Glide.with(context).load(u.imageUrl).placeholder(R.drawable.img).error(R.drawable.img).centerCrop().into(h.image);h.itemView.setOnClickListener(v->UniversityBottomSheets.openDetails(context,u));}@Override public int getItemCount(){return items.size();}static final class Holder extends RecyclerView.ViewHolder{ImageView image;TextView name,location,score;Holder(View v){super(v);image=v.findViewById(R.id.recommendedImage);name=v.findViewById(R.id.recommendedName);location=v.findViewById(R.id.recommendedLocation);score=v.findViewById(R.id.recommendedScore);}}}
