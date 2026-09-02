package fr.ses10doigts.telegrambots.service.sender;

import fr.ses10doigts.telegrambots.model.TelegramMessageReference;
import fr.ses10doigts.telegrambots.model.TelegramTypingAction;
import fr.ses10doigts.telegrambots.model.TelegramView;
import fr.ses10doigts.telegrambots.service.bot.CurrentTelegramBotContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ContextAwareTelegramSender implements TelegramSender {

    private final TelegramSenderRegistry senderRegistry;
    private final CurrentTelegramBotContext currentTelegramBotContext;

    private TelegramSender resolveCurrentSender() {
        String botId = currentTelegramBotContext.getCurrentBotId();

        if( botId == null ) {
            return senderRegistry.getDefaultBotSender();
        }else{
            return senderRegistry.getRequiredSender(botId);
        }
    }

    @Override
    public void sendMessage(Long chatId, String text) {
        resolveCurrentSender().sendMessage(chatId, text);
    }

    @Override
    public TelegramMessageReference sendMessageAndGetReference(Long chatId, String text) {
        return resolveCurrentSender().sendMessageAndGetReference(chatId, text);
    }

    @Override
    public void sendMarkdownMessage(Long chatId, String text) {
        resolveCurrentSender().sendMarkdownMessage(chatId, text);
    }

    @Override
    public TelegramMessageReference sendMarkdownMessageAndGetReference(Long chatId, String text) {
        return resolveCurrentSender().sendMarkdownMessageAndGetReference(chatId, text);
    }

    @Override
    public void sendMarkdownMessagePreservingLinks(Long chatId, String text) {
        resolveCurrentSender().sendMarkdownMessagePreservingLinks(chatId, text);
    }

    @Override
    public TelegramMessageReference sendMarkdownMessagePreservingLinksAndGetReference(Long chatId, String text) {
        return resolveCurrentSender().sendMarkdownMessagePreservingLinksAndGetReference(chatId, text);
    }

    @Override
    public void sendFormattedMessage(Long chatId, String markdownV2Text) {
        resolveCurrentSender().sendFormattedMessage(chatId, markdownV2Text);
    }

    @Override
    public TelegramMessageReference sendFormattedMessageAndGetReference(Long chatId, String markdownV2Text) {
        return resolveCurrentSender().sendFormattedMessageAndGetReference(chatId, markdownV2Text);
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
        resolveCurrentSender().sendView(chatId, view);
    }

    @Override
    public TelegramMessageReference sendViewAndGetReference(Long chatId, TelegramView view) {
        return resolveCurrentSender().sendViewAndGetReference(chatId, view);
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
        resolveCurrentSender().sendChatAction(chatId, action);
    }

    @Override
    public void sendTyping(Long chatId) {
        resolveCurrentSender().sendTyping(chatId);
    }

    @Override
    public void answerCallbackQuery(String callbackQueryId) {
        resolveCurrentSender().answerCallbackQuery(callbackQueryId);
    }
}
