package com.Mbuntu.MbuntuMobile;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.Mbuntu.MbuntuMobile.data.local.MessageEntity;
import com.Mbuntu.MbuntuMobile.data.repository.MessageRepository;
import com.Mbuntu.MbuntuMobile.ui.chat.MessageAdapter;
import com.Mbuntu.MbuntuMobile.utils.TokenManager;

// On implémente bien l'interface de l'adapter pour la suppression
public class ChatActivity extends AppCompatActivity implements MessageAdapter.OnMessageInteractionListener {

    private long conversationId;
    private long currentUserId;
    private boolean isGroupChat;

    private Toolbar toolbar;
    private RecyclerView recyclerViewMessages;
    private EditText editTextMessage;
    private View buttonSend;

    private MessageAdapter messageAdapter;
    private MessageRepository messageRepository;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Récupération des données passées par l'intent
        conversationId = getIntent().getLongExtra("CONVERSATION_ID", -1L);
        String conversationName = getIntent().getStringExtra("CONVERSATION_NAME");
        int participantCount = getIntent().getIntExtra("PARTICIPANT_COUNT", 0);
        isGroupChat = participantCount > 2;

        // Initialisation
        tokenManager = new TokenManager(this);
        currentUserId = tokenManager.getUserId();
        messageRepository = new MessageRepository(this);

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

        observeMessages();
        buttonSend.setOnClickListener(v -> sendMessage());
    }

    private void setupRecyclerView() {
        // On passe bien les 3 arguments requis par notre adapter final
        messageAdapter = new MessageAdapter(currentUserId, isGroupChat, this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerViewMessages.setLayoutManager(layoutManager);
        recyclerViewMessages.setAdapter(messageAdapter);
    }

    private void observeMessages() {
        messageRepository.getMessagesForConversation(conversationId).observe(this, messageEntities -> {
            if (messageEntities != null) {
                // On utilise setMessages car notre adapter final est un RecyclerView.Adapter simple
                messageAdapter.setMessages(messageEntities);
                if (messageAdapter.getItemCount() > 0) {
                    recyclerViewMessages.scrollToPosition(messageAdapter.getItemCount() - 1);
                }
            }
        });
    }

    private void sendMessage() {
        String content = editTextMessage.getText().toString().trim();
        if (content.isEmpty()) return;

        messageRepository.sendMessage(conversationId, content);
        editTextMessage.setText("");
    }

    /**
     * Cette méthode de l'interface est appelée par l'adapter
     * lorsque l'utilisateur confirme la suppression d'un message.
     */
    @Override
    public void onDeleteMessage(MessageEntity message) {
        Toast.makeText(this, "Suppression...", Toast.LENGTH_SHORT).show();
        // On délègue la logique au repository. L'UI se mettra à jour via le LiveData.
        messageRepository.deleteMessage(message);
    }
}