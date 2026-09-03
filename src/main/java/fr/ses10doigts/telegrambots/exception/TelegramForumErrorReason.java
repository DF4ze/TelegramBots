package fr.ses10doigts.telegrambots.exception;

/**
 * Cause probable d'un échec d'appel lié aux sujets (topics) d'un forum Telegram.
 *
 * <p>Déduite au mieux du message d'erreur renvoyé par l'API Telegram (celle-ci ne
 * fournit pas de code d'erreur structuré dédié) : à traiter comme une aide au
 * diagnostic, pas comme une garantie absolue.</p>
 */
public enum TelegramForumErrorReason {

    /**
     * Le groupe ciblé n'est pas un supergroupe avec "Sujets" activés
     * ({@code chat.is_forum} vaut {@code false}).
     */
    NOT_A_FORUM,

    /**
     * Le bot n'a pas les droits d'administrateur requis dans ce groupe
     * ({@code can_manage_topics} et/ou {@code can_delete_messages}).
     */
    MISSING_RIGHTS,

    /**
     * Le sujet ciblé n'existe pas (ou plus).
     */
    TOPIC_NOT_FOUND,

    /**
     * Toute autre erreur renvoyée par l'API Telegram.
     */
    API_ERROR
}
