package fr.ses10doigts.telegrambots.service.sender;

import fr.ses10doigts.telegrambots.model.TelegramForumAccessCheck;
import fr.ses10doigts.telegrambots.model.TelegramForumTopic;
import fr.ses10doigts.telegrambots.model.TelegramMessageReference;
import fr.ses10doigts.telegrambots.model.TelegramTopicIconColor;
import fr.ses10doigts.telegrambots.model.TelegramTypingAction;
import fr.ses10doigts.telegrambots.model.TelegramView;
import fr.ses10doigts.telegrambots.service.bot.CurrentTelegramBotContext;
import fr.ses10doigts.telegrambots.service.bot.CurrentTelegramThreadContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ContextAwareTelegramSender implements TelegramSender {

    private final TelegramSenderRegistry senderRegistry;
    private final CurrentTelegramBotContext currentTelegramBotContext;
    private final CurrentTelegramThreadContext currentTelegramThreadContext;

    private TelegramSender resolveCurrentSender() {
        String botId = currentTelegramBotContext.getCurrentBotId();

        if( botId == null ) {
            return senderRegistry.getDefaultBotSender();
        }else{
            return senderRegistry.getRequiredSender(botId);
        }
    }

    /**
     * Sujet (topic) courant, résolu depuis {@link CurrentTelegramThreadContext} —
     * {@code null} si l'exécution en cours n'est pas liée à un sujet précis.
     */
    private Integer resolveCurrentMessageThreadId() {
        return currentTelegramThreadContext.getCurrentMessageThreadId();
    }

    @Override
    public void sendMessage(Long chatId, String text) {
        resolveCurrentSender().sendMessage(chatId, resolveCurrentMessageThreadId(), text);
    }

    @Override
    public TelegramMessageReference sendMessageAndGetReference(Long chatId, String text) {
        return resolveCurrentSender().sendMessageAndGetReference(chatId, resolveCurrentMessageThreadId(), text);
    }

    @Override
    public void sendMessage(Long chatId, Integer messageThreadId, String text) {
        resolveCurrentSender().sendMessage(chatId, messageThreadId, text);
    }

    @Override
    public TelegramMessageReference sendMessageAndGetReference(Long chatId, Integer messageThreadId, String text) {
        return resolveCurrentSender().sendMessageAndGetReference(chatId, messageThreadId, text);
    }

    @Override
    public void sendMarkdownMessage(Long chatId, String text) {
        resolveCurrentSender().sendMarkdownMessage(chatId, resolveCurrentMessageThreadId(), text);
    }

    @Override
    public TelegramMessageReference sendMarkdownMessageAndGetReference(Long chatId, String text) {
        return resolveCurrentSender().sendMarkdownMessageAndGetReference(chatId, resolveCurrentMessageThreadId(), text);
    }

    @Override
    public void sendMarkdownMessage(Long chatId, Integer messageThreadId, String text) {
        resolveCurrentSender().sendMarkdownMessage(chatId, messageThreadId, text);
    }

    @Override
    public TelegramMessageReference sendMarkdownMessageAndGetReference(Long chatId, Integer messageThreadId, String text) {
        return resolveCurrentSender().sendMarkdownMessageAndGetReference(chatId, messageThreadId, text);
    }

    @Override
    public void sendMarkdownMessagePreservingLinks(Long chatId, String text) {
        resolveCurrentSender().sendMarkdownMessagePreservingLinks(chatId, resolveCurrentMessageThreadId(), text);
    }

    @Override
    public TelegramMessageReference sendMarkdownMessagePreservingLinksAndGetReference(Long chatId, String text) {
        return resolveCurrentSender().sendMarkdownMessagePreservingLinksAndGetReference(chatId, resolveCurrentMessageThreadId(), text);
    }

    @Override
    public void sendMarkdownMessagePreservingLinks(Long chatId, Integer messageThreadId, String text) {
        resolveCurrentSender().sendMarkdownMessagePreservingLinks(chatId, messageThreadId, text);
    }

    @Override
    public TelegramMessageReference sendMarkdownMessagePreservingLinksAndGetReference(Long chatId, Integer messageThreadId, String text) {
        return resolveCurrentSender().sendMarkdownMessagePreservingLinksAndGetReference(chatId, messageThreadId, text);
    }

    @Override
    public void sendFormattedMessage(Long chatId, String markdownV2Text) {
        resolveCurrentSender().sendFormattedMessage(chatId, resolveCurrentMessageThreadId(), markdownV2Text);
    }

    @Override
    public TelegramMessageReference sendFormattedMessageAndGetReference(Long chatId, String markdownV2Text) {
        return resolveCurrentSender().sendFormattedMessageAndGetReference(chatId, resolveCurrentMessageThreadId(), markdownV2Text);
    }

    @Override
    public void sendFormattedMessage(Long chatId, Integer messageThreadId, String markdownV2Text) {
        resolveCurrentSender().sendFormattedMessage(chatId, messageThreadId, markdownV2Text);
    }

    @Override
    public TelegramMessageReference sendFormattedMessageAndGetReference(Long chatId, Integer messageThreadId, String markdownV2Text) {
        return resolveCurrentSender().sendFormattedMessageAndGetReference(chatId, messageThreadId, markdownV2Text);
    }

    @Override
    public TelegramMessageReference editFormattedMessage(Long chatId, Integer messageId, String markdownV2Text) {
        return resolveCurrentSender().editFormattedMessage(chatId, messageId, markdownV2Text);
    }

    @Override
    public void sendPhoto(Long chatId, String photoPath, String caption) {
        resolveCurrentSender().sendPhoto(chatId, photoPath, caption);
    }

    @Override
    public TelegramMessageReference sendPhotoAndGetReference(Long chatId, String photoPath, String caption) {
        return resolveCurrentSender().sendPhotoAndGetReference(chatId, photoPath, caption);
    }

    @Override
    public void sendTextDocument(Long chatId, String content, String fileName, String caption) {
        resolveCurrentSender().sendTextDocument(chatId, content, fileName, caption);
    }

    @Override
    public TelegramMessageReference sendTextDocumentAndGetReference(Long chatId, String content, String fileName, String caption) {
        return resolveCurrentSender().sendTextDocumentAndGetReference(chatId, content, fileName, caption);
    }

    @Override
    public void sendDocument(Long chatId, String documentPath, String caption) {
        resolveCurrentSender().sendDocument(chatId, documentPath, caption);
    }

    @Override
    public TelegramMessageReference sendDocumentAndGetReference(Long chatId, String documentPath, String caption) {
        return resolveCurrentSender().sendDocumentAndGetReference(chatId, documentPath, caption);
    }

    @Override
    public void sendView(Long chatId, TelegramView view) {
        resolveCurrentSender().sendView(chatId, resolveCurrentMessageThreadId(), view);
    }

    @Override
    public TelegramMessageReference sendViewAndGetReference(Long chatId, TelegramView view) {
        return resolveCurrentSender().sendViewAndGetReference(chatId, resolveCurrentMessageThreadId(), view);
    }

    @Override
    public void sendView(Long chatId, Integer messageThreadId, TelegramView view) {
        resolveCurrentSender().sendView(chatId, messageThreadId, view);
    }

    @Override
    public TelegramMessageReference sendViewAndGetReference(Long chatId, Integer messageThreadId, TelegramView view) {
        return resolveCurrentSender().sendViewAndGetReference(chatId, messageThreadId, view);
    }

    @Override
    public TelegramMessageReference editMessage(Long chatId, Integer messageId, String text) {
        return resolveCurrentSender().editMessage(chatId, messageId, text);
    }

    @Override
    public TelegramMessageReference editMarkdownMessage(Long chatId, Integer messageId, String text) {
        return resolveCurrentSender().editMarkdownMessage(chatId, messageId, text);
    }

    @Override
    public TelegramMessageReference editMarkdownMessagePreservingLinks(Long chatId, Integer messageId, String text) {
        return resolveCurrentSender().editMarkdownMessagePreservingLinks(chatId, messageId, text);
    }

    @Override
    public TelegramMessageReference editView(Long chatId, Integer messageId, TelegramView view) {
        return resolveCurrentSender().editView(chatId, messageId, view);
    }

    @Override
    public boolean deleteMessage(Long chatId, Integer messageId) {
        return resolveCurrentSender().deleteMessage(chatId, messageId);
    }

    @Override
    public void sendChatAction(Long chatId, TelegramTypingAction action) {
        resolveCurrentSender().sendChatAction(chatId, resolveCurrentMessageThreadId(), action);
    }

    @Override
    public void sendChatAction(Long chatId, Integer messageThreadId, TelegramTypingAction action) {
        resolveCurrentSender().sendChatAction(chatId, messageThreadId, action);
    }

    @Override
    public void sendTyping(Long chatId) {
        resolveCurrentSender().sendTyping(chatId, resolveCurrentMessageThreadId());
    }

    @Override
    public void sendTyping(Long chatId, Integer messageThreadId) {
        resolveCurrentSender().sendTyping(chatId, messageThreadId);
    }

    @Override
    public void answerCallbackQuery(String callbackQueryId) {
        resolveCurrentSender().answerCallbackQuery(callbackQueryId);
    }

    @Override
    public TelegramForumAccessCheck checkForumAccess(Long chatId) {
        return resolveCurrentSender().checkForumAccess(chatId);
    }

    @Override
    public boolean canManageForumTopics(Long chatId) {
        return resolveCurrentSender().canManageForumTopics(chatId);
    }

    @Override
    public TelegramForumTopic createForumTopic(Long chatId, String name) {
        return resolveCurrentSender().createForumTopic(chatId, name);
    }

    @Override
    public TelegramForumTopic createForumTopic(
            Long chatId,
            String name,
            TelegramTopicIconColor iconColor,
            String iconCustomEmojiId
    ) {
        return resolveCurrentSender().createForumTopic(chatId, name, iconColor, iconCustomEmojiId);
    }

    @Override
    public boolean editForumTopic(Long chatId, Integer messageThreadId, String name, String iconCustomEmojiId) {
        return resolveCurrentSender().editForumTopic(chatId, messageThreadId, name, iconCustomEmojiId);
    }

    @Override
    public boolean closeForumTopic(Long chatId, Integer messageThreadId) {
        return resolveCurrentSender().closeForumTopic(chatId, messageThreadId);
    }

    @Override
    public boolean reopenForumTopic(Long chatId, Integer messageThreadId) {
        return resolveCurrentSender().reopenForumTopic(chatId, messageThreadId);
    }

    @Override
    public boolean deleteForumTopic(Long chatId, Integer messageThreadId) {
        return resolveCurrentSender().deleteForumTopic(chatId, messageThreadId);
    }

    @Override
    public boolean unpinAllForumTopicMessages(Long chatId, Integer messageThreadId) {
        return resolveCurrentSender().unpinAllForumTopicMessages(chatId, messageThreadId);
    }
}
