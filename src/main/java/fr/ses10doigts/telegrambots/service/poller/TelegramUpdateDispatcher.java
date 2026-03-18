package fr.ses10doigts.telegrambots.service.poller;

import fr.ses10doigts.telegrambots.configuration.TelegramProperties;
import fr.ses10doigts.telegrambots.model.TelegramUpdateContext;
import fr.ses10doigts.telegrambots.model.TelegramHandlerMethod;
import fr.ses10doigts.telegrambots.model.TelegramView;
import fr.ses10doigts.telegrambots.service.poller.handler.TelegramHandlerRegistry;
import fr.ses10doigts.telegrambots.service.sender.TelegramSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class TelegramUpdateDispatcher {

    private final TelegramHandlerRegistry registry;
    private final TelegramSender telegramSender;
    private final TelegramProperties properties;



    public void dispatch(Update update) {
        if (update.hasCallbackQuery()) {
            dispatchCallbackQuery(update);
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
        if (callbackQuery == null) {
            return;
        }

        // Ack immédiat pour éviter le spinner Telegram
        telegramSender.answerCallbackQuery(callbackQuery.getId());

        TelegramUpdateContext context = TelegramUpdateContext.from(update);

        if( context == null ){
            log.warn("dispatchCallbackQuery : Unable to handle update");
            return;
        }

        if (isNotAllowed(context.getUserId())) {
            handleUnauthorized(context);
            return;
        }

        TelegramHandlerMethod handler = registry.getCallbackHandlers().get( context.getCallbackData() );
        if (handler == null) {
            log.warn("No Telegram callback handler found for data={}", context.getCallbackData());
            return;
        }

        invoke(handler, context);
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