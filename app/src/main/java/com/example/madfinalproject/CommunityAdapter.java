package com.example.madfinalproject;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.madfinalproject.utils.Constants;
import com.example.madfinalproject.utils.LogUtils;
// ✅ Firestore Imports
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;

public class CommunityAdapter extends RecyclerView.Adapter<CommunityAdapter.PostViewHolder> {

    private List<CommunityPost> postList;
    private Context context;
    private String currentUserId;
    // ✅ Firestore Reference
    private FirebaseFirestore db;

    public CommunityAdapter(List<CommunityPost> postList, Context context, String currentUserId) {
        this.postList = postList;
        this.context = context;
        this.currentUserId = currentUserId;
        // ✅ Initialize Firestore
        this.db = FirebaseFirestore.getInstance();
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
        holder.tvTime.setText(post.getTimeAgo() + " • " + post.role);
        holder.tvTitle.setText(post.title);
        holder.tvBody.setText(post.body);
        holder.tvLikeCount.setText(String.valueOf(post.getLikeCount()));
        holder.tvCommentCount.setText(String.valueOf(post.commentCount));

        // Update like icon color based on like status
        if (post.isLiked) {
            holder.iconLike.setColorFilter(context.getResources().getColor(R.color.red, context.getTheme()));
        } else {
            holder.iconLike.setColorFilter(context.getResources().getColor(R.color.gray, context.getTheme()));
        }

        // Like Button Click Logic - Firestore Update
        holder.btnLike.setOnClickListener(v -> {
            if (currentUserId == null) {
                Toast.makeText(context, "Please login to like posts", Toast.LENGTH_SHORT).show();
                return;
            }

            if (post.postId == null) {
                Toast.makeText(context, "Post ID not found", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Firestore Document Reference
            DocumentReference postRef = db.collection(Constants.DB_COMMUNITY_POSTS).document(post.postId);

            if (post.isLiked) {
                // UNLIKE: Remove user from likes map using FieldValue.delete()
                // Syntax: "likes.USER_ID" -> DELETE
                postRef.update("likes." + currentUserId, FieldValue.delete())
                        .addOnSuccessListener(aVoid -> {
                            post.isLiked = false;
                            if (post.likes != null) {
                                post.likes.remove(currentUserId);
                            }
                            holder.tvLikeCount.setText(String.valueOf(post.getLikeCount()));
                            holder.iconLike.setColorFilter(context.getResources().getColor(R.color.gray, context.getTheme()));
                            notifyItemChanged(position); // Refresh single item
                        })
                        .addOnFailureListener(e -> {
                            LogUtils.e("CommunityAdapter", "Error unliking post", e);
                            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                // LIKE: Add user to likes map
                // Syntax: "likes.USER_ID" -> true
                postRef.update("likes." + currentUserId, true)
                        .addOnSuccessListener(aVoid -> {
                            post.isLiked = true;
                            if (post.likes == null) {
                                post.likes = new HashMap<>();
                            }
                            post.likes.put(currentUserId, true);
                            holder.tvLikeCount.setText(String.valueOf(post.getLikeCount()));
                            holder.iconLike.setColorFilter(context.getResources().getColor(R.color.red, context.getTheme()));
                            notifyItemChanged(position); // Refresh single item
                        })
                        .addOnFailureListener(e -> {
                            LogUtils.e("CommunityAdapter", "Error liking post", e);
                            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });

        // Comment Button Click - Open Comments Activity
        holder.btnComment.setOnClickListener(v -> {
            if (post.postId == null) {
                Toast.makeText(context, "Post ID not found", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(context, CommentsActivity.class);
            intent.putExtra("postId", post.postId);
            intent.putExtra("postTitle", post.title);
            context.startActivity(intent);
        });

        // More Menu (Edit/Delete for own posts)
        if (currentUserId != null && post.userId != null && post.userId.equals(currentUserId)) {
            holder.btnMore.setVisibility(View.VISIBLE);
            holder.btnMore.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(context, holder.btnMore);
                popup.getMenu().add("Edit Post");
                popup.getMenu().add("Delete Post");
                popup.setOnMenuItemClickListener(item -> {
                    if (item.getTitle().toString().equals("Edit Post")) {
                        editPost(post);
                        return true;
                    } else if (item.getTitle().toString().equals("Delete Post")) {
                        deletePost(post, position);
                        return true;
                    }
                    return false;
                });
                popup.show();
            });
        } else {
            holder.btnMore.setVisibility(View.GONE);
        }

        // Share Click
        holder.btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, post.title);
            shareIntent.putExtra(Intent.EXTRA_TEXT, post.title + "\n\n" + post.body);
            context.startActivity(Intent.createChooser(shareIntent, "Share Post"));
        });
    }

    private void editPost(CommunityPost post) {
        Intent intent = new Intent(context, CreatePostActivity.class);
        intent.putExtra("editMode", true);
        intent.putExtra("postId", post.postId);
        intent.putExtra("title", post.title);
        intent.putExtra("body", post.body);
        intent.putExtra("category", post.category);
        context.startActivity(intent);
    }

    private void deletePost(CommunityPost post, int position) {
        if (post.postId == null) {
            Toast.makeText(context, "Post ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show confirmation dialog
        new androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // ✅ Firestore Delete
                    db.collection(Constants.DB_COMMUNITY_POSTS).document(post.postId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(context, "Post deleted successfully", Toast.LENGTH_SHORT).show();
                                postList.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, postList.size());
                            })
                            .addOnFailureListener(e -> {
                                LogUtils.e("CommunityAdapter", "Error deleting post", e);
                                Toast.makeText(context, "Error deleting post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return postList != null ? postList.size() : 0;
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTime, tvTitle, tvBody, tvLikeCount, tvCommentCount;
        View btnLike, btnComment, btnShare;
        ImageView iconLike;
        ImageButton btnMore;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvUserName);
            tvTime = itemView.findViewById(R.id.tvTimeRole);
            tvTitle = itemView.findViewById(R.id.tvPostTitle);
            tvBody = itemView.findViewById(R.id.tvPostBody);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            tvCommentCount = itemView.findViewById(R.id.tvCommentCount);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnComment = itemView.findViewById(R.id.btnComment);
            iconLike = itemView.findViewById(R.id.iconLike);
            btnShare = itemView.findViewById(R.id.btnShare);
            btnMore = itemView.findViewById(R.id.btnMore);
        }
    }
}