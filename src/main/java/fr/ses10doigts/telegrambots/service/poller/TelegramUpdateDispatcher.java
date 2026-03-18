package fr.ses10doigts.telegrambots.service.poller;

import fr.ses10doigts.telegrambots.configuration.TelegramProperties;
import fr.ses10doigts.telegrambots.model.TelegramUpdateContext;
import fr.ses10doigts.telegrambots.model.TelegramHandlerMethod;
import fr.ses10doigts.telegrambots.model.TelegramView;
import fr.ses10doigts.telegrambots.service.poller.handler.TelegramHandlerRegistry;
import fr.ses10doigts.telegrambots.service.sender.TelegramSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class TelegramUpdateDispatcher {

    private final TelegramHandlerRegistry registry;
    private final TelegramSender telegramSender;
    private final TelegramProperties properties;
    private final TelegramClient telegramClient;



    public void dispatch(Update update) {
        if (update.hasCallbackQuery()) {
            handleCallbackQuery(update);
            return;
        }

        if (!update.hasMessage()) {
            return;
        }

        TelegramUpdateContext context = TelegramUpdateContext.from(update);
        if( context == null )
            return;

        if (isNotAllowed(context.getUserId())) {
            handleUnauthorized(context);
            return;
        }

        if (context.getCommand() != null) {
            TelegramHandlerMethod handler = registry.getCommandHandlers().get(context.getCommand());
            if (handler != null) {
                invoke(handler, context);
                return;
            }
        }

        for (TelegramHandlerMethod handler : registry.getChatHandlers()) {
            invoke(handler, context);
        }
    }

    private void dispatchCallbackQuery(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String callbackData = callbackQuery.getData();

        TelegramHandlerMethod handlerMethod = registry.getCallbackHandlers().get(callbackData);

        if (handlerMethod == null) {
            telegramSender.answerCallbackQuery(callbackQuery.getId());
            log.warn("No Telegram callback handler found for data={}", callbackData);
            return;
        }

        TelegramUpdateContext context = TelegramUpdateContext.from(update);

        try {
            invoke(handlerMethod, context);
        } finally {
            telegramSender.answerCallbackQuery(callbackQuery.getId());
        }
    }

    private void handleCallbackQuery(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        if (callbackQuery == null) {
            return;
        }

        answerCallback(callbackQuery.getId());

        Message message = null;

        if (callbackQuery.getMessage() instanceof Message m) {
            message = m;
        }

        Long chatId = message != null && message.getChat() != null ? message.getChatId() : null;
        Long userId = callbackQuery.getFrom() != null ? callbackQuery.getFrom().getId() : null;
        String callbackData = callbackQuery.getData();

        TelegramUpdateContext context = new TelegramUpdateContext(
                update,
                message,
                chatId,
                userId,
                message != null ? message.getText() : null,
                null,
                null,
                List.of(),
                true,
                callbackData
        );

        if (isNotAllowed(userId)) {
            handleUnauthorized(context);
            return;
        }

        TelegramHandlerMethod handler = registry.getCallbackHandlers().get(callbackData);
        if (handler != null) {
            invoke(handler, context);
        }
    }

    private void answerCallback(String callbackQueryId) {
        try {
            telegramClient.execute(AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackQueryId)
                    .build());
        } catch (TelegramApiException e) {
            log.warn("Unable to answer Telegram callback query", e);
        }
    }

    private boolean isNotAllowed(Long userId) {
        Set<Long> allowed = properties.getAllowedUserIds();
        return allowed != null && !allowed.isEmpty() && (userId == null || !allowed.contains(userId));
    }

    private void handleUnauthorized(TelegramUpdateContext context) {
        if ("/start".equals(context.getCommand()) && context.getChatId() != null) {
            telegramSender.sendMessage(
                    context.getChatId(),
                    "Access refused.\nYour Telegram id is : `" + context.getUserId() + "`\n" +
                            "Add it to : telegram.allowed-user-ids"
            );
        }

        log.warn("Unauthorized Telegram user blocked: userId={}, chatId={}",
                context.getUserId(), context.getChatId());
    }

    private void invoke(TelegramHandlerMethod handler, TelegramUpdateContext context) {
        try {
            Object result = handler.method().invoke(handler.bean(), context);

            switch (result) {
                case null -> {
                }
                case String response when !response.isBlank() ->
                    telegramSender.sendMessage(context.getChatId(), response);

                case TelegramView view ->
                    telegramSender.sendView(context.getChatId(), view);

                default ->
                    throw new IllegalStateException("Unsupported Telegram handler return type: " + result.getClass());

            }

        } catch (InvocationTargetException e) {
            log.error("Telegram handler threw an exception: {}#{}",
                    handler.bean().getClass().getSimpleName(),
                    handler.method().getName(),
                    e.getTargetException());
        } catch (Exception e) {
            log.error("Error while invoking Telegram handler {}#{}",
                    handler.bean().getClass().getSimpleName(),
                    handler.method().getName(),
                    e);
        }
    }
}