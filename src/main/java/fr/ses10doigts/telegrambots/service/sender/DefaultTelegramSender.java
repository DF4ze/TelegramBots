package fr.ses10doigts.telegrambots.service.sender;

import fr.ses10doigts.telegrambots.configuration.TelegramRetryProperties;
import fr.ses10doigts.telegrambots.model.TelegramButtonView;
import fr.ses10doigts.telegrambots.model.TelegramMessageFormat;
import fr.ses10doigts.telegrambots.model.TelegramMessageReference;
import fr.ses10doigts.telegrambots.model.TelegramTypingAction;
import fr.ses10doigts.telegrambots.model.TelegramView;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DefaultTelegramSender implements TelegramSender {

    private final TelegramClient client;
    private final TelegramRetryProperties retryProperties;

    public DefaultTelegramSender(String botToken, TelegramRetryProperties retryProperties) {
        this(new OkHttpTelegramClient(botToken), retryProperties);
    }

    DefaultTelegramSender(TelegramClient client, TelegramRetryProperties retryProperties) {
        this.client = client;
        this.retryProperties = retryProperties != null ? retryProperties : new TelegramRetryProperties();
    }

    /**
     * Envoie un message texte simple.
     */
    @Override
    public void sendMessage(Long chatId, String text) {
        sendMessageAndGetReference(chatId, text);
    }

    /**
     * Envoie un message texte simple et retourne sa référence.
     */
    @Override
    public TelegramMessageReference sendMessageAndGetReference(Long chatId, String text) {
        return sendText(chatId, text, null, null);
    }

    /**
     * Envoie un message Markdown V2 échappé.
     */
    @Override
    public void sendMarkdownMessage(Long chatId, String text) {
        sendMarkdownMessageAndGetReference(chatId, text);
    }

    /**
     * Envoie un message Markdown V2 échappé et retourne sa référence.
     */
    @Override
    public TelegramMessageReference sendMarkdownMessageAndGetReference(Long chatId, String text) {
        return sendText(chatId, TelegramMarkdownUtils.escapeMarkdownV2(text), "MarkdownV2", null);
    }

    /**
     * Envoie un message Markdown V2 en préservant les liens.
     */
    @Override
    public void sendMarkdownMessagePreservingLinks(Long chatId, String text) {
        sendMarkdownMessagePreservingLinksAndGetReference(chatId, text);
    }

    /**
     * Envoie un message Markdown V2 en préservant les liens et retourne sa référence.
     */
    @Override
    public TelegramMessageReference sendMarkdownMessagePreservingLinksAndGetReference(Long chatId, String text) {
        return sendText(chatId, TelegramMarkdownUtils.escapeMarkdownV2PreservingLinks(text), "MarkdownV2", null);
    }

    /**
     * Envoie un message MarkdownV2 déjà préparé par l'appelant, sans échappement.
     */
    @Override
    public void sendFormattedMessage(Long chatId, String markdownV2Text) {
        sendFormattedMessageAndGetReference(chatId, markdownV2Text);
    }

    /**
     * Envoie un message MarkdownV2 déjà préparé par l'appelant, sans échappement,
     * et retourne sa référence.
     */
    @Override
    public TelegramMessageReference sendFormattedMessageAndGetReference(Long chatId, String markdownV2Text) {
        return sendText(chatId, markdownV2Text, "MarkdownV2", null);
    }

    /**
     * Édite le texte MarkdownV2 déjà préparé par l'appelant, sans échappement,
     * d'un message existant.
     */
    @Override
    public TelegramMessageReference editFormattedMessage(Long chatId, Integer messageId, String markdownV2Text) {
        return editText(chatId, messageId, markdownV2Text, "MarkdownV2", null);
    }

    /**
     * Envoie une vue Telegram.
     */
    @Override
    public void sendView(Long chatId, TelegramView view) {
        sendViewAndGetReference(chatId, view);
    }

    /**
     * Envoie une vue Telegram et retourne sa référence.
     */
    @Override
    public TelegramMessageReference sendViewAndGetReference(Long chatId, TelegramView view) {
        if (view == null) {
            log.warn("TelegramView is null, nothing sent for chatId={}", chatId);
            return null;
        }

        String text = view.getText();
        boolean hasButtons = view.getButtons() != null && !view.getButtons().isEmpty();

        if (!hasButtons && (text == null || text.isBlank())) {
            log.warn("TelegramView text is blank and no buttons are present, nothing sent for chatId={}", chatId);
            return null;
        }

        if (!hasButtons) {
            return switch (resolveFormat(view)) {
                case MARKDOWN -> sendMarkdownMessageAndGetReference(chatId, text);
                case MARKDOWN_PRESERVE_LINKS -> sendMarkdownMessagePreservingLinksAndGetReference(chatId, text);
                default -> sendMessageAndGetReference(chatId, text);
            };
        }

        PreparedText preparedText = prepareViewText(view);
        return sendText(chatId, preparedText.text(), preparedText.parseMode(), buildInlineKeyboard(view.getButtons()));
    }

    /**
     * Envoie l'acquittement d'une callback query Telegram.
     */
    @Override
    public void answerCallbackQuery(String callbackQueryId) {
        try {
            AnswerCallbackQuery answerCallbackQuery = AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackQueryId)
                    .build();

            client.execute(answerCallbackQuery);
        } catch (TelegramApiException e) {
            log.error("Telegram answerCallbackQuery error", e);
        }
    }

    /**
     * Édite le texte d'un message existant.
     */
    @Override
    public TelegramMessageReference editMessage(Long chatId, Integer messageId, String text) {
        return editText(chatId, messageId, text, null, null);
    }

    /**
     * Édite le texte Markdown V2 d'un message existant.
     */
    @Override
    public TelegramMessageReference editMarkdownMessage(Long chatId, Integer messageId, String text) {
        return editText(chatId, messageId, TelegramMarkdownUtils.escapeMarkdownV2(text), "MarkdownV2", null);
    }

    /**
     * Édite le texte Markdown V2 d'un message existant en préservant les liens.
     */
    @Override
    public TelegramMessageReference editMarkdownMessagePreservingLinks(Long chatId, Integer messageId, String text) {
        return editText(chatId, messageId, TelegramMarkdownUtils.escapeMarkdownV2PreservingLinks(text), "MarkdownV2", null);
    }

    /**
     * Édite une vue Telegram existante.
     */
    @Override
    public TelegramMessageReference editView(Long chatId, Integer messageId, TelegramView view) {
        if (view == null) {
            log.warn("TelegramView is null, nothing edited for chatId={} messageId={}", chatId, messageId);
            return null;
        }

        boolean hasButtons = view.getButtons() != null && !view.getButtons().isEmpty();
        String text = view.getText();

        if (!hasButtons && (text == null || text.isBlank())) {
            log.warn("TelegramView text is blank and no buttons are present, nothing edited for chatId={} messageId={}", chatId, messageId);
            return null;
        }

        if (text == null || text.isBlank()) {
            return editReplyMarkup(chatId, messageId, buildInlineKeyboard(view.getButtons()));
        }

        PreparedText preparedText = prepareViewText(view);
        InlineKeyboardMarkup replyMarkup = hasButtons ? buildInlineKeyboard(view.getButtons()) : null;
        return editText(chatId, messageId, preparedText.text(), preparedText.parseMode(), replyMarkup);
    }

    /**
     * Supprime un message existant.
     */
    @Override
    public boolean deleteMessage(Long chatId, Integer messageId) {
        try {
            DeleteMessage deleteMessage = DeleteMessage.builder()
                    .chatId(chatId)
                    .messageId(messageId)
                    .build();

            return executeWithRetry("deleteMessage", () -> client.execute(deleteMessage));
        } catch (Exception e) {
            log.error("Telegram deleteMessage error", e);
            return false;
        }
    }

    /**
     * Envoie une action de présence Telegram.
     */
    @Override
    public void sendChatAction(Long chatId, TelegramTypingAction action) {
        try {
            SendChatAction sendChatAction = SendChatAction.builder()
                    .chatId(chatId)
                    .action(action.getApiValue())
                    .build();

            executeWithRetry("sendChatAction", () -> client.execute(sendChatAction));
        } catch (Exception e) {
            log.error("Telegram sendChatAction error", e);
        }
    }

    /**
     * Déclenche l'indication "est en train d'écrire".
     */
    @Override
    public void sendTyping(Long chatId) {
        sendChatAction(chatId, TelegramTypingAction.TYPING);
    }

    /**
     * Envoie une photo.
     */
    @Override
    public void sendPhoto(Long chatId, String photoPath, String caption) {
        sendPhotoAndGetReference(chatId, photoPath, caption);
    }

    /**
     * Envoie une photo et retourne sa référence.
     */
    @Override
    public TelegramMessageReference sendPhotoAndGetReference(Long chatId, String photoPath, String caption) {
        try {
            SendPhoto sendPhoto = new SendPhoto(
                    chatId.toString(),
                    new InputFile(new File(photoPath))
            );
            sendPhoto.setCaption(caption);
            Message message = executeWithRetry("sendPhoto", () -> client.execute(sendPhoto));
            return toReference(message, chatId);
        } catch (Exception e) {
            log.error("Telegram sendPhoto error", e);
            return null;
        }
    }

    /**
     * Envoie un document.
     */
    @Override
    public void sendDocument(Long chatId, String documentPath, String caption) {
        sendDocumentAndGetReference(chatId, documentPath, caption);
    }

    /**
     * Envoie un document et retourne sa référence.
     */
    @Override
    public TelegramMessageReference sendDocumentAndGetReference(Long chatId, String documentPath, String caption) {
        try {
            SendDocument sendDocument = new SendDocument(
                    chatId.toString(),
                    new InputFile(new File(documentPath))
            );
            sendDocument.setCaption(caption);
            Message message = executeWithRetry("sendDocument", () -> client.execute(sendDocument));
            return toReference(message, chatId);
        } catch (Exception e) {
            log.error("Telegram sendDocument error", e);
            return null;
        }
    }

    /**
     * Envoie un document texte généré à la volée.
     */
    @Override
    public void sendTextDocument(Long chatId, String content, String fileName, String caption) {
        sendTextDocumentAndGetReference(chatId, content, fileName, caption);
    }

    /**
     * Envoie un document texte généré à la volée et retourne sa référence.
     */
    @Override
    public TelegramMessageReference sendTextDocumentAndGetReference(Long chatId, String content, String fileName, String caption) {
        try {
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

            InputFile inputFile = new InputFile(
                    new ByteArrayInputStream(bytes),
                    fileName
            );

            SendDocument sendDocument = new SendDocument(chatId.toString(), inputFile);
            sendDocument.setCaption(caption);

            Message message = executeWithRetry("sendTextDocument", () -> client.execute(sendDocument));
            return toReference(message, chatId);
        } catch (Exception e) {
            log.error("Telegram sendTextDocument error", e);
            return null;
        }
    }

    /**
     * Construit le clavier inline demandé par la vue Telegram.
     */
    private InlineKeyboardMarkup buildInlineKeyboard(List<List<TelegramButtonView>> buttonRows) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();

        for (List<TelegramButtonView> row : buttonRows) {
            if (row == null || row.isEmpty()) {
                continue;
            }

            InlineKeyboardRow keyboardRow = new InlineKeyboardRow();

            for (TelegramButtonView buttonView : row) {
                if (buttonView == null) {
                    continue;
                }

                String text = buttonView.getText();
                String callbackData = buttonView.getCallbackData();

                if (text == null || text.isBlank() || callbackData == null || callbackData.isBlank()) {
                    log.warn("Skipping invalid Telegram button: text='{}', callbackData='{}'", text, callbackData);
                    continue;
                }

                InlineKeyboardButton button = InlineKeyboardButton.builder()
                        .text(text)
                        .callbackData(callbackData)
                        .build();

                keyboardRow.add(button);
            }

            if (!keyboardRow.isEmpty()) {
                keyboard.add(keyboardRow);
            }
        }

        return InlineKeyboardMarkup.builder()
                .keyboard(keyboard)
                .build();
    }

    /**
     * Prépare le texte et le parse mode d'une vue Telegram.
     */
    private PreparedText prepareViewText(TelegramView view) {
        String effectiveText = (view.getText() == null || view.getText().isBlank()) ? "Question :" : view.getText();

        return switch (resolveFormat(view)) {
            case MARKDOWN -> new PreparedText(
                    TelegramMarkdownUtils.escapeMarkdownV2(effectiveText),
                    "MarkdownV2"
            );
            case MARKDOWN_PRESERVE_LINKS -> new PreparedText(
                    TelegramMarkdownUtils.escapeMarkdownV2PreservingLinks(effectiveText),
                    "MarkdownV2"
            );
            default -> new PreparedText(effectiveText, null);
        };
    }

    /**
     * Retourne le format effectif d'une vue Telegram.
     */
    private TelegramMessageFormat resolveFormat(TelegramView view) {
        return view.getFormat() != null ? view.getFormat() : TelegramMessageFormat.PLAIN;
    }

    /**
     * Envoie un message texte avec parse mode et clavier inline optionnels.
     *
     * <p>Si {@code text} dépasse la limite Telegram ({@value TelegramMessageSplitter#TELEGRAM_MAX_MESSAGE_LENGTH}
     * caractères), il est automatiquement découpé et envoyé sous forme de
     * plusieurs messages consécutifs (voir {@link TelegramMessageSplitter}),
     * au lieu d'échouer ou d'être rejeté par l'API Telegram. Le clavier
     * inline, s'il y en a un, n'est attaché qu'au dernier morceau. La
     * référence retournée est celle du dernier message envoyé avec succès.</p>
     */
    private TelegramMessageReference sendText(Long chatId, String text, String parseMode, InlineKeyboardMarkup replyMarkup) {
        List<String> chunks = TelegramMessageSplitter.split(text);

        if (chunks.size() <= 1) {
            return sendSingleMessage(chatId, chunks.isEmpty() ? text : chunks.getFirst(), parseMode, replyMarkup);
        }

        log.debug("Telegram message exceeds {} characters (length={}), splitting into {} messages for chatId={}",
                TelegramMessageSplitter.TELEGRAM_MAX_MESSAGE_LENGTH, text.length(), chunks.size(), chatId);

        TelegramMessageReference lastReference = null;

        for (int i = 0; i < chunks.size(); i++) {
            boolean isLastChunk = i == chunks.size() - 1;
            lastReference = sendSingleMessage(chatId, chunks.get(i), parseMode, isLastChunk ? replyMarkup : null);

            if (lastReference == null) {
                // Un morceau a échoué : on interrompt l'envoi des morceaux suivants
                // pour éviter d'envoyer la suite hors d'ordre.
                break;
            }
        }

        return lastReference;
    }

    /**
     * Envoie un unique morceau de message texte avec parse mode et clavier
     * inline optionnels.
     */
    private TelegramMessageReference sendSingleMessage(Long chatId, String text, String parseMode, InlineKeyboardMarkup replyMarkup) {
        try {
            SendMessage sendMessage = new SendMessage(chatId.toString(), text);
            sendMessage.setDisableWebPagePreview(true);

            if (parseMode != null) {
                sendMessage.setParseMode(parseMode);
            }

            if (replyMarkup != null) {
                sendMessage.setReplyMarkup(replyMarkup);
            }

            Message message = executeWithRetry("sendMessage", () -> client.execute(sendMessage));
            return toReference(message, chatId);
        } catch (Exception e) {
            log.error("Telegram sendMessage error", e);
            return null;
        }
    }

    /**
     * Édite le texte d'un message existant.
     *
     * <p>Un message Telegram existant ne peut pas être scindé : si
     * {@code text} dépasse la limite Telegram
     * ({@value TelegramMessageSplitter#TELEGRAM_MAX_MESSAGE_LENGTH} caractères),
     * le premier morceau remplace le texte du message édité (avec le clavier
     * inline le cas échéant) et les morceaux suivants sont envoyés comme
     * nouveaux messages à la suite, au lieu de faire échouer l'édition. La
     * référence retournée reste celle du message édité.</p>
     */
    private TelegramMessageReference editText(Long chatId, Integer messageId, String text, String parseMode, InlineKeyboardMarkup replyMarkup) {
        List<String> chunks = TelegramMessageSplitter.split(text);

        if (chunks.size() <= 1) {
            return editSingleMessage(chatId, messageId, chunks.isEmpty() ? text : chunks.getFirst(), parseMode, replyMarkup);
        }

        log.warn("Edited Telegram message exceeds {} characters (length={}); editing the first chunk and sending {} extra message(s) for chatId={} messageId={}",
                TelegramMessageSplitter.TELEGRAM_MAX_MESSAGE_LENGTH, text.length(), chunks.size() - 1, chatId, messageId);

        TelegramMessageReference editedReference = editSingleMessage(chatId, messageId, chunks.getFirst(), parseMode, replyMarkup);

        for (int i = 1; i < chunks.size(); i++) {
            sendSingleMessage(chatId, chunks.get(i), parseMode, null);
        }

        return editedReference;
    }

    /**
     * Édite le texte d'un unique message existant (un morceau).
     */
    private TelegramMessageReference editSingleMessage(Long chatId, Integer messageId, String text, String parseMode, InlineKeyboardMarkup replyMarkup) {
        try {
            EditMessageText editMessageText = EditMessageText.builder()
                    .chatId(chatId)
                    .messageId(messageId)
                    .text(text)
                    .build();
            editMessageText.setDisableWebPagePreview(true);
            editMessageText.setReplyMarkup(replyMarkup);
            if (parseMode != null) {
                editMessageText.setParseMode(parseMode);
            }

            Object editedMessage = executeWithRetry("editMessage", () -> client.execute(editMessageText));
            return toReference(editedMessage, chatId, messageId);
        } catch (Exception e) {
            log.error("Telegram editMessage error", e);
            return null;
        }
    }

    /**
     * Édite uniquement le clavier inline d'un message existant.
     */
    private TelegramMessageReference editReplyMarkup(Long chatId, Integer messageId, InlineKeyboardMarkup replyMarkup) {
        try {
            EditMessageReplyMarkup editMessageReplyMarkup = EditMessageReplyMarkup.builder()
                    .chatId(chatId.toString())
                    .messageId(messageId)
                    .replyMarkup(replyMarkup)
                    .build();

            Object editedMessage = executeWithRetry("editMessageReplyMarkup", () -> client.execute(editMessageReplyMarkup));
            return toReference(editedMessage, chatId, messageId);
        } catch (Exception e) {
            log.error("Telegram editMessageReplyMarkup error", e);
            return null;
        }
    }

    /**
     * Construit une référence à partir d'un message Telegram renvoyé par l'API.
     */
    private TelegramMessageReference toReference(Message message, Long fallbackChatId) {
        if (message == null) {
            return null;
        }

        return TelegramMessageReference.builder()
                .chatId(message.getChatId() != null ? message.getChatId() : fallbackChatId)
                .messageId(message.getMessageId())
                .build();
    }

    /**
     * Construit une référence à partir d'une réponse Telegram de type Message ou Boolean.
     */
    private TelegramMessageReference toReference(Object result, Long fallbackChatId, Integer fallbackMessageId) {
        if (result instanceof Message message) {
            return toReference(message, fallbackChatId);
        }

        return TelegramMessageReference.builder()
                .chatId(fallbackChatId)
                .messageId(fallbackMessageId)
                .build();
    }

    /**
     * Exécute un appel Telegram avec retry optionnel.
     */
    private <T> T executeWithRetry(String actionName, TelegramCall<T> call) throws Exception {

        if (!retryProperties.isEnabled()) {
            return call.execute();
        }

        int maxAttempts = Math.max(1, retryProperties.getMaxAttempts());
        long delayMillis = Math.max(0, retryProperties.getDelaySeconds()) * 1000L;

        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return call.execute();
            } catch (Exception e) {
                lastException = e;

                if (!isRetryable(e)) {
                    throw e;
                }

                log.warn("Telegram {} failed on attempt {}/{}", actionName, attempt, maxAttempts, e);

                if (attempt < maxAttempts && delayMillis > 0) {
                    long currentDelay = delayMillis * attempt; // Délai qui s'allonge
                    log.info("Waiting {}ms before next attempt for {} (attempt {})", currentDelay, actionName, attempt);
                    try {
                        Thread.sleep(currentDelay);
                    } catch (InterruptedException interruptedException) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("Telegram retry interrupted", interruptedException);
                    }
                }
            }
        }

        throw lastException;
    }

    /**
     * Indique si une erreur Telegram mérite un retry.
     */
    private boolean isRetryable(Exception exception) {
        if (!(exception instanceof TelegramApiException telegramApiException)) {
            return true;
        }

        String message = telegramApiException.getMessage();
        if (message == null) {
            return true;
        }

        String lowerMessage = message.toLowerCase();

        if (lowerMessage.contains("400 bad request")) {
            return false;
        }

        if (lowerMessage.contains("403 forbidden")) {
            return false;
        }

        return !lowerMessage.contains("404 not found");
    }

    private record PreparedText(String text, String parseMode) {
    }
}
