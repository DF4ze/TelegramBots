package fr.ses10doigts.telegrambots.service.sender;

import fr.ses10doigts.telegrambots.model.TelegramView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SimpleTelegramSender {

    private final TelegramSenderRegistry senderRegistry;

    private TelegramSender resolveSender( String botId ) {
        return senderRegistry.getRequiredSender(botId);
    }

    public void sendMessage(String botName, Long chatId, String text) {
        resolveSender(botName).sendMessage(chatId, text);
    }

    public void sendMarkdownMessage(String botName, Long chatId, String text) {
        resolveSender(botName).sendMarkdownMessage(chatId, text);
    }

    public void sendMarkdownMessagePreservingLinks(String botName, Long chatId, String text) {
        resolveSender(botName).sendMarkdownMessagePreservingLinks(chatId, text);
    }

    public void sendPhoto(String botName, Long chatId, String photoPath, String caption) {
        resolveSender(botName).sendPhoto(chatId, photoPath, caption);
    }

    public void sendDocument(String botName, Long chatId, String documentPath, String caption) {
        resolveSender(botName).sendDocument(chatId, documentPath, caption);
    }

    public void sendView(String botName, Long chatId, TelegramView view) {
        resolveSender(botName).sendView(chatId, view);
    }

    public void answerCallbackQuery(String botName, String callbackQueryId) {
        resolveSender(botName).answerCallbackQuery(callbackQueryId);
    }
}
