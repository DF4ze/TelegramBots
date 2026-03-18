package fr.ses10doigts.telegrambots.service.poller;

import static org.junit.jupiter.api.Assertions.*;

import fr.ses10doigts.telegrambots.configuration.TelegramProperties;
import fr.ses10doigts.telegrambots.model.TelegramHandlerMethod;
import fr.ses10doigts.telegrambots.model.TelegramUpdateContext;
import fr.ses10doigts.telegrambots.service.poller.handler.TelegramHandlerRegistry;
import fr.ses10doigts.telegrambots.service.sender.TelegramSender;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TelegramUpdateDispatcherTest {
/*
    @Test
    void shouldSendResponseWhenHandlerReturnsString() throws Exception {
        TelegramSender sender = Mockito.mock(TelegramSender.class);

        DemoCommandHandler bean = new DemoCommandHandler();
        Method method = DemoCommandHandler.class.getMethod("ping", TelegramUpdateContext.class);

        TelegramHandlerMethod handlerMethod = new TelegramHandlerMethod(bean, method);

        TelegramHandlerRegistry registry = Mockito.mock(TelegramHandlerRegistry.class);
        when(registry.getCommandHandlers()).thenReturn(Map.of("/ping", handlerMethod));
        when(registry.getChatHandlers()).thenReturn(List.of());

        TelegramProperties properties = new TelegramProperties();

        TelegramUpdateDispatcher dispatcher = new TelegramUpdateDispatcher(registry, sender, properties, sender.getClient());

        Update update = buildTextUpdate(42L, 99L, "/ping");
        dispatcher.dispatch(update);

        verify(sender).sendMessage(42L, "pong");
    }

    private Update buildTextUpdate(Long chatId, Long userId, String text) {
        User user = new User();
        user.setId(userId);

        Chat chat = new Chat();
        chat.setId(chatId);

        Message message = new Message();
        message.setText(text);
        message.setChat(chat);
        message.setFrom(user);

        Update update = new Update();
        update.setMessage(message);

        return update;
    }

    static class DemoCommandHandler {
        public String ping(TelegramUpdateContext ctx) {
            return "pong";
        }
    }

 */
}