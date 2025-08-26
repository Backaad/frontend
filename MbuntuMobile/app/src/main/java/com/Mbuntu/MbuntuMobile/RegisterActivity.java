package com.Mbuntu.MbuntuMobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.Mbuntu.MbuntuMobile.api.ApiService;
import com.Mbuntu.MbuntuMobile.api.RetrofitClient;
import com.Mbuntu.MbuntuMobile.api.models.AuthenticationResponse;
import com.Mbuntu.MbuntuMobile.api.models.RegisterRequest;
import com.Mbuntu.MbuntuMobile.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextFullName, editTextEmail, editTextPassword; // Simplifié, car nous n'avons que ces 3 champs pour l'API
    private Button buttonRegister;
    private ProgressBar progressBar;
    private TextView textViewLoginLink;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        tokenManager = new TokenManager(this);

        // Liaison des vues
        editTextFullName = findViewById(R.id.editTextFullName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        progressBar = findViewById(R.id.progressBar);
        textViewLoginLink = findViewById(R.id.textViewLoginLink);

        buttonRegister.setOnClickListener(v -> attemptRegistration());

        textViewLoginLink.setOnClickListener(v -> {
            // Retourner à l'activité de connexion
            finish();
        });
    }

    private void attemptRegistration() {
        String fullName = editTextFullName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Validations de base
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs obligatoires", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Le mot de passe doit contenir au moins 6 caractères", Toast.LENGTH_SHORT).show();
            return;
        }

        // Afficher la progression et lancer l'appel
        showLoading(true);
        registerUser(fullName, email, password);
    }

    private void registerUser(String username, String email, String password) {
        RegisterRequest registerRequest = new RegisterRequest(username, email, password);
        ApiService apiService = RetrofitClient.getApiService(this);

        apiService.register(registerRequest).enqueue(new Callback<AuthenticationResponse>() {
            @Override
            public void onResponse(Call<AuthenticationResponse> call, Response<AuthenticationResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    // Succès ! L'utilisateur est créé ET connecté
                    AuthenticationResponse authResponse = response.body();
                    tokenManager.saveToken(authResponse.getToken(), authResponse.getUserId() ,authResponse.getUsername());

                    Toast.makeText(RegisterActivity.this, "Inscription réussie !", Toast.LENGTH_LONG).show();

                    // Naviguer vers l'écran principal
                    navigateToMain();
                } else {
                    // Gérer les erreurs, par exemple si l'email existe déjà
                    Toast.makeText(RegisterActivity.this, "Erreur lors de l'inscription. L'email est peut-être déjà utilisé.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AuthenticationResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(RegisterActivity.this, "Erreur réseau: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            buttonRegister.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            buttonRegister.setVisibility(View.VISIBLE);
        }
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        // Efface toutes les activités précédentes (Login, Register) de la pile
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}