package fr.ses10doigts.telegrambots.model;

import fr.ses10doigts.telegrambots.service.poller.command.ParsedCommand;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.List;

@Data
@AllArgsConstructor
public class TelegramUpdateContext {
    private final String botId;

    private Update update;
    private Message message;
    private Integer messageId;

    /**
     * Identifiant du sujet (topic) de forum dans lequel le message a été reçu, ou
     * {@code null} si le message ne provient pas d'un sujet nommé (chat classique,
     * ou sujet "Général" d'un forum).
     */
    private Integer messageThreadId;

    /**
     * {@code true} si le message provient réellement d'un sujet nommé d'un forum
     * (équivalent à {@code messageThreadId != null}).
     */
    private boolean topicMessage;

    private Long chatId;
    private Long userId;

    private String text;

    private String command;
    private String commandArgsRaw;
    private List<String> args;

    private boolean callbackQuery;
    private String callbackData;

    public static TelegramUpdateContext from(Update update, String botId) {
        if (update == null) {
            return null;
        }

        Message message = null;
        Integer messageId = null;
        Integer messageThreadId = null;
        boolean topicMessage = false;
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
                messageId = accessibleMessage.getMessageId();
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

            messageId = message.getMessageId();
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

        if (message != null && message.isTopicMessage()) {
            topicMessage = true;
            messageThreadId = message.getMessageThreadId();
        }

        if (text != null && !text.isBlank()) {
            ParsedCommand parsed = ParsedCommand.parse(text);
            command = parsed.getCommand();
            argsRaw = parsed.getArgsRaw();
            args = parsed.getArgs();
        }

        return new TelegramUpdateContext(
                botId,
                update,
                message,
                messageId,
                messageThreadId,
                topicMessage,
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
