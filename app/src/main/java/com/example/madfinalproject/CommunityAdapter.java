package com.example.madfinalproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CommunityAdapter extends RecyclerView.Adapter<CommunityAdapter.PostViewHolder> {

    private List<CommunityPost> postList;

    public CommunityAdapter(List<CommunityPost> postList) {
        this.postList = postList;
    }

    // Filter ke liye method
    public void updateList(List<CommunityPost> newList) {
        this.postList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_community_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        CommunityPost post = postList.get(position);

        holder.tvName.setText(post.userName);
        holder.tvTime.setText(post.time + " • " + post.role);
        holder.tvTitle.setText(post.title);
        holder.tvBody.setText(post.body);
        holder.tvLikeCount.setText(String.valueOf(post.likeCount));
        holder.tvCommentCount.setText(String.valueOf(post.commentCount));

        // Like Button Click Logic
        holder.btnLike.setOnClickListener(v -> {
            if (post.isLiked) {
                post.likeCount--;
                holder.iconLike.setColorFilter(holder.itemView.getContext().getColor(R.color.gray)); // Gray
            } else {
                post.likeCount++;
                holder.iconLike.setColorFilter(holder.itemView.getContext().getColor(R.color.red)); // Red
            }
            post.isLiked = !post.isLiked;
            holder.tvLikeCount.setText(String.valueOf(post.likeCount));
        });

        // Share Click
        holder.btnShare.setOnClickListener(v ->
                Toast.makeText(v.getContext(), "Sharing " + post.title, Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTime, tvTitle, tvBody, tvLikeCount, tvCommentCount;
        View btnLike, btnShare;
        ImageView iconLike;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvUserName);
            tvTime = itemView.findViewById(R.id.tvTimeRole);
            tvTitle = itemView.findViewById(R.id.tvPostTitle);
            tvBody = itemView.findViewById(R.id.tvPostBody);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            tvCommentCount = itemView.findViewById(R.id.tvCommentCount);
            btnLike = itemView.findViewById(R.id.btnLike);
            iconLike = itemView.findViewById(R.id.iconLike);
            btnShare = itemView.findViewById(R.id.btnShare);
        }
    }
}