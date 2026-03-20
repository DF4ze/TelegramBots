package fr.ses10doigts.telegrambots.model;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class TelegramUpdateContextTest {

    private static final String BOT_ID = "bot-1";

    @Test
    void shouldBuildContextFromSimpleTextMessage() {
        Update update = messageUpdate(100L, 200L, "hello world");

        TelegramUpdateContext context = TelegramUpdateContext.from(update, BOT_ID);

        assertNotNull(context);
        assertEquals(BOT_ID, context.getBotId());
        assertSame(update, context.getUpdate());
        assertEquals(100L, context.getChatId());
        assertEquals(200L, context.getUserId());
        assertEquals("hello world", context.getText());
        assertNull(context.getCommand());
        assertNull(context.getCommandArgsRaw());
        assertEquals(List.of(), context.getArgs());
        assertFalse(context.isCallbackQuery());
        assertNull(context.getCallbackData());
    }

    @Test
    void shouldParseCommandWithArguments() {
        Update update = messageUpdate(100L, 200L, "/trade btc 10 now");

        TelegramUpdateContext context = TelegramUpdateContext.from(update, BOT_ID);

        assertNotNull(context);
        assertEquals("/trade", context.getCommand());
        assertEquals("btc 10 now", context.getCommandArgsRaw());
        assertEquals(List.of("btc", "10", "now"), context.getArgs());
    }

    @Test
    void shouldBuildContextFromCallbackQuery() {
        Update update = callbackUpdate(123L, 456L, "BTN_CLICK", "menu text");

        TelegramUpdateContext context = TelegramUpdateContext.from(update, BOT_ID);

        assertNotNull(context);
        assertTrue(context.isCallbackQuery());
        assertEquals("BTN_CLICK", context.getCallbackData());
        assertEquals(123L, context.getChatId());
        assertEquals(456L, context.getUserId());
        assertEquals("menu text", context.getText());
    }

    @Test
    void shouldReturnNullWhenUpdateIsNull() {
        assertNull(TelegramUpdateContext.from(null, BOT_ID));
    }

    @Test
    void shouldReturnNullWhenUpdateHasNeitherMessageNorCallback() {
        Update update = new Update();

        TelegramUpdateContext context = TelegramUpdateContext.from(update, BOT_ID);

        assertNull(context);
    }

    @Test
    void shouldReturnNullWhenUpdateClaimsCallbackQueryButCallbackIsNull() {
        Update update = Mockito.mock(Update.class);
        when(update.hasCallbackQuery()).thenReturn(true);
        when(update.getCallbackQuery()).thenReturn(null);

        TelegramUpdateContext context = TelegramUpdateContext.from(update, BOT_ID);

        assertNull(context);
    }

    @Test
    void shouldReturnNullWhenUpdateClaimsMessageButMessageIsNull() {
        Update update = Mockito.mock(Update.class);
        when(update.hasCallbackQuery()).thenReturn(false);
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(null);

        TelegramUpdateContext context = TelegramUpdateContext.from(update, BOT_ID);

        assertNull(context);
    }

    @Test
    void shouldHandleMessageWithoutText() {
        Update update = messageUpdate(100L, 200L, null);

        TelegramUpdateContext context = TelegramUpdateContext.from(update, BOT_ID);

        assertNotNull(context);
        assertEquals(100L, context.getChatId());
        assertEquals(200L, context.getUserId());
        assertNull(context.getText());
        assertNull(context.getCommand());
        assertNull(context.getCommandArgsRaw());
        assertEquals(List.of(), context.getArgs());
    }

    @Test
    void shouldHandleBlankTextWithoutParsingCommand() {
        Update update = messageUpdate(100L, 200L, "   ");

        TelegramUpdateContext context = TelegramUpdateContext.from(update, BOT_ID);

        assertNotNull(context);
        assertEquals("   ", context.getText());
        assertNull(context.getCommand());
        assertNull(context.getCommandArgsRaw());
        assertEquals(List.of(), context.getArgs());
    }

    @Test
    void shouldHandleCallbackQueryWithMissingMessageAndUser() {
        Update update = new Update();

        CallbackQuery callbackQuery = Mockito.mock(CallbackQuery.class);
        when(callbackQuery.getData()).thenReturn(null);
        when(callbackQuery.getFrom()).thenReturn(null);
        when(callbackQuery.getMessage()).thenReturn(null);

        update.setCallbackQuery(callbackQuery);

        TelegramUpdateContext context = TelegramUpdateContext.from(update, BOT_ID);

        assertNotNull(context);
        assertTrue(context.isCallbackQuery());
        assertNull(context.getCallbackData());
        assertNull(context.getChatId());
        assertNull(context.getUserId());
        assertNull(context.getText());
    }

    private Update messageUpdate(Long chatId, Long userId, String text) {
        Update update = new Update();

        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        User user = Mockito.mock(User.class);

        when(chat.getId()).thenReturn(chatId);
        when(user.getId()).thenReturn(userId);
        when(message.getChat()).thenReturn(chat);
        when(message.getChatId()).thenReturn(chatId);
        when(message.getFrom()).thenReturn(user);
        when(message.hasText()).thenReturn(true);
        when(message.getText()).thenReturn(text);

        update.setMessage(message);
        return update;
    }

    private Update callbackUpdate(Long chatId, Long userId, String data, String messageText) {
        Update update = new Update();

        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        User user = Mockito.mock(User.class);
        CallbackQuery callbackQuery = Mockito.mock(CallbackQuery.class);

        when(chat.getId()).thenReturn(chatId);
        when(user.getId()).thenReturn(userId);
        when(message.getChat()).thenReturn(chat);
        when(message.getChatId()).thenReturn(chatId);
        when(message.hasText()).thenReturn(true);
        when(message.getText()).thenReturn(messageText);
        when(callbackQuery.getMessage()).thenReturn(message);
        when(callbackQuery.getFrom()).thenReturn(user);
        when(callbackQuery.getData()).thenReturn(data);

        update.setCallbackQuery(callbackQuery);
        return update;
    }
}