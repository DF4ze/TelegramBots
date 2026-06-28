package fr.ses10doigts.telegrambots.service.sender;

import fr.ses10doigts.telegrambots.model.TelegramMessageReference;
import fr.ses10doigts.telegrambots.model.TelegramTypingAction;
import fr.ses10doigts.telegrambots.model.TelegramView;

public interface TelegramSender {

    /**
     * Envoie un message texte simple sans exposer son identifiant.
     */
    void sendMessage(Long chatId, String text);

    /**
     * Envoie un message texte simple et retourne sa référence Telegram.
     */
    TelegramMessageReference sendMessageAndGetReference(Long chatId, String text);

    /**
     * Envoie un message Markdown V2 échappé sans exposer son identifiant.
     */
    void sendMarkdownMessage(Long chatId, String text);

    /**
     * Envoie un message Markdown V2 échappé et retourne sa référence Telegram.
     */
    TelegramMessageReference sendMarkdownMessageAndGetReference(Long chatId, String text);

    /**
     * Envoie un message Markdown V2 en préservant les liens sans exposer son identifiant.
     */
    void sendMarkdownMessagePreservingLinks(Long chatId, String text);

    /**
     * Envoie un message Markdown V2 en préservant les liens et retourne sa référence Telegram.
     */
    TelegramMessageReference sendMarkdownMessagePreservingLinksAndGetReference(Long chatId, String text);

    /**
     * Envoie une photo sans exposer son identifiant.
     */
    void sendPhoto(Long chatId, String photoPath, String caption);

    /**
     * Envoie une photo et retourne sa référence Telegram.
     */
    TelegramMessageReference sendPhotoAndGetReference(Long chatId, String photoPath, String caption);

    /**
     * Envoie un document texte généré à la volée sans exposer son identifiant.
     */
    void sendTextDocument(Long chatId, String content, String fileName, String caption);

    /**
     * Envoie un document texte généré à la volée et retourne sa référence Telegram.
     */
    TelegramMessageReference sendTextDocumentAndGetReference(Long chatId, String content, String fileName, String caption);

    /**
     * Envoie un document sans exposer son identifiant.
     */
    void sendDocument(Long chatId, String documentPath, String caption);

    /**
     * Envoie un document et retourne sa référence Telegram.
     */
    TelegramMessageReference sendDocumentAndGetReference(Long chatId, String documentPath, String caption);

    /**
     * Envoie une vue Telegram sans exposer son identifiant.
     */
    void sendView(Long chatId, TelegramView view);

    /**
     * Envoie une vue Telegram et retourne sa référence Telegram.
     */
    TelegramMessageReference sendViewAndGetReference(Long chatId, TelegramView view);

    /**
     * Édite le texte d'un message existant.
     */
    TelegramMessageReference editMessage(Long chatId, Integer messageId, String text);

    /**
     * Édite le texte Markdown V2 d'un message existant.
     */
    TelegramMessageReference editMarkdownMessage(Long chatId, Integer messageId, String text);

    /**
     * Édite le texte Markdown V2 d'un message existant en préservant les liens.
     */
    TelegramMessageReference editMarkdownMessagePreservingLinks(Long chatId, Integer messageId, String text);

    /**
     * Édite une vue Telegram existante.
     */
    TelegramMessageReference editView(Long chatId, Integer messageId, TelegramView view);

    /**
     * Supprime un message existant.
     */
    boolean deleteMessage(Long chatId, Integer messageId);

    /**
     * Envoie une action de présence Telegram.
     */
    void sendChatAction(Long chatId, TelegramTypingAction action);

    /**
     * Déclenche l'indication "est en train d'écrire".
     */
    void sendTyping(Long chatId);

    /**
     * Acquitte une callback query Telegram.
     */
    void answerCallbackQuery(String callbackQueryId);

}
