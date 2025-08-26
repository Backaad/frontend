package com.Mbuntu.MbuntuMobile.ui.conversations;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
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

        // Initialisation
        tokenManager = new TokenManager(getContext());
        recyclerViewConversations = view.findViewById(R.id.recyclerViewConversations);
        progressBar = view.findViewById(R.id.progressBar);
        fabNewChat = view.findViewById(R.id.fab_new_chat);

        // --- MODIFICATION PRINCIPALE ---
        // Le clic sur le bouton flottant n'appelle plus l'API, mais lance la nouvelle activité.
        fabNewChat.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NewConversationActivity.class);
            startActivity(intent);
        });

        setupRecyclerView();
    }

    @Override
    public void onResume() {
        super.onResume();
        // On recharge la liste des conversations chaque fois que ce fragment redevient visible.
        // C'est crucial pour voir les nouvelles conversations créées depuis NewConversationActivity.
        loadConversations();
    }

    private void setupRecyclerView() {
        adapter = new ConversationsAdapter();
        recyclerViewConversations.setAdapter(adapter);
    }

    private void loadConversations() {
        showLoading(true);
        String token = "Bearer " + tokenManager.getToken();

        ApiService apiService = RetrofitClient.getApiService(getContext());
        apiService.getConversations(token).enqueue(new Callback<List<ConversationResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<ConversationResponse>> call, @NonNull Response<List<ConversationResponse>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setConversations(response.body());
                } else {
                    Toast.makeText(getContext(), "Impossible de charger les conversations", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ConversationResponse>> call, @NonNull Throwable t) {
                showLoading(false);
                Toast.makeText(getContext(), "Erreur réseau: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // La méthode createNewConversation() a été supprimée car cette logique est maintenant dans NewConversationActivity.

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerViewConversations.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            recyclerViewConversations.setVisibility(View.VISIBLE);
        }
    }
}