package com.Mbuntu.MbuntuMobile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.Mbuntu.MbuntuMobile.api.ApiService;
import com.Mbuntu.MbuntuMobile.api.RetrofitClient;
import com.Mbuntu.MbuntuMobile.api.models.AuthenticationRequest;
import com.Mbuntu.MbuntuMobile.api.models.AuthenticationResponse;
import com.Mbuntu.MbuntuMobile.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonLogin;
    private ProgressBar progressBar; // Pour le chargement
    private TextView textViewRegister; // Pour le lien vers l'inscription
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tokenManager = new TokenManager(this);

        // Si un token existe déjà, on va directement à l'écran principal sans afficher cet écran
        if (tokenManager.getToken() != null) {
            navigateToMain();
            return;
        }

        // Liaison des vues depuis le layout activity_login.xml
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        progressBar = findViewById(R.id.progressBar);
        textViewRegister = findViewById(R.id.textViewRegister);

        // Définit l'action du clic sur le bouton de connexion
        buttonLogin.setOnClickListener(v -> attemptLogin());

        // Définit l'action du clic sur le lien "S'inscrire"
        textViewRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void attemptLogin() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true); // Affiche la barre de progression
        loginUser(email, password);
    }

    private void loginUser(String email, String password) {
        AuthenticationRequest request = new AuthenticationRequest(email, password);
        ApiService apiService = RetrofitClient.getApiService(this);

        apiService.login(request).enqueue(new Callback<AuthenticationResponse>() {
            @Override
            public void onResponse(Call<AuthenticationResponse> call, Response<AuthenticationResponse> response) {
                showLoading(false); // Cache la barre de progression

                if (response.isSuccessful() && response.body() != null) {
                    AuthenticationResponse authResponse = response.body();
                    tokenManager.saveToken(authResponse.getToken(), authResponse.getUserId(),authResponse.getUsername());
                    Toast.makeText(LoginActivity.this, "Connexion réussie !", Toast.LENGTH_LONG).show();
                    navigateToMain();
                } else {
                    Toast.makeText(LoginActivity.this, "Erreur: Identifiants incorrects.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AuthenticationResponse> call, Throwable t) {
                showLoading(false); // Cache la barre de progression
                Toast.makeText(LoginActivity.this, "Erreur réseau: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("LOGIN_FAILURE", "Erreur: ", t);
            }
        });
    }

    // Méthode pour gérer l'affichage de la ProgressBar et du bouton
    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            buttonLogin.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            buttonLogin.setVisibility(View.VISIBLE);
        }
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        // Efface la pile d'activités pour que l'utilisateur ne puisse pas revenir à l'écran de connexion
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}