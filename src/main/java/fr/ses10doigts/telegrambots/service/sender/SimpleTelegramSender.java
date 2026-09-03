package fr.ses10doigts.telegrambots.service.sender;

import fr.ses10doigts.telegrambots.model.TelegramForumAccessCheck;
import fr.ses10doigts.telegrambots.model.TelegramForumTopic;
import fr.ses10doigts.telegrambots.model.TelegramMessageReference;
import fr.ses10doigts.telegrambots.model.TelegramTopicIconColor;
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

    public void sendMessage(String botName, Long chatId, Integer messageThreadId, String text) {
        resolveSender(botName).sendMessage(chatId, messageThreadId, text);
    }

    public TelegramMessageReference sendMessageAndGetReference(String botName, Long chatId, Integer messageThreadId, String text) {
        return resolveSender(botName).sendMessageAndGetReference(chatId, messageThreadId, text);
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

    public void sendFormattedMessage(String botName, Long chatId, String markdownV2Text) {
        resolveSender(botName).sendFormattedMessage(chatId, markdownV2Text);
    }

    public TelegramMessageReference sendFormattedMessageAndGetReference(String botName, Long chatId, String markdownV2Text) {
        return resolveSender(botName).sendFormattedMessageAndGetReference(chatId, markdownV2Text);
    }

    public TelegramMessageReference editFormattedMessage(String botName, Long chatId, Integer messageId, String markdownV2Text) {
        return resolveSender(botName).editFormattedMessage(chatId, messageId, markdownV2Text);
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

    public void sendView(String botName, Long chatId, Integer messageThreadId, TelegramView view) {
        resolveSender(botName).sendView(chatId, messageThreadId, view);
    }

    public TelegramMessageReference sendViewAndGetReference(String botName, Long chatId, Integer messageThreadId, TelegramView view) {
        return resolveSender(botName).sendViewAndGetReference(chatId, messageThreadId, view);
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

    public void sendChatAction(String botName, Long chatId, Integer messageThreadId, TelegramTypingAction action) {
        resolveSender(botName).sendChatAction(chatId, messageThreadId, action);
    }

    public void sendTyping(String botName, Long chatId) {
        resolveSender(botName).sendTyping(chatId);
    }

    public void sendTyping(String botName, Long chatId, Integer messageThreadId) {
        resolveSender(botName).sendTyping(chatId, messageThreadId);
    }

    public void answerCallbackQuery(String botName, String callbackQueryId) {
        resolveSender(botName).answerCallbackQuery(callbackQueryId);
    }

    public TelegramForumAccessCheck checkForumAccess(String botName, Long chatId) {
        return resolveSender(botName).checkForumAccess(chatId);
    }

    public boolean canManageForumTopics(String botName, Long chatId) {
        return resolveSender(botName).canManageForumTopics(chatId);
    }

    public TelegramForumTopic createForumTopic(String botName, Long chatId, String name) {
        return resolveSender(botName).createForumTopic(chatId, name);
    }

    public TelegramForumTopic createForumTopic(
            String botName,
            Long chatId,
            String name,
            TelegramTopicIconColor iconColor,
            String iconCustomEmojiId
    ) {
        return resolveSender(botName).createForumTopic(chatId, name, iconColor, iconCustomEmojiId);
    }

    public boolean editForumTopic(String botName, Long chatId, Integer messageThreadId, String name, String iconCustomEmojiId) {
        return resolveSender(botName).editForumTopic(chatId, messageThreadId, name, iconCustomEmojiId);
    }

    public boolean closeForumTopic(String botName, Long chatId, Integer messageThreadId) {
        return resolveSender(botName).closeForumTopic(chatId, messageThreadId);
    }

    public boolean reopenForumTopic(String botName, Long chatId, Integer messageThreadId) {
        return resolveSender(botName).reopenForumTopic(chatId, messageThreadId);
    }

    public boolean deleteForumTopic(String botName, Long chatId, Integer messageThreadId) {
        return resolveSender(botName).deleteForumTopic(chatId, messageThreadId);
    }

    public boolean unpinAllForumTopicMessages(String botName, Long chatId, Integer messageThreadId) {
        return resolveSender(botName).unpinAllForumTopicMessages(chatId, messageThreadId);
    }
}
