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

    /**
     * Retourne les messages d'une conversation sous forme de LiveData.
     * Lance une synchronisation en arrière-plan pour récupérer les dernières mises à jour.
     */
    public LiveData<List<MessageEntity>> getMessagesForConversation(long conversationId) {
        refreshMessages(conversationId);
        return messageDao.getMessagesForConversation(conversationId);
    }

    /**
     * Contacte l'API pour récupérer l'historique des messages et met à jour la base de données locale.
     */
    private void refreshMessages(long conversationId) {
        String token = "Bearer " + tokenManager.getToken();
        apiService.getMessagesForConversation(token, conversationId).enqueue(new Callback<List<MessageResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<MessageResponse>> call, @NonNull Response<List<MessageResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Synchronisation réussie: " + response.body().size() + " messages reçus du serveur.");
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

    /**
     * Gère l'envoi d'un nouveau message en utilisant une approche "optimiste".
     */
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

        // On insère le message "en attente" dans la DB locale, ce qui met à jour l'UI.
        executorService.execute(() -> {
            // Note: Pour une version avancée, cette méthode 'insert' devrait retourner le 'localId'
            // pour pouvoir mettre à jour le bon message plus tard de manière garantie.
            messageDao.insertOrUpdateMessage(sendingMessage);
        });

        String token = "Bearer " + tokenManager.getToken();
        SendMessageRequest request = new SendMessageRequest(conversationId, content);

        apiService.sendMessage(token, request).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(@NonNull Call<MessageResponse> call, @NonNull Response<MessageResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // SUCCÈS : On met à jour le statut du message en "SENT".
                    // L'idéal serait de retrouver le message par un ID unique temporaire et de le mettre à jour.
                    // Pour l'instant, on se contente de savoir que ça a réussi.
                    Log.d(TAG, "Message envoyé avec succès et confirmé par le serveur.");
                    // On pourrait mettre à jour le statut ici si on avait l'ID local.
                } else {
                    // ÉCHEC Côté Serveur
                    Log.e(TAG, "Échec de l'envoi du message, code: " + response.code());
                    sendingMessage.status = MessageEntity.STATUS_FAILED;
                    executorService.execute(() -> messageDao.insertOrUpdateMessage(sendingMessage));
                }
            }

            @Override
            public void onFailure(@NonNull Call<MessageResponse> call, @NonNull Throwable t) {
                // ÉCHEC Côté Réseau
                Log.e(TAG, "Échec de l'envoi du message (réseau)", t);
                sendingMessage.status = MessageEntity.STATUS_FAILED;
                executorService.execute(() -> messageDao.insertOrUpdateMessage(sendingMessage));
            }
        });
    }

    /**
     * Insère un message reçu depuis le WebSocket dans la base de données locale.
     * @param messageResponse Le message tel que reçu du serveur.
     */
    public void insertMessageFromWebSocket(MessageResponse messageResponse) {
        executorService.execute(() -> {
            MessageEntity entity = mapResponseToEntity(messageResponse);
            messageDao.insertOrUpdateMessage(entity);
            Log.d(TAG, "Message WebSocket (ID serveur: " + entity.serverId + ") inséré dans la DB locale.");
        });
    }

    /**
     * Méthode utilitaire statique pour convertir un DTO réseau (MessageResponse)
     * en une entité de base de données locale (MessageEntity).
     */
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
        // Par défaut, un message mappé depuis une réponse serveur est considéré comme "envoyé".
        entity.status = MessageEntity.STATUS_SENT;
        return entity;
    }
}