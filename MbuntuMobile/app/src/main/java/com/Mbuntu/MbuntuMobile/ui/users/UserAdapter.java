package com.Mbuntu.MbuntuMobile.ui.users;

import android.graphics.Color;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<UserProfileResponse> users = new ArrayList<>();
    private final Set<Long> selectedUserIds = new HashSet<>(); // Pour stocker les sélections
    private final OnUserClickListener listener;

    // L'interface de communication change pour renvoyer le nombre d'utilisateurs sélectionnés
    public interface OnUserClickListener {
        void onUserClick(UserProfileResponse user, int totalSelected);
    }

    public UserAdapter(OnUserClickListener listener) {
        this.listener = listener;
    }

    public void setUsers(List<UserProfileResponse> users) {
        this.users = users;
        this.selectedUserIds.clear(); // On vide la sélection quand la liste change
        notifyDataSetChanged();
    }

    // Méthode pour que l'activité puisse récupérer la liste des IDs
    public List<Long> getSelectedUserIds() {
        return new ArrayList<>(selectedUserIds);
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
        boolean isSelected = selectedUserIds.contains(user.getId());
        holder.bind(user, isSelected, listener);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }


    // Le ViewHolder est maintenant conscient de l'état de sélection
    class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewAvatar;
        TextView textViewUsername;
        View itemView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            imageViewAvatar = itemView.findViewById(R.id.imageViewAvatar);
            textViewUsername = itemView.findViewById(R.id.textViewUsername);
        }

        public void bind(final UserProfileResponse user, boolean isSelected, final OnUserClickListener listener) {
            textViewUsername.setText(user.getUsername());

            Glide.with(itemView.getContext())
                    .load(user.getProfilePictureUrl())
                    .placeholder(R.mipmap.ic_launcher_round)
                    .error(R.mipmap.ic_launcher_round)
                    .circleCrop()
                    .into(imageViewAvatar);

            // On change l'apparence de la ligne si elle est sélectionnée
            itemView.setBackgroundColor(isSelected ? Color.parseColor("#E0E0E0") : Color.TRANSPARENT);

            itemView.setOnClickListener(v -> {
                // Logique de sélection/désélection
                if (selectedUserIds.contains(user.getId())) {
                    selectedUserIds.remove(user.getId());
                } else {
                    selectedUserIds.add(user.getId());
                }
                // On notifie l'activité du changement
                listener.onUserClick(user, selectedUserIds.size());
                // On rafraîchit l'affichage de cette ligne spécifique
                notifyItemChanged(getAdapterPosition());
            });
        }
    }
}