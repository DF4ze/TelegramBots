package fr.ses10doigts.telegrambots.model;

import fr.ses10doigts.telegrambots.service.poller.ParsedCommand;
import lombok.AllArgsConstructor;
import lombok.Data;
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

    public static TelegramUpdateContext from( Update update ){
        if (!update.hasMessage()) {
            return null;
        }

        Message message = update.getMessage();
        if (message == null || !message.hasText()) {
            return null;
        }

        String text = message.getText();
        Long chatId = message.getChatId();
        Long userId = message.getFrom() != null ? message.getFrom().getId() : null;
        ParsedCommand parsed = ParsedCommand.parse(text);

        return new TelegramUpdateContext(
                update,
                message,
                chatId,
                userId,
                text,
                parsed.getCommand(),
                parsed.getArgsRaw(),
                parsed.getArgs(),
                false,
                null
        );
    }
}