package com.Mbuntu.MbuntuMobile;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.Mbuntu.MbuntuMobile.data.repository.MessageRepository; // NOUVEL IMPORT
import com.Mbuntu.MbuntuMobile.ui.chat.MessageAdapter;
import com.Mbuntu.MbuntuMobile.utils.TokenManager;
import com.Mbuntu.MbuntuMobile.websocket.WebSocketManager;

public class ChatActivity extends AppCompatActivity {

    private long conversationId;
    private long currentUserId;

    private Toolbar toolbar;
    private RecyclerView recyclerViewMessages;
    private EditText editTextMessage;
    private View buttonSend;

    private MessageAdapter messageAdapter;
    private MessageRepository messageRepository; // NOUVEAU : Le Repository
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        conversationId = getIntent().getLongExtra("CONVERSATION_ID", -1L);
        String conversationName = getIntent().getStringExtra("CONVERSATION_NAME");

        // Initialisation des managers et repositories
        tokenManager = new TokenManager(this);
        currentUserId = tokenManager.getUserId();
        messageRepository = new MessageRepository(this); // On crée notre repository

        // Liaison des vues
        toolbar = findViewById(R.id.toolbarChat);
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);

        // Configuration
        toolbar.setTitle(conversationName);
        toolbar.setNavigationOnClickListener(v -> finish());

        setupRecyclerView();

        if (conversationId == -1L) {
            Toast.makeText(this, "Erreur: Conversation invalide", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // On commence à observer les messages de la base de données locale
        observeMessages();

        buttonSend.setOnClickListener(v -> sendMessage());
    }

    // La gestion du WebSocket a été déplacée dans le Repository et n'est plus nécessaire ici
    // Les méthodes onStart, onStop, setupMessageObserver sont donc à supprimer.

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerViewMessages.setLayoutManager(layoutManager);
        recyclerViewMessages.setAdapter(messageAdapter);
    }

    // NOUVELLE FAÇON DE CHARGER LES MESSAGES
    private void observeMessages() {
        // On demande au repository le LiveData des messages.
        // Le repository va nous donner les données locales ET lancer une synchro réseau.
        messageRepository.getMessagesForConversation(conversationId).observe(this, messageEntities -> {
            // Cet bloc de code sera exécuté AUTOMATIQUEMENT chaque fois
            // que la liste des messages dans la base de données locale change.
            if (messageEntities != null) {
                messageAdapter.setMessages(messageEntities);
                recyclerViewMessages.scrollToPosition(messageAdapter.getItemCount() - 1);
            }
        });
    }

    // NOUVELLE FAÇON D'ENVOYER UN MESSAGE
    private void sendMessage() {
        String content = editTextMessage.getText().toString().trim();
        if (content.isEmpty()) return;

        // On dit simplement au repository d'envoyer le message.
        // Le repository va gérer l'UI optimiste, la sauvegarde locale et l'appel réseau.
        messageRepository.sendMessage(conversationId, content);

        // On vide le champ de texte
        editTextMessage.setText("");
    }

    // L'ancienne méthode loadMessageHistory() est maintenant gérée par le Repository.
}