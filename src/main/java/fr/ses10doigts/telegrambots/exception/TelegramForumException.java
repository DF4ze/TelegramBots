package fr.ses10doigts.telegrambots.exception;

/**
 * Levée lorsqu'un appel lié aux sujets (topics) d'un forum Telegram échoue :
 * création, édition, fermeture, réouverture, suppression, etc.
 *
 * <p>Contrairement au reste de {@code TelegramSender} (qui journalise et renvoie
 * silencieusement {@code null}/{@code false} en cas d'erreur), les méthodes de
 * gestion des sujets lèvent cette exception explicite : ce sont des actions
 * intentionnelles côté appelant (créer/supprimer un sujet), dont l'échec doit
 * pouvoir être distingué et traité au cas par cas plutôt qu'ignoré.</p>
 */
public class TelegramForumException extends RuntimeException {

    private final TelegramForumErrorReason reason;

    public TelegramForumException(TelegramForumErrorReason reason, String message, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }

    public TelegramForumException(TelegramForumErrorReason reason, String message) {
        this(reason, message, null);
    }

    /**
     * @return la cause probable de l'échec, déduite au mieux de la réponse Telegram.
     */
    public TelegramForumErrorReason getReason() {
        return reason;
    }
}
