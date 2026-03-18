package fr.ses10doigts.telegrambots.service.sender;

import fr.ses10doigts.telegrambots.model.TelegramView;
import fr.ses10doigts.telegrambots.service.bot.CurrentTelegramBotContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ContextAwareTelegramSender implements TelegramSender {

    private final TelegramSenderRegistry senderRegistry;
    private final CurrentTelegramBotContext currentTelegramBotContext;

    private TelegramSender resolveCurrentSender() {
        String botId = currentTelegramBotContext.getRequiredCurrentBotId();
        return senderRegistry.getRequiredSender(botId);
    }

    @Override
    public void sendMessage(Long chatId, String text) {
        resolveCurrentSender().sendMessage(chatId, text);
    }

    @Override
    public void sendMarkdownMessage(Long chatId, String text) {
        resolveCurrentSender().sendMarkdownMessage(chatId, text);
    }

    @Override
    public void sendMarkdownMessagePreservingLinks(Long chatId, String text) {
        resolveCurrentSender().sendMarkdownMessagePreservingLinks(chatId, text);
    }

    @Override
    public void sendPhoto(Long chatId, String photoPath, String caption) {
        resolveCurrentSender().sendPhoto(chatId, photoPath, caption);
    }

    @Override
    public void sendDocument(Long chatId, String documentPath, String caption) {
        resolveCurrentSender().sendDocument(chatId, documentPath, caption);
    }

    @Override
    public void sendView(Long chatId, TelegramView view) {
        resolveCurrentSender().sendView(chatId, view);
    }

    @Override
    public void answerCallbackQuery(String callbackQueryId) {
        resolveCurrentSender().answerCallbackQuery(callbackQueryId);
    }
}