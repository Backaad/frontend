package com.Mbuntu.MbuntuMobile.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Représente un message dans la base de données locale (Room).
 */
@Entity(tableName = "messages")
public class MessageEntity {

    // Clé primaire locale qui s'auto-incrémente.
    // C'est l'identifiant unique du message SUR LE TÉLÉPHONE.
    @PrimaryKey(autoGenerate = true)
    public long localId;

    // L'ID du message sur le serveur. Il peut être null au début
    // pour un message "optimiste" qui n'a pas encore été confirmé.
    public Long serverId;

    // L'ID de la conversation à laquelle ce message appartient.
    public long conversationId;

    // L'ID de l'expéditeur.
    public long senderId;

    // Le nom d'affichage de l'expéditeur.
    public String senderUsername;

    // Le contenu textuel du message.
    public String content;

    // Le timestamp du message. On le stocke en String (format ISO) pour simplifier.
    public String timestamp;

    // Le statut du message : "SENDING", "SENT", "FAILED".
    public String status;

    public static final String STATUS_SENDING = "SENDING";
    public static final String STATUS_SENT = "SENT";
    public static final String STATUS_FAILED = "FAILED";
}