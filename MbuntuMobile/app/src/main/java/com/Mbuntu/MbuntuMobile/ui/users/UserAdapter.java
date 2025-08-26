package com.Mbuntu.MbuntuMobile.ui.users;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.Mbuntu.MbuntuMobile.R;
import com.Mbuntu.MbuntuMobile.api.models.UserProfileResponse;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<UserProfileResponse> users = new ArrayList<>();
    private final OnUserClickListener listener;

    // On utilise une interface pour gérer les clics de manière propre,
    // l'activité implémentera cette interface.
    public interface OnUserClickListener {
        void onUserClick(UserProfileResponse user);
    }

    public UserAdapter(OnUserClickListener listener) {
        this.listener = listener;
    }

    public void setUsers(List<UserProfileResponse> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserProfileResponse user = users.get(position);
        holder.bind(user, listener);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }


    // ViewHolder pour un seul utilisateur dans la liste
    static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewAvatar;
        TextView textViewUsername;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewAvatar = itemView.findViewById(R.id.imageViewAvatar);
            textViewUsername = itemView.findViewById(R.id.textViewUsername);
        }

        public void bind(final UserProfileResponse user, final OnUserClickListener listener) {
            textViewUsername.setText(user.getUsername());

            // Utiliser Glide pour charger la photo de profil
            Glide.with(itemView.getContext())
                    .load(user.getProfilePictureUrl())
                    .placeholder(R.mipmap.ic_launcher_round) // Image par défaut
                    .error(R.mipmap.ic_launcher_round)     // Image si l'URL est mauvaise
                    .circleCrop() // Pour un avatar rond
                    .into(imageViewAvatar);

            // Gérer le clic sur toute la ligne
            itemView.setOnClickListener(v -> listener.onUserClick(user));
        }
    }
}