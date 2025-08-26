package com.Mbuntu.MbuntuMobile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.Mbuntu.MbuntuMobile.api.ApiService;
import com.Mbuntu.MbuntuMobile.api.RetrofitClient;
import com.Mbuntu.MbuntuMobile.api.models.UpdateProfileRequest;
import com.Mbuntu.MbuntuMobile.api.models.UserProfileResponse;
import com.Mbuntu.MbuntuMobile.utils.TokenManager;
import com.bumptech.glide.Glide;
import java.util.Map;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private ImageView imageViewProfilePicture;
    private EditText editTextUsername, editTextBio;
    private Button buttonSaveChanges;
    private ProgressBar progressBar;
    private TokenManager tokenManager;
    private ApiService apiService;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialisation
        tokenManager = new TokenManager(this);
        apiService = RetrofitClient.getApiService(this);

        // Liaison des vues
        Toolbar toolbar = findViewById(R.id.toolbarProfile);
        imageViewProfilePicture = findViewById(R.id.imageViewProfilePicture);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextBio = findViewById(R.id.editTextBio);
        buttonSaveChanges = findViewById(R.id.buttonSaveChanges);
        progressBar = findViewById(R.id.progressBarProfile);
        View fabChangePicture = findViewById(R.id.fabChangePicture);

        // Configurer le bouton retour de la toolbar
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialiser le lanceur pour la galerie d'images
        setupImagePicker();

        // Gérer les clics
        fabChangePicture.setOnClickListener(v -> openGallery());
        buttonSaveChanges.setOnClickListener(v -> saveProfileChanges());

        // Charger les données du profil au démarrage
        loadUserProfile();
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        // Afficher l'image sélectionnée et l'uploader
                        Glide.with(this).load(selectedImageUri).into(imageViewProfilePicture);
                        uploadProfilePicture(selectedImageUri);
                    }
                }
        );
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void loadUserProfile() {
        showLoading(true);
        String token = "Bearer " + tokenManager.getToken();

        apiService.getMyProfile(token).enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    populateProfileData(response.body());
                } else {
                    Toast.makeText(ProfileActivity.this, "Impossible de charger le profil", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(ProfileActivity.this, "Erreur réseau", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateProfileData(UserProfileResponse profile) {
        editTextUsername.setText(profile.getUsername());
        editTextBio.setText(profile.getBio());

        if ((profile.getProfilePictureUrl() != null)) {
            Glide.with(this)
                    .load(profile.getProfilePictureUrl())
                    .placeholder(R.mipmap.ic_launcher_round) // Image par défaut
                    .into(imageViewProfilePicture);
        }
    }

    private void saveProfileChanges() {
        showLoading(true);
        String token = "Bearer " + tokenManager.getToken();

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setUsername(editTextUsername.getText().toString());
        request.setBio(editTextBio.getText().toString());

        apiService.updateMyProfile(token, request).enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "Profil mis à jour !", Toast.LENGTH_SHORT).show();
                    populateProfileData(response.body());
                } else {
                    Toast.makeText(ProfileActivity.this, "Erreur lors de la mise à jour", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(ProfileActivity.this, "Erreur réseau", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadProfilePicture(Uri fileUri) {
        showLoading(true);

        try {
            // Convertir l'URI en fichier, puis en MultipartBody.Part
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            File tempFile = new File(getCacheDir(), "temp_image.jpg");
            FileOutputStream fos = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int read;
            while((read = inputStream.read(buffer)) != -1){
                fos.write(buffer, 0, read);
            }
            inputStream.close();
            fos.close();

            RequestBody requestFile = RequestBody.create(MediaType.parse(getContentResolver().getType(fileUri)), tempFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", tempFile.getName(), requestFile);

            String token = "Bearer " + tokenManager.getToken();

            apiService.uploadProfilePicture(token, body).enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(@NonNull Call<Map<String, String>> call, @NonNull Response<Map<String, String>> response) {
                    showLoading(false);
                    if (response.isSuccessful()) {
                        Toast.makeText(ProfileActivity.this, "Photo de profil mise à jour !", Toast.LENGTH_SHORT).show();
                        // On pourrait recharger le profil pour obtenir la nouvelle URL, mais c'est optionnel
                    } else {
                        Toast.makeText(ProfileActivity.this, "Échec de l'upload de l'image.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Map<String, String>> call, @NonNull Throwable t) {
                    showLoading(false);
                    Toast.makeText(ProfileActivity.this, "Erreur réseau lors de l'upload.", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            showLoading(false);
            Toast.makeText(this, "Erreur lors de la préparation du fichier.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }
}