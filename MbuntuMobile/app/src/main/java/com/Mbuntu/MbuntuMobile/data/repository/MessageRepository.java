package com.Mbuntu.MbuntuMobile.data.repository;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.Mbuntu.MbuntuMobile.api.ApiService;
import com.Mbuntu.MbuntuMobile.api.RetrofitClient;
import com.Mbuntu.MbuntuMobile.api.models.MessageResponse;
import com.Mbuntu.MbuntuMobile.api.models.SendMessageRequest;
import com.Mbuntu.MbuntuMobile.data.local.AppDatabase;
import com.Mbuntu.MbuntuMobile.data.local.MessageDao;
import com.Mbuntu.MbuntuMobile.data.local.MessageEntity;
import com.Mbuntu.MbuntuMobile.utils.TokenManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageRepository {

    private static final String TAG = "MessageRepository";
    private final MessageDao messageDao;
    private final ApiService apiService;
    private final TokenManager tokenManager;
    private final ExecutorService executorService;

    public MessageRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        this.messageDao = db.messageDao();
        this.apiService = RetrofitClient.getApiService(context);
        this.tokenManager = new TokenManager(context);
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<MessageEntity>> getMessagesForConversation(long conversationId) {
        refreshMessages(conversationId);
        return messageDao.getMessagesForConversation(conversationId);
    }

    private void refreshMessages(long conversationId) {
        String token = "Bearer " + tokenManager.getToken();
        apiService.getMessagesForConversation(token, conversationId).enqueue(new Callback<List<MessageResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<MessageResponse>> call, @NonNull Response<List<MessageResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    executorService.execute(() -> {
                        List<MessageEntity> messageEntities = response.body().stream()
                                .map(MessageRepository::mapResponseToEntity)
                                .collect(Collectors.toList());
                        messageDao.insertAll(messageEntities);
                    });
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<MessageResponse>> call, @NonNull Throwable t) {
                Log.e(TAG, "Échec de la synchronisation des messages", t);
            }
        });
    }

    public void sendMessage(long conversationId, String content) {
        long currentUserId = tokenManager.getUserId();
        String currentUsername = tokenManager.getUsername();

        final MessageEntity sendingMessage = new MessageEntity();
        sendingMessage.conversationId = conversationId;
        sendingMessage.content = content;
        sendingMessage.senderId = currentUserId;
        sendingMessage.senderUsername = currentUsername;
        sendingMessage.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        sendingMessage.status = MessageEntity.STATUS_SENDING;

        executorService.execute(() -> {
            long localId = messageDao.insertOrUpdateMessage(sendingMessage);
            String token = "Bearer " + tokenManager.getToken();
            SendMessageRequest request = new SendMessageRequest(conversationId, content);
            try {
                Response<MessageResponse> response = apiService.sendMessage(token, request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    MessageEntity sentMessage = mapResponseToEntity(response.body());
                    sentMessage.localId = localId;
                    sentMessage.status = MessageEntity.STATUS_SENT;
                    messageDao.insertOrUpdateMessage(sentMessage);
                } else {
                    Log.e(TAG, "Échec de l'envoi du message, code: " + response.code());
                    sendingMessage.localId = localId;
                    sendingMessage.status = MessageEntity.STATUS_FAILED;
                    messageDao.insertOrUpdateMessage(sendingMessage);
                }
            } catch (Exception e) {
                Log.e(TAG, "Échec de l'envoi du message (réseau)", e);
                sendingMessage.localId = localId;
                sendingMessage.status = MessageEntity.STATUS_FAILED;
                messageDao.insertOrUpdateMessage(sendingMessage);
            }
        });
    }

    // --- NOUVELLE MÉTHODE DE SUPPRESSION ---
    public void deleteMessage(MessageEntity message) {
        if (message.serverId == null) {
            Log.e(TAG, "Impossible de supprimer un message qui n'a pas d'ID serveur.");
            // Si le message n'a pas d'ID serveur (il était en cours d'envoi),
            // on peut le supprimer localement.
            executorService.execute(() -> messageDao.deleteMessage(message));
            return;
        }

        // 1. UI Optimiste : Suppression immédiate de la base de données locale.
        executorService.execute(() -> messageDao.deleteMessage(message));

        // 2. Appel au serveur en arrière-plan pour la suppression réelle.
        String token = "Bearer " + tokenManager.getToken();
        apiService.deleteMessage(token, message.serverId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Message " + message.serverId + " supprimé avec succès sur le serveur.");
                } else {
                    Log.e(TAG, "Échec de la suppression sur le serveur pour le message " + message.serverId);
                    // Dans une vraie app, il faudrait gérer ce cas, par exemple en ré-insérant le message
                    // dans la base locale pour que l'utilisateur puisse réessayer.
                }
            }
            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e(TAG, "Échec réseau lors de la suppression du message " + message.serverId, t);
                // Gérer l'échec réseau.
            }
        });
    }

    public void insertMessageFromWebSocket(MessageResponse messageResponse) {
        executorService.execute(() -> {
            MessageEntity entity = mapResponseToEntity(messageResponse);
            messageDao.insertOrUpdateMessage(entity);
            Log.d(TAG, "Message WebSocket (ID serveur: " + entity.serverId + ") inséré dans la DB locale.");
        });
    }

    public static MessageEntity mapResponseToEntity(MessageResponse response) {
        MessageEntity entity = new MessageEntity();
        entity.serverId = response.getId();
        entity.conversationId = response.getConversationId();
        entity.senderId = response.getSenderId();
        entity.senderUsername = response.getSenderUsername();
        entity.content = response.getContent();
        if (response.getTimestamp() != null) {
            entity.timestamp = response.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        entity.status = MessageEntity.STATUS_SENT;
        return entity;
    }
}