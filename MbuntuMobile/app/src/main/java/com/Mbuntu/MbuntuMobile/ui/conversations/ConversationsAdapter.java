package com.Mbuntu.MbuntuMobile.ui.conversations;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.Mbuntu.MbuntuMobile.ChatActivity;
import com.Mbuntu.MbuntuMobile.R;
import com.Mbuntu.MbuntuMobile.api.models.ConversationResponse;
import com.Mbuntu.MbuntuMobile.api.models.MessageResponse;

import java.util.ArrayList;
import java.util.List;

public class ConversationsAdapter extends RecyclerView.Adapter<ConversationsAdapter.ConversationViewHolder> {

    // La liste des données que l'adapter va gérer
    private List<ConversationResponse> conversations = new ArrayList<>();

    // Méthode pour mettre à jour la liste des conversations et rafraîchir l'affichage
    public void setConversations(List<ConversationResponse> conversations) {
        this.conversations = conversations;
        notifyDataSetChanged(); // C'est cette ligne qui dit au RecyclerView de se redessiner
    }

    /**
     * Cette méthode est appelée par le RecyclerView quand il a besoin de créer une nouvelle ligne.
     * Elle "gonfle" (inflate) le layout XML d'une ligne (item_conversation.xml) pour le transformer en objet View.
     */
    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    /**
     * Cette méthode est appelée par le RecyclerView pour afficher les données à une position spécifique.
     * Elle lie les données d'une conversation (ex: le nom) aux vues de la ligne.
     */
    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        ConversationResponse conversation = conversations.get(position);
        holder.bind(conversation);
    }

    /**
     * Cette méthode retourne le nombre total d'items dans la liste.
     */
    @Override
    public int getItemCount() {
        return conversations.size();
    }


    /**
     * Le "ViewHolder" est une classe interne qui représente UNE SEULE LIGNE dans la liste.
     * Son rôle est de garder en mémoire les références aux vues (TextView, ImageView...)
     * pour éviter de les rechercher à chaque fois, ce qui est très performant.
     */
    class ConversationViewHolder extends RecyclerView.ViewHolder {
        // Les vues qui composent une ligne de notre liste
        TextView textViewName;
        TextView textViewLastMessage;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            // On fait le lien entre nos variables et les vues du fichier item_conversation.xml
            textViewName = itemView.findViewById(R.id.textViewConversationName);
            textViewLastMessage = itemView.findViewById(R.id.textViewLastMessage);

            // On peut aussi gérer les clics sur une ligne ici
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    ConversationResponse clickedConversation = conversations.get(position);
                    Toast.makeText(itemView.getContext(), "Clic sur la conversation: " + clickedConversation.getName(), Toast.LENGTH_SHORT).show();
                    // Lancer ChatActivity en lui passant l'ID de la conversation
                    Intent intent = new Intent(itemView.getContext(), ChatActivity.class);
                    intent.putExtra("CONVERSATION_ID", clickedConversation.getId());
                    intent.putExtra("CONVERSATION_NAME", clickedConversation.getName());
                    itemView.getContext().startActivity(intent);
                }
            });
        }

        // Méthode pour remplir les vues avec les données d'un objet ConversationResponse
        void bind(ConversationResponse conversation) {
            textViewName.setText(conversation.getName());
            // Pour l'instant, le dernier message est un texte par défaut
            textViewLastMessage.setText("Participants: " + conversation.getParticipantIds().size());
        }
    }


}