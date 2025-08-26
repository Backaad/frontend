package com.Mbuntu.MbuntuMobile.websocket;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.Mbuntu.MbuntuMobile.api.models.MessageResponse;
import com.Mbuntu.MbuntuMobile.data.repository.MessageRepository;
import com.Mbuntu.MbuntuMobile.utils.TokenManager;
import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class WebSocketManager {
    private static final String TAG = "WebSocketManager";
    private static final String WEBSOCKET_URL = "ws://192.168.1.175:8080/MbuntuApi-0.0.1-SNAPSHOT/ws";

    private static WebSocketManager instance;
    private final OkHttpClient client;
    private WebSocket webSocket;

    // NOUVEAU : Le manager a besoin du Repository pour sauvegarder les messages entrants
    private MessageRepository messageRepository;
    private TokenManager tokenManager;
    private final Gson gson = new Gson();

    private WebSocketManager() {
        client = new OkHttpClient();
    }

    public static synchronized WebSocketManager getInstance() {
        if (instance == null) {
            instance = new WebSocketManager();
        }
        return instance;
    }

    // La méthode connect a maintenant besoin du contexte pour initialiser les managers
    public void connect(Context context, long userId) {
        if (webSocket != null) {
            return; // Déjà connecté
        }

        // Initialisation unique des managers
        if (this.messageRepository == null) {
            this.messageRepository = new MessageRepository(context.getApplicationContext());
        }
        if (this.tokenManager == null) {
            this.tokenManager = new TokenManager(context.getApplicationContext());
        }

        Log.d(TAG, "Tentative de connexion au WebSocket...");
        Request request = new Request.Builder().url(WEBSOCKET_URL).build();

        WebSocketListener internalListener = new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket ws, @NonNull Response response) {
                super.onOpen(ws, response);
                webSocket = ws;
                Log.d(TAG, "Connexion WebSocket ouverte !");
                webSocket.send("AUTH:" + userId);
            }

            @Override
            public void onMessage(@NonNull WebSocket ws, @NonNull String text) {
                super.onMessage(ws, text);
                Log.d(TAG, "Message reçu du serveur via WebSocket: " + text);

                try {
                    MessageResponse messageResponse = gson.fromJson(text, MessageResponse.class);
                    long currentUserId = tokenManager.getUserId();

                    // --- LOGIQUE CRUCIALE ANTI-DOUBLON ---
                    // On ne traite et sauvegarde le message que s'il ne vient PAS de nous.
                    // Nos propres messages sont gérés par le flux HTTP du MessageRepository.
                    if (messageResponse != null && messageResponse.getSenderId() != currentUserId) {
                        messageRepository.insertMessageFromWebSocket(messageResponse);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Erreur de traitement du message WebSocket", e);
                }
            }

            @Override
            public void onClosing(@NonNull WebSocket ws, int code, @NonNull String reason) { /* ... inchangé ... */ }

            @Override
            public void onFailure(@NonNull WebSocket ws, @NonNull Throwable t, @Nullable Response response) { /* ... inchangé ... */ }
        };

        client.newWebSocket(request, internalListener);
    }

    public void disconnect() { /* ... inchangé ... */ }
}