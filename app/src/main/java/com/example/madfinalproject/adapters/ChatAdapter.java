package com.example.madfinalproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.madfinalproject.R;
import com.example.madfinalproject.models.ChatMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying chat messages in RecyclerView
 */
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER = 0;
    private static final int VIEW_TYPE_BOT = 1;
    private static final int VIEW_TYPE_LOADING = 2;
    private static final int VIEW_TYPE_ERROR = 3;

    private List<ChatMessage> messages;

    public ChatAdapter() {
        this.messages = new ArrayList<>();
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public void updateLastMessage(String text, int type) {
        if (!messages.isEmpty()) {
            int lastIndex = messages.size() - 1;
            ChatMessage lastMessage = messages.get(lastIndex);
            if (lastMessage.getType() == ChatMessage.TYPE_LOADING) {
                messages.remove(lastIndex);
                messages.add(new ChatMessage(text, type));
                notifyItemChanged(lastIndex);
            } else {
                addMessage(new ChatMessage(text, type));
            }
        }
    }

    public void removeLoadingMessage() {
        if (!messages.isEmpty()) {
            int lastIndex = messages.size() - 1;
            ChatMessage lastMessage = messages.get(lastIndex);
            if (lastMessage.getType() == ChatMessage.TYPE_LOADING) {
                messages.remove(lastIndex);
                notifyItemRemoved(lastIndex);
            }
        }
    }

    public void clearMessages() {
        messages.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messages.get(position);
        switch (message.getType()) {
            case ChatMessage.TYPE_USER:
                return VIEW_TYPE_USER;
            case ChatMessage.TYPE_BOT:
                return VIEW_TYPE_BOT;
            case ChatMessage.TYPE_LOADING:
                return VIEW_TYPE_LOADING;
            case ChatMessage.TYPE_ERROR:
                return VIEW_TYPE_ERROR;
            default:
                return VIEW_TYPE_BOT;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        
        switch (viewType) {
            case VIEW_TYPE_USER:
                return new UserMessageViewHolder(
                    inflater.inflate(R.layout.item_chat_user, parent, false)
                );
            case VIEW_TYPE_BOT:
                return new BotMessageViewHolder(
                    inflater.inflate(R.layout.item_chat_bot, parent, false)
                );
            case VIEW_TYPE_LOADING:
                return new LoadingViewHolder(
                    inflater.inflate(R.layout.item_chat_loading, parent, false)
                );
            case VIEW_TYPE_ERROR:
                return new ErrorViewHolder(
                    inflater.inflate(R.layout.item_chat_error, parent, false)
                );
            default:
                return new BotMessageViewHolder(
                    inflater.inflate(R.layout.item_chat_bot, parent, false)
                );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);

        if (holder instanceof UserMessageViewHolder) {
            ((UserMessageViewHolder) holder).bind(message);
        } else if (holder instanceof BotMessageViewHolder) {
            ((BotMessageViewHolder) holder).bind(message);
        } else if (holder instanceof LoadingViewHolder) {
            ((LoadingViewHolder) holder).bind();
        } else if (holder instanceof ErrorViewHolder) {
            ((ErrorViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // ViewHolders
    static class UserMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView tvMessage;

        UserMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvUserMessage);
        }

        void bind(ChatMessage message) {
            tvMessage.setText(message.getMessage());
        }
    }

    static class BotMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView tvMessage;

        BotMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvBotMessage);
        }

        void bind(ChatMessage message) {
            tvMessage.setText(message.getMessage());
        }
    }

    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        private ProgressBar progressBar;
        private TextView tvLoading;

        LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar);
            tvLoading = itemView.findViewById(R.id.tvLoading);
        }

        void bind() {
            progressBar.setVisibility(View.VISIBLE);
            tvLoading.setText("AI is thinking...");
        }
    }

    static class ErrorViewHolder extends RecyclerView.ViewHolder {
        private TextView tvError;

        ErrorViewHolder(@NonNull View itemView) {
            super(itemView);
            tvError = itemView.findViewById(R.id.tvError);
        }

        void bind(ChatMessage message) {
            tvError.setText(message.getMessage());
        }
    }
}
