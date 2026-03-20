package fr.ses10doigts.telegrambots.service.poller;

import fr.ses10doigts.telegrambots.configuration.TelegramBotProperties;
import fr.ses10doigts.telegrambots.model.TelegramHandlerMethod;
import fr.ses10doigts.telegrambots.model.TelegramUpdateContext;
import fr.ses10doigts.telegrambots.model.TelegramView;
import fr.ses10doigts.telegrambots.service.bot.CurrentTelegramBotContext;
import fr.ses10doigts.telegrambots.service.bot.TelegramBotRegistry;
import fr.ses10doigts.telegrambots.service.poller.handler.TelegramHandlerRegistry;
import fr.ses10doigts.telegrambots.service.sender.TelegramSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class TelegramUpdateDispatcherTest {

    private TelegramHandlerRegistry registry;
    private TelegramSender sender;
    private CurrentTelegramBotContext currentBotContext;
    private TelegramBotRegistry telegramBotRegistry;
    private TelegramUpdateDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        registry = mock(TelegramHandlerRegistry.class);
        sender = mock(TelegramSender.class);
        currentBotContext = new CurrentTelegramBotContext();
        telegramBotRegistry = mock(TelegramBotRegistry.class);

        dispatcher = new TelegramUpdateDispatcher(registry, sender, currentBotContext, telegramBotRegistry);
    }

    @Test
    void shouldDispatchCommandHandlerForMessageUpdate() throws Exception {
        currentBotContext.setCurrentBotId("bot-1");
        allowUser("bot-1", 42L);

        SampleController controller = spy(new SampleController());
        Method method = SampleController.class.getMethod("handleStart", TelegramUpdateContext.class);
        TelegramHandlerMethod handler = new TelegramHandlerMethod(controller, method);

        when(registry.findCommandHandler("bot-1", "/start")).thenReturn(handler);
        when(registry.findChatHandlers("bot-1")).thenReturn(List.of());

        Update update = messageUpdate(100L, 42L, "/start hello world");

        dispatcher.dispatch(update);

        verify(registry).findCommandHandler("bot-1", "/start");
        verify(sender).sendMessage(100L, "started");
        assertThat(controller.lastContext).isNotNull();
        assertThat(controller.lastContext.getCommand()).isEqualTo("/start");
        assertThat(controller.invocationCount).isEqualTo(1);
    }

    @Test
    void shouldDispatchChatHandlersWhenNoCommandIsMatched() throws Exception {
        currentBotContext.setCurrentBotId("bot-1");
        allowUser("bot-1", 42L);

        SampleController controller = spy(new SampleController());
        Method method = SampleController.class.getMethod("handleChat", TelegramUpdateContext.class);
        TelegramHandlerMethod handler = new TelegramHandlerMethod(controller, method);

        when(registry.findCommandHandler("bot-1", null)).thenReturn(null);
        when(registry.findChatHandlers("bot-1")).thenReturn(List.of(handler));

        Update update = messageUpdate(100L, 42L, "hello there");

        dispatcher.dispatch(update);

        verify(registry).findChatHandlers("bot-1");
        verify(sender).sendMessage(100L, "chat-ok");
        assertThat(controller.invocationCount).isEqualTo(1);
    }

    @Test
    void shouldAnswerCallbackQueryAndDispatchCallbackHandler() throws Exception {
        currentBotContext.setCurrentBotId("bot-1");
        allowUser("bot-1", 42L);

        SampleController controller = spy(new SampleController());
        Method method = SampleController.class.getMethod("handleCallback", TelegramUpdateContext.class);
        TelegramHandlerMethod handler = new TelegramHandlerMethod(controller, method);

        when(registry.findCallbackHandler("bot-1", "confirm")).thenReturn(handler);

        Update update = callbackUpdate(100L, 42L, "confirm", "cb-1");

        dispatcher.dispatch(update);

        verify(sender).answerCallbackQuery("cb-1");
        verify(registry).findCallbackHandler("bot-1", "confirm");
        verify(sender).sendMessage(100L, "callback-ok");
        assertThat(controller.invocationCount).isEqualTo(1);
    }

    @Test
    void shouldRefuseUnauthorizedUserOnStartCommand() {
        currentBotContext.setCurrentBotId("bot-1");
        denyAllUsers("bot-1");

        Update update = messageUpdate(100L, 999L, "/start");

        dispatcher.dispatch(update);

        verify(sender).sendMessage(
                100L,
                "Access refused.\nYour Telegram id is : `999`\nAdd it to : telegram.allowed-user-ids"
        );
        verifyNoInteractions(registry);
    }

    @Test
    void shouldIgnoreCallbackWithoutMatchingHandler() {
        currentBotContext.setCurrentBotId("bot-1");
        allowUser("bot-1", 42L);

        when(registry.findCallbackHandler("bot-1", "missing")).thenReturn(null);

        Update update = callbackUpdate(100L, 42L, "missing", "cb-1");

        dispatcher.dispatch(update);

        verify(sender).answerCallbackQuery("cb-1");
        verify(registry).findCallbackHandler("bot-1", "missing");
        verify(sender, never()).sendMessage(anyLong(), anyString());
        verify(sender, never()).sendView(anyLong(), any(TelegramView.class));
    }

    @Test
    void shouldIgnoreUpdateWithoutMessageAndWithoutCallback() {
        currentBotContext.setCurrentBotId("bot-1");

        Update update = mock(Update.class);
        when(update.hasMessage()).thenReturn(false);
        when(update.hasCallbackQuery()).thenReturn(false);

        dispatcher.dispatch(update);

        verifyNoInteractions(registry, sender, telegramBotRegistry);
    }

    @Test
    void shouldIgnoreCallbackWhenCallbackObjectIsNull() {
        currentBotContext.setCurrentBotId("bot-1");

        Update update = mock(Update.class);
        when(update.hasCallbackQuery()).thenReturn(true);
        when(update.getCallbackQuery()).thenReturn(null);

        dispatcher.dispatch(update);

        verifyNoInteractions(registry, sender, telegramBotRegistry);
    }

    private void allowUser(String botId, Long userId) {
        TelegramBotProperties bot = new TelegramBotProperties();
        bot.setId(botId);
        bot.getSecurity().setAllowedUserIds(Set.of(userId));
        when(telegramBotRegistry.getRequiredBot(botId)).thenReturn(bot);
    }

    private void denyAllUsers(String botId) {
        TelegramBotProperties bot = new TelegramBotProperties();
        bot.setId(botId);
        bot.getSecurity().setAllowedUserIds(Set.of(1L));
        when(telegramBotRegistry.getRequiredBot(botId)).thenReturn(bot);
    }

    private static Update messageUpdate(Long chatId, Long userId, String text) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);

        Message message = mock(Message.class);
        when(message.getFrom()).thenReturn(user);
        when(message.getChatId()).thenReturn(chatId);
        when(message.hasText()).thenReturn(true);
        when(message.getText()).thenReturn(text);

        Update update = mock(Update.class);
        when(update.hasMessage()).thenReturn(true);
        when(update.hasCallbackQuery()).thenReturn(false);
        when(update.getMessage()).thenReturn(message);
        return update;
    }

    private static Update callbackUpdate(Long chatId, Long userId, String callbackData, String callbackId) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);

        Message message = mock(Message.class);
        when(message.getChatId()).thenReturn(chatId);
        when(message.hasText()).thenReturn(true);
        when(message.getText()).thenReturn("/callback");

        CallbackQuery callbackQuery = mock(CallbackQuery.class);
        when(callbackQuery.getId()).thenReturn(callbackId);
        when(callbackQuery.getFrom()).thenReturn(user);
        when(callbackQuery.getMessage()).thenReturn(message);
        when(callbackQuery.getData()).thenReturn(callbackData);

        Update update = mock(Update.class);
        when(update.hasCallbackQuery()).thenReturn(true);
        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(update.hasMessage()).thenReturn(false);
        return update;
    }

    public static class SampleController {
        int invocationCount = 0;
        TelegramUpdateContext lastContext;

        public String handleStart(TelegramUpdateContext context) {
            invocationCount++;
            lastContext = context;
            return "started";
        }

        public String handleChat(TelegramUpdateContext context) {
            invocationCount++;
            lastContext = context;
            return "chat-ok";
        }

        public String handleCallback(TelegramUpdateContext context) {
            invocationCount++;
            lastContext = context;
            return "callback-ok";
        }
    }
}