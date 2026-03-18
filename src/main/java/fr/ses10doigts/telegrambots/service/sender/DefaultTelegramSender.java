package fr.ses10doigts.telegrambots.service.sender;

import fr.ses10doigts.telegrambots.configuration.TelegramRetryProperties;
import fr.ses10doigts.telegrambots.model.TelegramButtonView;
import fr.ses10doigts.telegrambots.model.TelegramView;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DefaultTelegramSender implements TelegramSender {


    private final TelegramClient client;
    private final TelegramRetryProperties retryProperties;

    public DefaultTelegramSender(String botToken, TelegramRetryProperties retryProperties) {
        this.client = new OkHttpTelegramClient(botToken);
        this.retryProperties = retryProperties;
    }

    @Override
    public void sendMessage(Long chatId, String text) {
        sendText(chatId, text, null);
    }

    @Override
    public void sendMarkdownMessage(Long chatId, String text) {
        sendText(chatId, TelegramMarkdownUtils.escapeMarkdownV2(text), "MarkdownV2");
    }

    @Override
    public void sendMarkdownMessagePreservingLinks(Long chatId, String text) {
        sendText(chatId, TelegramMarkdownUtils.escapeMarkdownV2PreservingLinks(text), "MarkdownV2");
    }

    @Override
    public void sendView(Long chatId, TelegramView view) {
        if (view == null) {
            log.warn("TelegramView is null, nothing sent for chatId={}", chatId);
            return;
        }

        String text = view.getText();
        boolean hasButtons = view.getButtons() != null && !view.getButtons().isEmpty();

        if (!hasButtons && (text == null || text.isBlank())) {
            log.warn("TelegramView text is blank and no buttons are present, nothing sent for chatId={}", chatId);
            return;
        }

        if (!hasButtons) {
            sendMessage(chatId, text);
            return;
        }

        try {
            String effectiveText = (text == null || text.isBlank()) ? "Question :" : text;

            SendMessage sendMessage = new SendMessage(chatId.toString(), effectiveText);
            sendMessage.setDisableWebPagePreview(true);
            sendMessage.setReplyMarkup(buildInlineKeyboard(view.getButtons()));

            executeWithRetry("sendView", () -> client.execute(sendMessage));
        } catch (Exception e) {
            log.error("Telegram sendView error", e);
        }
    }

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

    private void sendText(Long chatId, String text, String parseMode) {
        try {
            SendMessage sendMessage = new SendMessage(chatId.toString(), text);
            sendMessage.setDisableWebPagePreview(true);

            if (parseMode != null) {
                sendMessage.setParseMode(parseMode);
            }

            executeWithRetry("sendMessage", () -> client.execute(sendMessage));
        } catch (Exception e) {
            log.error("Telegram sendMessage error", e);
        }
    }

    @Override
    public void sendPhoto(Long chatId, String photoPath, String caption) {
        try {
            SendPhoto sendPhoto = new SendPhoto(
                    chatId.toString(),
                    new InputFile(new File(photoPath))
            );
            sendPhoto.setCaption(caption);
            executeWithRetry("sendPhoto", () -> client.execute(sendPhoto));
        } catch (Exception e) {
            log.error("Telegram sendPhoto error", e);
        }
    }

    @Override
    public void sendDocument(Long chatId, String documentPath, String caption) {
        try {
            SendDocument sendDocument = new SendDocument(
                    chatId.toString(),
                    new InputFile(new File(documentPath))
            );
            sendDocument.setCaption(caption);
            executeWithRetry("sendDocument", () -> client.execute(sendDocument));
        } catch (Exception e) {
            log.error("Telegram sendDocument error", e);
        }
    }

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
                    try {
                        Thread.sleep(delayMillis);
                    } catch (InterruptedException interruptedException) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("Telegram retry interrupted", interruptedException);
                    }
                }
            }
        }

        throw lastException;
    }

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

}