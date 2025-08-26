package com.Mbuntu.MbuntuMobile;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.Mbuntu.MbuntuMobile.api.ApiService;
import com.Mbuntu.MbuntuMobile.api.RetrofitClient;
import com.Mbuntu.MbuntuMobile.api.models.ConversationResponse;
import com.Mbuntu.MbuntuMobile.api.models.CreateConversationRequest;
import com.Mbuntu.MbuntuMobile.api.models.UserProfileResponse;
import com.Mbuntu.MbuntuMobile.ui.users.UserAdapter;
import com.Mbuntu.MbuntuMobile.utils.TokenManager;

import java.util.Collections;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewConversationActivity extends AppCompatActivity implements UserAdapter.OnUserClickListener {

    private EditText editTextSearch;
    private RecyclerView recyclerViewUsers;
    private ProgressBar progressBar;
    private UserAdapter userAdapter;
    private TokenManager tokenManager;
    private ApiService apiService;

    // Pour éviter de lancer une recherche à chaque lettre tapée
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_conversation);

        // Initialisation
        tokenManager = new TokenManager(this);
        apiService = RetrofitClient.getApiService(this);

        // Liaison des vues
        Toolbar toolbar = findViewById(R.id.toolbarNewConv);
        editTextSearch = findViewById(R.id.editTextSearch);
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        progressBar = findViewById(R.id.progressBarSearch);

        // Configuration
        toolbar.setNavigationOnClickListener(v -> finish());
        setupRecyclerView();
        setupSearch();
    }

    private void setupRecyclerView() {
        // On passe "this" car notre activité implémente l'interface OnUserClickListener
        userAdapter = new UserAdapter(this);
        recyclerViewUsers.setAdapter(userAdapter);
    }

    private void setupSearch() {
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Annuler la recherche précédente si l'utilisateur tape encore
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // On lance la recherche après un délai de 500ms
                searchRunnable = () -> searchUsers(s.toString());
                searchHandler.postDelayed(searchRunnable, 500); // Délai de 500ms
            }
        });
    }

    private void searchUsers(String query) {
        if (query.trim().length() < 2) {
            userAdapter.setUsers(Collections.emptyList());
            return;
        }

        showLoading(true);
        String token = "Bearer " + tokenManager.getToken();

        apiService.searchUsers(token, query.trim()).enqueue(new Callback<List<UserProfileResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<UserProfileResponse>> call, @NonNull Response<List<UserProfileResponse>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    userAdapter.setUsers(response.body());
                } else {
                    Toast.makeText(NewConversationActivity.this, "Erreur de recherche", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<UserProfileResponse>> call, @NonNull Throwable t) {
                showLoading(false);
                Toast.makeText(NewConversationActivity.this, "Erreur réseau", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Cette méthode est appelée par l'UserAdapter quand on clique sur un utilisateur.
     * Elle est définie par l'interface UserAdapter.OnUserClickListener.
     */
    @Override
    public void onUserClick(UserProfileResponse user) {
        Toast.makeText(this, "Création d'une conversation avec " + user.getUsername(), Toast.LENGTH_SHORT).show();
        // On appelle l'API pour créer la conversation
        createConversationWith(user);
    }

    private void createConversationWith(UserProfileResponse user) {
        showLoading(true);
        String token = "Bearer " + tokenManager.getToken();
        CreateConversationRequest request = new CreateConversationRequest(
                "Conversation avec " + user.getUsername(), // Nom temporaire
                Collections.singletonList(user.getId())
        );

        apiService.createConversation(token, request).enqueue(new Callback<ConversationResponse>() {
            @Override
            public void onResponse(@NonNull Call<ConversationResponse> call, @NonNull Response<ConversationResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    // Succès ! On navigue vers l'écran de chat.
                    ConversationResponse conversation = response.body();
                    Intent intent = new Intent(NewConversationActivity.this, ChatActivity.class);
                    intent.putExtra("CONVERSATION_ID", conversation.getId());
                    intent.putExtra("CONVERSATION_NAME", conversation.getName());
                    startActivity(intent);
                    finish(); // On ferme cet écran
                } else {
                    Toast.makeText(NewConversationActivity.this, "Erreur lors de la création", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ConversationResponse> call, @NonNull Throwable t) {
                showLoading(false);
                Toast.makeText(NewConversationActivity.this, "Erreur réseau", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }
}