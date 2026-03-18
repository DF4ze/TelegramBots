package fr.ses10doigts.telegrambots.service.poller;

import fr.ses10doigts.telegrambots.service.bot.CurrentTelegramBotContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@RequiredArgsConstructor
public class TelegramPollingBotAdapter implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private final String botId;
    private final String botToken;
    private final TelegramUpdateDispatcher dispatcher;
    private final CurrentTelegramBotContext botContext;


    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {
        if (!StringUtils.hasText(botId)) {
            throw new IllegalStateException("Telegram bot id must not be empty");
        }

        try {
            botContext.setCurrentBotId(botId);
            dispatcher.dispatch(update);
        } finally {
            botContext.clear();
        }
    }
}