package fr.ses10doigts.telegrambots.service.sender;

import fr.ses10doigts.telegrambots.model.TelegramView;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public interface TelegramSender {

    void sendMessage(Long chatId, String text);

    void sendMarkdownMessage(Long chatId, String text);

    void sendMarkdownMessagePreservingLinks(Long chatId, String text);

    void sendPhoto(Long chatId, String photoPath, String caption);

    void sendDocument(Long chatId, String documentPath, String caption);

    void sendView(Long chatId, TelegramView view);

    void answerCallbackQuery(String callbackQueryId);

}