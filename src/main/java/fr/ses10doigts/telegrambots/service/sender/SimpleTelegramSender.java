package fr.ses10doigts.telegrambots.service.sender;

import fr.ses10doigts.telegrambots.model.TelegramMessageReference;
import fr.ses10doigts.telegrambots.model.TelegramTypingAction;
import fr.ses10doigts.telegrambots.model.TelegramView;
import lombok.RequiredArgsConstructor;

/**
 * Façade simplifiée pour l'envoi de messages Telegram par bot nommé.
 *
 * <p>Délègue à {@link TelegramSenderRegistry} le routage vers le bon bot.
 * Ce composant est instancié par {@code TelegramAutoConfiguration} — pas via
 * {@code @Service} / component-scan, ce qui le rendrait invisible aux applications
 * consommatrices dont le scan ne couvre pas ce package.</p>
 */
@RequiredArgsConstructor
public class SimpleTelegramSender {

    private final TelegramSenderRegistry senderRegistry;

    private TelegramSender resolveSender( String botId ) {
        return senderRegistry.getRequiredSender(botId);
    }

    public void sendMessage(String botName, Long chatId, String text) {
        resolveSender(botName).sendMessage(chatId, text);
    }

    public TelegramMessageReference sendMessageAndGetReference(String botName, Long chatId, String text) {
        return resolveSender(botName).sendMessageAndGetReference(chatId, text);
    }

    public void sendMarkdownMessage(String botName, Long chatId, String text) {
        resolveSender(botName).sendMarkdownMessage(chatId, text);
    }

    public TelegramMessageReference sendMarkdownMessageAndGetReference(String botName, Long chatId, String text) {
        return resolveSender(botName).sendMarkdownMessageAndGetReference(chatId, text);
    }

    public void sendMarkdownMessagePreservingLinks(String botName, Long chatId, String text) {
        resolveSender(botName).sendMarkdownMessagePreservingLinks(chatId, text);
    }

    public TelegramMessageReference sendMarkdownMessagePreservingLinksAndGetReference(String botName, Long chatId, String text) {
        return resolveSender(botName).sendMarkdownMessagePreservingLinksAndGetReference(chatId, text);
    }

    public void sendPhoto(String botName, Long chatId, String photoPath, String caption) {
        resolveSender(botName).sendPhoto(chatId, photoPath, caption);
    }

    public TelegramMessageReference sendPhotoAndGetReference(String botName, Long chatId, String photoPath, String caption) {
        return resolveSender(botName).sendPhotoAndGetReference(chatId, photoPath, caption);
    }

    public void sendTextDocument(String botName, Long chatId, String content, String fileName, String caption) {
        resolveSender(botName).sendTextDocument(chatId, content, fileName, caption);
    }

    public TelegramMessageReference sendTextDocumentAndGetReference(String botName, Long chatId, String content, String fileName, String caption) {
        return resolveSender(botName).sendTextDocumentAndGetReference(chatId, content, fileName, caption);
    }

    public void sendDocument(String botName, Long chatId, String documentPath, String caption) {
        resolveSender(botName).sendDocument(chatId, documentPath, caption);
    }

    public TelegramMessageReference sendDocumentAndGetReference(String botName, Long chatId, String documentPath, String caption) {
        return resolveSender(botName).sendDocumentAndGetReference(chatId, documentPath, caption);
    }

    public void sendView(String botName, Long chatId, TelegramView view) {
        resolveSender(botName).sendView(chatId, view);
    }

    public TelegramMessageReference sendViewAndGetReference(String botName, Long chatId, TelegramView view) {
        return resolveSender(botName).sendViewAndGetReference(chatId, view);
    }

    public TelegramMessageReference editMessage(String botName, Long chatId, Integer messageId, String text) {
        return resolveSender(botName).editMessage(chatId, messageId, text);
    }

    public TelegramMessageReference editMarkdownMessage(String botName, Long chatId, Integer messageId, String text) {
        return resolveSender(botName).editMarkdownMessage(chatId, messageId, text);
    }

    public TelegramMessageReference editMarkdownMessagePreservingLinks(String botName, Long chatId, Integer messageId, String text) {
        return resolveSender(botName).editMarkdownMessagePreservingLinks(chatId, messageId, text);
    }

    public TelegramMessageReference editView(String botName, Long chatId, Integer messageId, TelegramView view) {
        return resolveSender(botName).editView(chatId, messageId, view);
    }

    public boolean deleteMessage(String botName, Long chatId, Integer messageId) {
        return resolveSender(botName).deleteMessage(chatId, messageId);
    }

    public void sendChatAction(String botName, Long chatId, TelegramTypingAction action) {
        resolveSender(botName).sendChatAction(chatId, action);
    }

    public void sendTyping(String botName, Long chatId) {
        resolveSender(botName).sendTyping(chatId);
    }

    public void answerCallbackQuery(String botName, String callbackQueryId) {
        resolveSender(botName).answerCallbackQuery(callbackQueryId);
    }
}
