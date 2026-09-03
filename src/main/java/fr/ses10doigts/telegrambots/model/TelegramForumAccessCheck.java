package fr.ses10doigts.telegrambots.model;

import lombok.Builder;
import lombok.Value;

/**
 * Résultat d'une vérification préalable des conditions requises pour gérer les
 * sujets (topics) d'un groupe Telegram donné.
 *
 * <p>Permet à un bot consommateur d'afficher un message d'erreur clair avant même
 * de tenter un appel Telegram, plutôt que de laisser remonter une
 * {@link fr.ses10doigts.telegrambots.exception.TelegramForumException}.</p>
 *
 * @see TelegramSender#checkForumAccess
 */
@Value
@Builder
public class TelegramForumAccessCheck {

    /**
     * {@code true} si le groupe est un supergroupe avec "Sujets" activés
     * ({@code chat.is_forum}). Cette bascule est faite manuellement par un
     * administrateur humain dans les paramètres du groupe : aucun appel de l'API
     * Bot Telegram ne permet de l'activer depuis un bot.
     */
    boolean forum;

    /**
     * {@code true} si le bot est administrateur du groupe avec le droit
     * "Gérer les sujets" ({@code can_manage_topics}), nécessaire pour créer, éditer,
     * fermer ou rouvrir un sujet.
     */
    boolean canManageTopics;

    /**
     * {@code true} si le bot est administrateur du groupe avec le droit
     * "Supprimer les messages" ({@code can_delete_messages}), nécessaire en plus de
     * {@link #canManageTopics} pour supprimer un sujet.
     */
    boolean canDeleteMessages;

    /**
     * @return {@code true} si le bot peut créer/éditer/fermer/rouvrir des sujets
     *         dans ce groupe.
     */
    public boolean canManageForumTopics() {
        return forum && canManageTopics;
    }

    /**
     * @return {@code true} si le bot peut supprimer des sujets dans ce groupe.
     */
    public boolean canDeleteForumTopics() {
        return forum && canDeleteMessages;
    }
}
