package com.example.madfinalproject.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.madfinalproject.R;
import com.example.madfinalproject.models.ConsultantsChatRoom;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ConsultantsChatListAdapter
        extends RecyclerView.Adapter<ConsultantsChatListAdapter.ChatListVH> {

    // ── Click listener interface ──
    public interface OnRoomClickListener {
        void onClick(ConsultantsChatRoom room);
    }

    private final Context             context;
    private final List<ConsultantsChatRoom>      rooms;
    private final OnRoomClickListener listener;

    // ── Constructor ──
    public ConsultantsChatListAdapter(
            Context             context,
            List<ConsultantsChatRoom>      rooms,
            OnRoomClickListener listener
    ) {
        this.context  = context;
        this.rooms    = rooms;
        this.listener = listener;
    }

    // ─────────────────────────────────────────────
    @NonNull
    @Override
    public ChatListVH onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_chat_list, parent, false);
        return new ChatListVH(v);
    }

    // ─────────────────────────────────────────────
    @Override
    public void onBindViewHolder(
            @NonNull ChatListVH holder, int position) {

        ConsultantsChatRoom room = rooms.get(position);

        // ── Name ──
        holder.tvName.setText(room.getConsultantName());

        // ── Expertise — blue color ──
        holder.tvExpertise.setText(room.getConsultantExpertise());

        // ── Last Message ──
        String lastMsg = room.getLastMessage();
        holder.tvLastMessage.setText(
                lastMsg != null && !lastMsg.isEmpty()
                        ? lastMsg
                        : "Start a conversation..."
        );

        // ── Time ──
        holder.tvTime.setText(
                formatTime(room.getLastMessageTime()));

        // ── Unread Badge ──
        int unread = room.getUnreadCount();
        if (unread > 0) {
            holder.tvUnread.setVisibility(View.VISIBLE);
            holder.tvUnread.setText(
                    unread > 99 ? "99+" : String.valueOf(unread));
            // Unread message bold dikhao
            holder.tvLastMessage.setTextColor(0xFF1A1A2E);
            holder.tvLastMessage.setTypeface(
                    null, Typeface.BOLD);
        } else {
            holder.tvUnread.setVisibility(View.GONE);
            holder.tvLastMessage.setTextColor(0xFF888888);
            holder.tvLastMessage.setTypeface(
                    null, Typeface.NORMAL);
        }

        // ── Online Dot ──
        holder.ivOnlineDot.setVisibility(
                room.isConsultantOnline()
                        ? View.VISIBLE
                        : View.INVISIBLE
        );

        // ── Profile Photo ──
        loadProfilePhoto(holder.ivPhoto, room.getConsultantPhoto());

        // ── Item Click ──
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(room);
        });
    }

    // ─────────────────────────────────────────────
    @Override
    public int getItemCount() {
        return rooms.size();
    }

    // ─────────────────────────────────────────────
    // PROFILE PHOTO LOAD
    // ─────────────────────────────────────────────
    private void loadProfilePhoto(ImageView imageView, String photoUrl) {
        if (photoUrl != null && !photoUrl.isEmpty()) {
            Glide.with(context)
                    .load(photoUrl)
                    .transform(new CircleCrop())
                    .placeholder(R.drawable.bg_circle_gray)
                    .error(R.drawable.ic_university)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.ic_university);
        }
    }

    // ─────────────────────────────────────────────
    // TIME FORMAT — Image jaisa
    // ─────────────────────────────────────────────
    private String formatTime(com.google.firebase.Timestamp ts) {
        if (ts == null) return "";

        Date now  = new Date();
        Date then = ts.toDate();
        long diffSec = (now.getTime() - then.getTime()) / 1000;

        if (diffSec < 60)    return "Just now";
        if (diffSec < 3600)  return (diffSec / 60)   + "m";
        if (diffSec < 86400) return (diffSec / 3600)  + "h";

        long diffDays = diffSec / 86400;
        if (diffDays == 1)  return "Yesterday";
        if (diffDays < 7)   return diffDays + "d";

        return new SimpleDateFormat("MMM dd", Locale.getDefault())
                .format(then);
    }

    // ─────────────────────────────────────────────
    // VIEW HOLDER
    // ─────────────────────────────────────────────
    static class ChatListVH extends RecyclerView.ViewHolder {

        ImageView ivPhoto;
        ImageView ivOnlineDot;
        TextView  tvName;
        TextView  tvExpertise;
        TextView  tvLastMessage;
        TextView  tvTime;
        TextView  tvUnread;

        ChatListVH(@NonNull View itemView) {
            super(itemView);
            ivPhoto       = itemView.findViewById(R.id.ivPhoto);
            ivOnlineDot   = itemView.findViewById(R.id.ivOnlineDot);
            tvName        = itemView.findViewById(R.id.tvConsultantName);
            tvExpertise   = itemView.findViewById(R.id.tvExpertise);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTime        = itemView.findViewById(R.id.tvTime);
            tvUnread      = itemView.findViewById(R.id.tvUnreadCount);
        }
    }
}
