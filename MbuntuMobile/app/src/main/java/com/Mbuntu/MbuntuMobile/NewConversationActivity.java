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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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

    // Nouveaux champs pour la gestion de groupe
    private EditText editTextGroupName;
    private FloatingActionButton fabConfirm;

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_conversation);

        tokenManager = new TokenManager(this);
        apiService = RetrofitClient.getApiService(this);

        Toolbar toolbar = findViewById(R.id.toolbarNewConv);
        editTextSearch = findViewById(R.id.editTextSearch);
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        progressBar = findViewById(R.id.progressBarSearch);
        editTextGroupName = findViewById(R.id.editTextGroupName);
        fabConfirm = findViewById(R.id.fabConfirm);

        toolbar.setNavigationOnClickListener(v -> finish());
        setupRecyclerView();
        setupSearch();

        fabConfirm.setOnClickListener(v -> createConversation());
    }

    private void setupRecyclerView() {
        // "this" fonctionne car l'activité implémente l'interface de l'adapter
        userAdapter = new UserAdapter(this);
        recyclerViewUsers.setAdapter(userAdapter);
    }

    private void setupSearch() {
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
            }
            @Override public void afterTextChanged(Editable s) {
                searchRunnable = () -> searchUsers(s.toString());
                searchHandler.postDelayed(searchRunnable, 500);
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
     * Méthode de l'interface, appelée par l'adapter à chaque fois qu'un utilisateur est
     * sélectionné ou désélectionné.
     */
    @Override
    public void onUserClick(UserProfileResponse user, int totalSelected) {
        // Gérer la visibilité des vues pour la création de groupe
        if (totalSelected > 1) {
            editTextGroupName.setVisibility(View.VISIBLE);
        } else {
            editTextGroupName.setVisibility(View.GONE);
        }

        if (totalSelected > 0) {
            fabConfirm.setVisibility(View.VISIBLE);
        } else {
            fabConfirm.setVisibility(View.GONE);
        }
    }

    /**
     * Méthode appelée par le clic sur le bouton flottant de confirmation.
     * Gère à la fois la création de chats privés et de groupes.
     */
    private void createConversation() {
        List<Long> selectedIds = userAdapter.getSelectedUserIds();
        String groupName = null;

        if (selectedIds.isEmpty()) {
            Toast.makeText(this, "Veuillez sélectionner au moins un contact.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Si c'est une sélection multiple, on vérifie le nom du groupe
        if (selectedIds.size() > 1) {
            groupName = editTextGroupName.getText().toString().trim();
            if (groupName.isEmpty()) {
                Toast.makeText(this, "Veuillez entrer un nom pour le groupe.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        showLoading(true);
        String token = "Bearer " + tokenManager.getToken();
        CreateConversationRequest request = new CreateConversationRequest(groupName, selectedIds);

        apiService.createConversation(token, request).enqueue(new Callback<ConversationResponse>() {
            @Override
            public void onResponse(@NonNull Call<ConversationResponse> call, @NonNull Response<ConversationResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    ConversationResponse conversation = response.body();
                    Intent intent = new Intent(NewConversationActivity.this, ChatActivity.class);
                    intent.putExtra("CONVERSATION_ID", conversation.getId());
                    // On passe le nom calculé par le backend
                    intent.putExtra("CONVERSATION_NAME", response.body().getName());
                    intent.putExtra("PARTICIPANT_COUNT", response.body().getParticipants().size());
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(NewConversationActivity.this, "Erreur lors de la création de la conversation", Toast.LENGTH_SHORT).show();
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