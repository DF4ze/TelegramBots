package fr.ses10doigts.telegrambots.service.sender;

import fr.ses10doigts.telegrambots.configuration.TelegramRetryProperties;
import fr.ses10doigts.telegrambots.model.TelegramMessageReference;
import fr.ses10doigts.telegrambots.model.TelegramTypingAction;
import fr.ses10doigts.telegrambots.model.TelegramView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
    void shouldSendFormattedMessageWithoutEscapingAndWithMarkdownV2ParseMode() throws Exception {
        Message telegramMessage = mock(Message.class);
        when(telegramMessage.getChatId()).thenReturn(100L);
        when(telegramMessage.getMessageId()).thenReturn(55);
        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        when(client.execute(captor.capture())).thenReturn(telegramMessage);

        // Contient volontairement des caracteres reserves MarkdownV2 (*, _, .) deja
        // echappes/formes par l'appelant : sendFormattedMessage ne doit RIEN toucher,
        // contrairement a sendMarkdownMessage qui echapperait tout (voir TelegramMarkdownUtils).
        String rawMarkdownV2 = "*Gras* et _italique_, point deja echappe\\.";

        TelegramMessageReference reference = sender.sendFormattedMessageAndGetReference(100L, rawMarkdownV2);

        assertThat(reference).isNotNull();
        assertThat(reference.getChatId()).isEqualTo(100L);
        assertThat(reference.getMessageId()).isEqualTo(55);
        assertThat(captor.getValue().getText()).isEqualTo(rawMarkdownV2);
        assertThat(captor.getValue().getParseMode()).isEqualTo("MarkdownV2");
    }

    @Test
    void shouldSendFormattedMessageInAGivenForumThreadWhenMessageThreadIdIsProvided() throws Exception {
        // Regression : sendFormattedMessage(chatId, messageThreadId, text) ignorait
        // jusqu'ici le messageThreadId (toujours envoye hors sujet, sur "General"),
        // contrairement a sendMessage/sendView/sendChatAction qui le supportaient deja -
        // voir ContextAwareTelegramSender pour le mecanisme de resolution ThreadLocal
        // que ce defaut cassait silencieusement pour tout appelant se reposant sur
        // sendFormattedMessage/AndGetReference depuis un Thread de forum.
        Message telegramMessage = mock(Message.class);
        when(telegramMessage.getChatId()).thenReturn(100L);
        when(telegramMessage.getMessageId()).thenReturn(55);
        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        when(client.execute(captor.capture())).thenReturn(telegramMessage);

        sender.sendFormattedMessageAndGetReference(100L, 42, "*deja formate*");

        assertThat(captor.getValue().getMessageThreadId()).isEqualTo(42);
        assertThat(captor.getValue().getParseMode()).isEqualTo("MarkdownV2");
    }

    @Test
    void shouldEditFormattedMessageWithoutEscapingAndWithMarkdownV2ParseMode() throws Exception {
        ArgumentCaptor<EditMessageText> captor = ArgumentCaptor.forClass(EditMessageText.class);
        when(client.execute(captor.capture())).thenReturn(Boolean.TRUE);

        String rawMarkdownV2 = "*Projet* mis a jour";

        TelegramMessageReference reference = sender.editFormattedMessage(100L, 55, rawMarkdownV2);

        assertThat(reference).isNotNull();
        assertThat(reference.getChatId()).isEqualTo(100L);
        assertThat(reference.getMessageId()).isEqualTo(55);
        assertThat(captor.getValue().getText()).isEqualTo(rawMarkdownV2);
        assertThat(captor.getValue().getParseMode()).isEqualTo("MarkdownV2");
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

    @Test
    void shouldSendTypingActionInAGivenForumThreadWhenMessageThreadIdIsProvided() throws Exception {
        when(client.execute(any(SendChatAction.class))).thenReturn(Boolean.TRUE);
        ArgumentCaptor<SendChatAction> captor = ArgumentCaptor.forClass(SendChatAction.class);

        sender.sendTyping(100L, 42);

        verify(client).execute(captor.capture());
        assertThat(captor.getValue().getMessageThreadId()).isEqualTo(42);
    }

    @Test
    void shouldNotSetMessageThreadIdOnTypingActionWhenNull() throws Exception {
        when(client.execute(any(SendChatAction.class))).thenReturn(Boolean.TRUE);
        ArgumentCaptor<SendChatAction> captor = ArgumentCaptor.forClass(SendChatAction.class);

        sender.sendTyping(100L, null);

        verify(client).execute(captor.capture());
        assertThat(captor.getValue().getMessageThreadId()).isNull();
    }

    @Test
    void shouldSplitLongMessageIntoMultipleSendMessageCallsInsteadOfFailing() throws Exception {
        Message telegramMessage = mock(Message.class);
        when(telegramMessage.getChatId()).thenReturn(100L);
        when(telegramMessage.getMessageId()).thenReturn(55);
        when(client.execute(any(SendMessage.class))).thenReturn(telegramMessage);

        String longText = "a".repeat(9000);
        int expectedChunks = (int) Math.ceil(longText.length() / (double) TelegramMessageSplitter.TELEGRAM_MAX_MESSAGE_LENGTH);

        TelegramMessageReference reference = sender.sendMessageAndGetReference(100L, longText);

        verify(client, times(expectedChunks)).execute(any(SendMessage.class));
        assertThat(reference).isNotNull();
        assertThat(reference.getMessageId()).isEqualTo(55);
    }

    @Test
    void shouldNotSplitMessageWithinTheTelegramLimit() throws Exception {
        Message telegramMessage = mock(Message.class);
        when(telegramMessage.getChatId()).thenReturn(100L);
        when(telegramMessage.getMessageId()).thenReturn(55);
        when(client.execute(any(SendMessage.class))).thenReturn(telegramMessage);

        String text = "a".repeat(TelegramMessageSplitter.TELEGRAM_MAX_MESSAGE_LENGTH);

        sender.sendMessageAndGetReference(100L, text);

        verify(client, times(1)).execute(any(SendMessage.class));
    }

    @Test
    void shouldEditFirstChunkAndSendExtraMessagesWhenEditedTextExceedsTheLimit() throws Exception {
        when(client.execute(any(EditMessageText.class))).thenReturn(Boolean.TRUE);

        Message telegramMessage = mock(Message.class);
        when(telegramMessage.getChatId()).thenReturn(100L);
        when(telegramMessage.getMessageId()).thenReturn(999);
        when(client.execute(any(SendMessage.class))).thenReturn(telegramMessage);

        String longText = "b".repeat(9000);
        int expectedChunks = (int) Math.ceil(longText.length() / (double) TelegramMessageSplitter.TELEGRAM_MAX_MESSAGE_LENGTH);

        TelegramMessageReference reference = sender.editMessage(100L, 55, longText);

        verify(client, times(1)).execute(any(EditMessageText.class));
        verify(client, times(expectedChunks - 1)).execute(any(SendMessage.class));
        assertThat(reference).isNotNull();
        assertThat(reference.getChatId()).isEqualTo(100L);
        assertThat(reference.getMessageId()).isEqualTo(55);
    }
}
