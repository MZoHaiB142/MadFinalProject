package com.example.madfinalproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.madfinalproject.R;
import com.example.madfinalproject.models.ConsultantsChatMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ConsultantChatMessageAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT     = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private final List<ConsultantsChatMessage> messages;
    private final String     currentUserId;

    public ConsultantChatMessageAdapter(List<ConsultantsChatMessage> messages,
                              String currentUserId) {
        this.messages      = messages;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        ConsultantsChatMessage msg = messages.get(position);
        return msg.getSenderId().equals(currentUserId)
                ? VIEW_TYPE_SENT
                : VIEW_TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        LayoutInflater inf = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_SENT) {
            View v = inf.inflate(R.layout.item_message_sent,
                    parent, false);
            return new SentVH(v);
        } else {
            View v = inf.inflate(R.layout.item_message_received,
                    parent, false);
            return new ReceivedVH(v);
        }
    }

    @Override
    public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder holder, int position) {

        ConsultantsChatMessage msg = messages.get(position);
        String timeStr  = formatTime(msg.getTimestamp());

        if (holder instanceof SentVH) {
            SentVH h = (SentVH) holder;
            h.tvMessage.setText(msg.getText());
            h.tvTime.setText(timeStr);
            // Read receipt
            h.tvRead.setText(msg.isRead() ? "✓✓" : "✓");
            h.tvRead.setTextColor(
                    msg.isRead() ? 0xFF1976D2 : 0xFF9E9E9E);
        } else if (holder instanceof ReceivedVH) {
            ReceivedVH h = (ReceivedVH) holder;
            h.tvMessage.setText(msg.getText());
            h.tvTime.setText(timeStr);
        }
    }

    @Override
    public int getItemCount() { return messages.size(); }

    private String formatTime(com.google.firebase.Timestamp ts) {
        if (ts == null) return "";
        Date date = ts.toDate();
        return new SimpleDateFormat("hh:mm a", Locale.getDefault())
                .format(date);
    }

    // ── Sent Message ViewHolder ──
    static class SentVH extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime, tvRead;
        SentVH(@NonNull View v) {
            super(v);
            tvMessage = v.findViewById(R.id.tvMessage);
            tvTime    = v.findViewById(R.id.tvTime);
            tvRead    = v.findViewById(R.id.tvRead);
        }
    }

    // ── Received Message ViewHolder ──
    static class ReceivedVH extends RecyclerView.ViewHolder {
        TextView  tvMessage, tvTime;
        ImageView ivSenderPhoto;  // ← add karo

        ReceivedVH(@NonNull View v) {
            super(v);
            tvMessage    = v.findViewById(R.id.tvMessage);
            tvTime       = v.findViewById(R.id.tvTime);
            ivSenderPhoto = v.findViewById(R.id.ivSenderPhoto); // ← add karo
        }
    }
}