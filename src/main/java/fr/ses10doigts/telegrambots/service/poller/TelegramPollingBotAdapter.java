package fr.ses10doigts.telegrambots.service.poller;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
public class TelegramPollingBotAdapter implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private final String botToken;
    private final TelegramUpdateDispatcher dispatcher;

    public TelegramPollingBotAdapter(String botToken, TelegramUpdateDispatcher dispatcher) {
        this.botToken = botToken;
        this.dispatcher = dispatcher;
    }

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
        dispatcher.dispatch(update);
    }
}