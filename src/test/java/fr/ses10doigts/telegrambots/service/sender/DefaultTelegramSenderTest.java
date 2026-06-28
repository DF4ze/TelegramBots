package fr.ses10doigts.telegrambots.service.sender;

import fr.ses10doigts.telegrambots.configuration.TelegramRetryProperties;
import fr.ses10doigts.telegrambots.model.TelegramMessageReference;
import fr.ses10doigts.telegrambots.model.TelegramTypingAction;
import fr.ses10doigts.telegrambots.model.TelegramView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DefaultTelegramSenderTest {

    private TelegramClient client;
    private DefaultTelegramSender sender;

    @BeforeEach
    void setUp() {
        client = mock(TelegramClient.class);
        sender = new DefaultTelegramSender(client, new TelegramRetryProperties());
    }

    @Test
    void shouldReturnMessageReferenceWhenSendingMessage() throws Exception {
        Message telegramMessage = mock(Message.class);
        when(telegramMessage.getChatId()).thenReturn(100L);
        when(telegramMessage.getMessageId()).thenReturn(55);
        when(client.execute(any(SendMessage.class))).thenReturn(telegramMessage);

        TelegramMessageReference reference = sender.sendMessageAndGetReference(100L, "hello");

        assertThat(reference).isNotNull();
        assertThat(reference.getChatId()).isEqualTo(100L);
        assertThat(reference.getMessageId()).isEqualTo(55);
        verify(client).execute(any(SendMessage.class));
    }

    @Test
    void shouldEditMessageAndReturnFallbackReferenceWhenTelegramReturnsBoolean() throws Exception {
        when(client.execute(any(EditMessageText.class))).thenReturn(Boolean.TRUE);

        TelegramMessageReference reference = sender.editMessage(100L, 55, "updated");

        assertThat(reference).isNotNull();
        assertThat(reference.getChatId()).isEqualTo(100L);
        assertThat(reference.getMessageId()).isEqualTo(55);
        verify(client).execute(any(EditMessageText.class));
    }

    @Test
    void shouldUseReplyMarkupEditionWhenViewHasOnlyButtons() throws Exception {
        when(client.execute(any(EditMessageReplyMarkup.class))).thenReturn(Boolean.TRUE);

        TelegramView view = TelegramView.builder()
                .buttons(List.of(List.of()))
                .build();

        TelegramMessageReference reference = sender.editView(100L, 55, view);

        assertThat(reference).isNotNull();
        assertThat(reference.getMessageId()).isEqualTo(55);
        verify(client).execute(any(EditMessageReplyMarkup.class));
        verify(client, never()).execute(any(EditMessageText.class));
    }

    @Test
    void shouldDeleteMessage() throws Exception {
        when(client.execute(any(DeleteMessage.class))).thenReturn(Boolean.TRUE);

        boolean deleted = sender.deleteMessage(100L, 55);

        assertThat(deleted).isTrue();
        verify(client).execute(any(DeleteMessage.class));
    }

    @Test
    void shouldSendTypingAction() throws Exception {
        when(client.execute(any(SendChatAction.class))).thenReturn(Boolean.TRUE);

        sender.sendChatAction(100L, TelegramTypingAction.TYPING);
        sender.sendTyping(100L);

        verify(client, times(2)).execute(any(SendChatAction.class));
    }
}
