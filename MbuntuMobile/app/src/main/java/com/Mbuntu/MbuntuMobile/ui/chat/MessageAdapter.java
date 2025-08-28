package com.Mbuntu.MbuntuMobile.ui.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.Mbuntu.MbuntuMobile.R;
import com.Mbuntu.MbuntuMobile.data.local.MessageEntity;

import java.util.ArrayList;
import java.util.List;

// On revient à un RecyclerView.Adapter simple pour une gestion manuelle de la liste
public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<MessageEntity> messages = new ArrayList<>();
    private final long currentUserId;
    private final boolean isGroupChat;
    private final OnMessageInteractionListener listener;

    public interface OnMessageInteractionListener {
        void onDeleteMessage(MessageEntity message);
    }

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    public MessageAdapter(long currentUserId, boolean isGroupChat, OnMessageInteractionListener listener) {
        this.currentUserId = currentUserId;
        this.isGroupChat = isGroupChat;
        this.listener = listener;
    }

    // Méthode pour initialiser ou rafraîchir complètement la liste
    public void setMessages(List<MessageEntity> newMessages) {
        this.messages.clear();
        this.messages.addAll(newMessages);
        notifyDataSetChanged();
    }

    // Méthode pour ajouter un seul message (non utilisée dans l'architecture finale, mais on peut la garder)
    public void addMessage(MessageEntity message) {
        this.messages.add(message);
        notifyItemInserted(this.messages.size() - 1);
    }

    @Override
    public int getItemViewType(int position) {
        MessageEntity message = messages.get(position);
        if (message.senderId == currentUserId) {
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view, listener);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageEntity message = messages.get(position);
        if (holder.getItemViewType() == VIEW_TYPE_MESSAGE_SENT) {
            ((SentMessageViewHolder) holder).bind(message);
        } else {
            ((ReceivedMessageViewHolder) holder).bind(message, isGroupChat);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // ViewHolder pour les messages ENVOYÉS
    private class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView textViewContent;
        TextView textViewTimestamp;
        ImageView imageViewStatus;

        SentMessageViewHolder(View itemView, OnMessageInteractionListener listener) {
            super(itemView);
            textViewContent = itemView.findViewById(R.id.textViewMessageContent);
            textViewTimestamp = itemView.findViewById(R.id.textViewMessageTimestamp);
            imageViewStatus = itemView.findViewById(R.id.imageViewStatus);

            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    MessageEntity message = messages.get(position); // On récupère depuis notre liste interne
                    if (!MessageEntity.STATUS_SENDING.equals(message.status)) {
                        showDeleteConfirmationDialog(message, listener);
                    }
                }
                return true;
            });
        }

        private void showDeleteConfirmationDialog(MessageEntity message, OnMessageInteractionListener listener) {
            new AlertDialog.Builder(itemView.getContext())
                    .setTitle("Supprimer le message")
                    .setMessage("Êtes-vous sûr de vouloir supprimer ce message ?")
                    .setPositiveButton("Supprimer", (dialog, which) -> listener.onDeleteMessage(message))
                    .setNegativeButton("Annuler", null)
                    .show();
        }

        void bind(MessageEntity message) {
            textViewContent.setText(message.content);
            textViewTimestamp.setText(message.timestamp != null ? message.timestamp.substring(11, 16) : "");
            if (message.status != null) {
                switch (message.status) {
                    case MessageEntity.STATUS_SENDING:
                        imageViewStatus.setImageResource(R.drawable.ic_status_sending);
                        imageViewStatus.setVisibility(View.VISIBLE);
                        break;
                    case MessageEntity.STATUS_FAILED:
                        imageViewStatus.setImageResource(R.drawable.ic_status_failed);
                        imageViewStatus.setVisibility(View.VISIBLE);
                        break;
                    case MessageEntity.STATUS_SENT:
                    default:
                        imageViewStatus.setImageResource(R.drawable.ic_status_sent);
                        imageViewStatus.setVisibility(View.VISIBLE);
                        break;
                }
            } else {
                imageViewStatus.setVisibility(View.GONE);
            }
        }
    }

    // ViewHolder pour les messages REÇUS
    private static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView textViewContent;
        TextView textViewTimestamp;
        TextView textViewSenderName;

        ReceivedMessageViewHolder(View itemView) {
            super(itemView);
            textViewContent = itemView.findViewById(R.id.textViewMessageContent);
            textViewTimestamp = itemView.findViewById(R.id.textViewMessageTimestamp);
            textViewSenderName = itemView.findViewById(R.id.textViewSenderName);
        }

        void bind(MessageEntity message, boolean isGroupChat) {
            textViewContent.setText(message.content);
            textViewTimestamp.setText(message.timestamp != null ? message.timestamp.substring(11, 16) : "");
            if (isGroupChat) {
                textViewSenderName.setText(message.senderUsername);
                textViewSenderName.setVisibility(View.VISIBLE);
            } else {
                textViewSenderName.setVisibility(View.GONE);
            }
        }
    }
}