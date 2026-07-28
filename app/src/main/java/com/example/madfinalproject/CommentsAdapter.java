package com.example.madfinalproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {

    private List<Comment> commentsList;
    private String currentUserId;

    public CommentsAdapter(List<Comment> commentsList, String currentUserId) {
        this.commentsList = commentsList;
        this.currentUserId = currentUserId;
    }

    public void updateList(List<Comment> newList) {
        this.commentsList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentsList.get(position);

        holder.tvUserName.setText(comment.userName);
        holder.tvCommentText.setText(comment.text);
        holder.tvTime.setText(comment.getTimeAgo());
    }

    @Override
    public int getItemCount() {
        return commentsList != null ? commentsList.size() : 0;
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvCommentText, tvTime;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvCommentText = itemView.findViewById(R.id.tvCommentText);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
