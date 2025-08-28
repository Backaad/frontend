package com.Mbuntu.MbuntuMobile.ui.conversations;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView; // Nouvel import
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.Mbuntu.MbuntuMobile.NewConversationActivity;
import com.Mbuntu.MbuntuMobile.R;
import com.Mbuntu.MbuntuMobile.api.ApiService;
import com.Mbuntu.MbuntuMobile.api.RetrofitClient;
import com.Mbuntu.MbuntuMobile.api.models.ConversationResponse;
import com.Mbuntu.MbuntuMobile.utils.TokenManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConversationsFragment extends Fragment {

    private RecyclerView recyclerViewConversations;
    private ProgressBar progressBar;
    private TextView textViewEmptyList; // La vue pour le message "liste vide"
    private TokenManager tokenManager;
    private ConversationsAdapter adapter;
    private FloatingActionButton fabNewChat;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_conversations, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tokenManager = new TokenManager(getContext());
        recyclerViewConversations = view.findViewById(R.id.recyclerViewConversations);
        progressBar = view.findViewById(R.id.progressBar);
        textViewEmptyList = view.findViewById(R.id.textViewEmptyList); // Liaison de la nouvelle vue
        fabNewChat = view.findViewById(R.id.fab_new_chat);

        fabNewChat.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NewConversationActivity.class);
            startActivity(intent);
        });

        setupRecyclerView();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadConversations();
    }

    private void setupRecyclerView() {
        adapter = new ConversationsAdapter(getContext());
        recyclerViewConversations.setAdapter(adapter);
    }

    private void loadConversations() {
        showLoading(true);
        String token = "Bearer " + tokenManager.getToken();

        RetrofitClient.getApiService(getContext()).getConversations(token).enqueue(new Callback<List<ConversationResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<ConversationResponse>> call, @NonNull Response<List<ConversationResponse>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<ConversationResponse> conversations = response.body();
                    // --- NOUVELLE LOGIQUE D'AFFICHAGE ---
                    if (conversations.isEmpty()) {
                        // Si la liste est vide, on affiche le message approprié.
                        textViewEmptyList.setText("Aucune conversation.\nCliquez sur '+' pour en démarrer une.");
                        textViewEmptyList.setVisibility(View.VISIBLE);
                        recyclerViewConversations.setVisibility(View.GONE);
                    } else {
                        // Sinon, on affiche la liste des conversations.
                        textViewEmptyList.setVisibility(View.GONE);
                        recyclerViewConversations.setVisibility(View.VISIBLE);
                        adapter.setConversations(conversations);
                    }
                } else {
                    // Gérer le cas où la réponse du serveur est une erreur
                    textViewEmptyList.setText("Impossible de charger les conversations.");
                    textViewEmptyList.setVisibility(View.VISIBLE);
                    recyclerViewConversations.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ConversationResponse>> call, @NonNull Throwable t) {
                showLoading(false);
                // Gérer le cas où il n'y a pas de connexion réseau
                textViewEmptyList.setText("Erreur réseau. Vérifiez votre connexion.");
                textViewEmptyList.setVisibility(View.VISIBLE);
                recyclerViewConversations.setVisibility(View.GONE);
            }
        });
    }

    private void showLoading(boolean isLoading) {
        // La méthode gère maintenant les 3 états : chargement, liste vide, et liste affichée.
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            textViewEmptyList.setVisibility(View.GONE);
            recyclerViewConversations.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }
}