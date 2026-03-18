package fr.ses10doigts.telegrambots.model;

import fr.ses10doigts.telegrambots.service.poller.ParsedCommand;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.List;

@Data
@AllArgsConstructor
public class TelegramUpdateContext {
    private Update update;
    private Message message;

    private Long chatId;
    private Long userId;

    private String text;

    private String command;
    private String commandArgsRaw;
    private List<String> args;

    private boolean callbackQuery;
    private String callbackData;

    public static TelegramUpdateContext from(Update update) {
        if (update == null) {
            return null;
        }

        Message message = null;
        Long chatId = null;
        Long userId = null;
        String text = null;
        String command = null;
        String argsRaw = null;
        List<String> args = List.of();
        boolean callback = false;
        String callbackData = null;

        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            if (callbackQuery == null) {
                return null;
            }

            callback = true;
            callbackData = callbackQuery.getData();

            if (callbackQuery.getFrom() != null) {
                userId = callbackQuery.getFrom().getId();
            }

            if (callbackQuery.getMessage() instanceof Message accessibleMessage) {
                message = accessibleMessage;
                chatId = accessibleMessage.getChatId();

                if (accessibleMessage.hasText()) {
                    text = accessibleMessage.getText();
                }
            }
        } else if (update.hasMessage()) {
            message = update.getMessage();
            if (message == null) {
                return null;
            }

            chatId = message.getChatId();

            if (message.getFrom() != null) {
                userId = message.getFrom().getId();
            }

            if (message.hasText()) {
                text = message.getText();
            }
        } else {
            return null;
        }

        if (text != null && !text.isBlank()) {
            ParsedCommand parsed = ParsedCommand.parse(text);
            command = parsed.getCommand();
            argsRaw = parsed.getArgsRaw();
            args = parsed.getArgs();
        }

        return new TelegramUpdateContext(
                update,
                message,
                chatId,
                userId,
                text,
                command,
                argsRaw,
                args,
                callback,
                callbackData
        );
    }
}