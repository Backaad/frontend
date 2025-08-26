package com.Mbuntu.MbuntuMobile.ui.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.Mbuntu.MbuntuMobile.R;
import com.Mbuntu.MbuntuMobile.data.local.MessageEntity; // MODIFIÉ : On utilise l'entité locale
import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<MessageEntity> messages = new ArrayList<>(); // La liste est maintenant de type MessageEntity
    private final long currentUserId;

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    public MessageAdapter(long currentUserId) {
        this.currentUserId = currentUserId;
    }

    // L'adapter reçoit maintenant une liste d'entités de la base de données
    public void setMessages(List<MessageEntity> newMessages) {
        this.messages.clear();
        this.messages.addAll(newMessages);
        notifyDataSetChanged();
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
            return new SentMessageViewHolder(view);
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
            ((ReceivedMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // ViewHolder pour les messages ENVOYÉS
    private static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView textViewContent;
        TextView textViewTimestamp;
        ImageView imageViewStatus; // Pour afficher l'état (envoi, envoyé, échec)

        SentMessageViewHolder(View itemView) {
            super(itemView);
            textViewContent = itemView.findViewById(R.id.textViewMessageContent);
            textViewTimestamp = itemView.findViewById(R.id.textViewMessageTimestamp);
            // imageViewStatus = itemView.findViewById(R.id.imageViewStatus); // À ajouter dans le layout
        }

        void bind(MessageEntity message) {
            textViewContent.setText(message.content);
            textViewTimestamp.setText(message.timestamp != null ? message.timestamp.substring(11, 16) : "");

            // Mettre à jour l'icône de statut
            // if (MessageEntity.STATUS_SENDING.equals(message.status)) { ... }
            // else if (MessageEntity.STATUS_FAILED.equals(message.status)) { ... }
        }
    }

    // ViewHolder pour les messages REÇUS
    private static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView textViewContent;
        TextView textViewTimestamp;

        ReceivedMessageViewHolder(View itemView) {
            super(itemView);
            textViewContent = itemView.findViewById(R.id.textViewMessageContent);
            textViewTimestamp = itemView.findViewById(R.id.textViewMessageTimestamp);
        }

        void bind(MessageEntity message) {
            textViewContent.setText(message.content);
            textViewTimestamp.setText(message.timestamp != null ? message.timestamp.substring(11, 16) : "");
        }
    }
}