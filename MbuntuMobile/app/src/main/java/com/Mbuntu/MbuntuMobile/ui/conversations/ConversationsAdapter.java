package com.Mbuntu.MbuntuMobile.ui.conversations;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.Mbuntu.MbuntuMobile.ChatActivity;
import com.Mbuntu.MbuntuMobile.R;
import com.Mbuntu.MbuntuMobile.api.models.ConversationResponse;
import com.Mbuntu.MbuntuMobile.api.models.UserProfileResponse;
import com.Mbuntu.MbuntuMobile.utils.AvatarGenerator;
import com.Mbuntu.MbuntuMobile.utils.TokenManager;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class ConversationsAdapter extends RecyclerView.Adapter<ConversationsAdapter.ConversationViewHolder> {

    private final List<ConversationResponse> conversations;
    private final TokenManager tokenManager;

    public ConversationsAdapter(Context context) {
        this.conversations = new ArrayList<>();
        this.tokenManager = new TokenManager(context);
    }

    public void setConversations(List<ConversationResponse> newConversations) {
        this.conversations.clear();
        this.conversations.addAll(newConversations);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        ConversationResponse conversation = conversations.get(position);
        holder.bind(conversation, tokenManager.getUserId());
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }


    class ConversationViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewAvatar;
        TextView textViewName;
        TextView textViewLastMessage;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewAvatar = itemView.findViewById(R.id.imageViewAvatar);
            textViewName = itemView.findViewById(R.id.textViewConversationName);
            textViewLastMessage = itemView.findViewById(R.id.textViewLastMessage);
        }

        void bind(final ConversationResponse conversation, long currentUserId) {
            String conversationTitle = conversation.getName();
            UserProfileResponse otherUser = null;

            if (conversationTitle == null || conversationTitle.isEmpty()) {
                if (conversation.getParticipants() != null && conversation.getParticipants().size() > 1) {
                    for (UserProfileResponse participant : conversation.getParticipants()) {
                        if (participant.getId() != currentUserId) {
                            otherUser = participant;
                            break;
                        }
                    }
                }
                conversationTitle = (otherUser != null) ? otherUser.getUsername() : "Conversation";
            }

            textViewName.setText(conversationTitle);
            textViewLastMessage.setText("Cliquez pour discuter...");

            String imageUrl = null;
            if (otherUser != null && otherUser.getProfilePictureUrl() != null) {
                imageUrl = String.valueOf(otherUser.getProfilePictureUrl()); // En supposant que c'est une String
            }
            // on genere un avatar par defeaut avec l'initiale
            Drawable placeholder = AvatarGenerator.getDrawable(conversationTitle);

            Glide.with(itemView.getContext())
                    .load(imageUrl)
                    .placeholder(placeholder)
                    .error(placeholder)
                    .circleCrop()
                    .into(imageViewAvatar);

            final String finalConversationTitle = conversationTitle;
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    // Maintenant que la classe n'est plus statique, 'conversations' est accessible ici.
                    ConversationResponse clickedConversation = conversations.get(position);

                    Intent intent = new Intent(itemView.getContext(), ChatActivity.class);
                    intent.putExtra("CONVERSATION_ID", clickedConversation.getId());
                    intent.putExtra("CONVERSATION_NAME", finalConversationTitle);

                    int participantCount = 0;
                    if (clickedConversation.getParticipants() != null) {
                        participantCount = clickedConversation.getParticipants().size();
                    }
                    intent.putExtra("PARTICIPANT_COUNT", participantCount);

                    itemView.getContext().startActivity(intent);
                }
            });
        }
    }
}