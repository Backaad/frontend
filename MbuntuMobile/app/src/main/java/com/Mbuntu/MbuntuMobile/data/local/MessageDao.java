package com.Mbuntu.MbuntuMobile.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * Data Access Object pour la table des messages.
 * C'est ici que l'on définit toutes les opérations sur la base de données.
 */
@Dao
public interface MessageDao {

    // Insère un nouveau message. Si un message avec le même ID existe, il le remplace.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdateMessage(MessageEntity message);

    // Insère une liste de messages (utile pour la synchronisation).
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<MessageEntity> messages);

    // Récupère tous les messages d'une conversation, triés par date.
    // Le retour est un LiveData, ce qui signifie que l'UI sera notifiée
    // automatiquement à chaque changement dans cette liste.
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    LiveData<List<MessageEntity>> getMessagesForConversation(long conversationId);

    // Supprime tous les messages (utile pour une réinitialisation).
    @Query("DELETE FROM messages")
    void deleteAllMessages();
}