package com.example.madfinalproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.madfinalproject.R;
import com.example.madfinalproject.CommunityPost;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyPostsAdapter extends RecyclerView.Adapter<MyPostsAdapter.PostViewHolder> {

    private List<CommunityPost> postList;
    private OnPostActionListener listener;

    public interface OnPostActionListener {
        void onEdit(CommunityPost post);
        void onDelete(CommunityPost post);
    }

    public MyPostsAdapter(List<CommunityPost> postList, OnPostActionListener listener) {
        this.postList = postList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        CommunityPost post = postList.get(position);

        // 🔥 FIX: 'content' ki jagah 'body' use karein
        holder.tvContent.setText(post.body);

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
            holder.tvDate.setText(sdf.format(new Date(post.timestamp)));
        } catch (Exception e) {
            holder.tvDate.setText("Recently");
        }

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(post));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(post));
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public void updateList(List<CommunityPost> newList) {
        this.postList = newList;
        notifyDataSetChanged();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent, tvDate;
        LinearLayout btnEdit, btnDelete;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvDate = itemView.findViewById(R.id.tvDate);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}