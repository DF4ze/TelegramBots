package fr.ses10doigts.telegrambots.model;

import lombok.Builder;
import lombok.Value;

/**
 * Représente un sujet (topic) d'un forum Telegram (supergroupe avec "Sujets" activés).
 *
 * <p>Telegram n'expose aucun champ de description/sous-titre pour un sujet : seuls
 * un titre ({@code name}) et une icône (couleur unie et/ou emoji personnalisé) sont
 * disponibles.</p>
 */
@Value
@Builder
public class TelegramForumTopic {

    /**
     * Identifiant du sujet. C'est aussi l'identifiant du message qui a servi à créer
     * le sujet, utilisé comme {@code message_thread_id} pour cibler ce sujet dans les
     * autres appels (envoi de message, édition, fermeture, suppression...).
     */
    Integer messageThreadId;

    /**
     * Titre du sujet (1 à 128 caractères).
     */
    String name;

    /**
     * Couleur unie de l'icône, ou {@code null} si Telegram a renvoyé une valeur RGB
     * qui ne correspond à aucune couleur connue de {@link TelegramTopicIconColor}
     * (nouvelle couleur ajoutée côté Telegram, par exemple).
     */
    TelegramTopicIconColor iconColor;

    /**
     * Identifiant de l'emoji personnalisé utilisé comme icône, ou {@code null} si le
     * sujet utilise uniquement la couleur unie.
     */
    String iconCustomEmojiId;
}
