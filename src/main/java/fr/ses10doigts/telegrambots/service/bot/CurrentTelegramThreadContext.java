package fr.ses10doigts.telegrambots.service.bot;

/**
 * Sujet (topic) Telegram courant, lié au thread d'exécution.
 *
 * <p>Même principe que {@link CurrentTelegramBotContext}, mais pour le
 * {@code message_thread_id} du sujet dans lequel une commande a été reçue.</p>
 *
 * <p>Positionné par {@code TelegramUpdateDispatcher} avant l'invocation d'un
 * handler (uniquement lorsque le message reçu appartient réellement à un sujet
 * nommé, pas au sujet "Général") et consommé par {@code ContextAwareTelegramSender}
 * pour que les méthodes d'envoi "historiques" (sans paramètre de thread explicite)
 * répondent automatiquement dans le bon sujet.</p>
 */
public class CurrentTelegramThreadContext {

    private static final ThreadLocal<Integer> CURRENT_MESSAGE_THREAD_ID = new ThreadLocal<>();

    public void setCurrentMessageThreadId(Integer messageThreadId) {
        if (messageThreadId == null) {
            CURRENT_MESSAGE_THREAD_ID.remove();
        } else {
            CURRENT_MESSAGE_THREAD_ID.set(messageThreadId);
        }
    }

    /**
     * @return le {@code message_thread_id} du sujet courant, ou {@code null} si
     *         l'exécution en cours n'est pas liée à un sujet précis (message hors
     *         forum, sujet "Général", job planifié, etc.).
     */
    public Integer getCurrentMessageThreadId() {
        return CURRENT_MESSAGE_THREAD_ID.get();
    }

    public void clear() {
        CURRENT_MESSAGE_THREAD_ID.remove();
    }
}
