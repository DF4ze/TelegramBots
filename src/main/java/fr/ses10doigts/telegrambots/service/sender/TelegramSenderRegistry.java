package fr.ses10doigts.telegrambots.service.sender;

import fr.ses10doigts.telegrambots.configuration.TelegramBotProperties;
import fr.ses10doigts.telegrambots.service.bot.TelegramBotRegistry;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class TelegramSenderRegistry {

    private final Map<String, TelegramSender> sendersByBotId;
    private final String defaultBotId;


    public TelegramSenderRegistry(TelegramBotRegistry botRegistry,
                                  String defaultBotId) {
        Map<String, TelegramSender> map = new LinkedHashMap<>();

        for (TelegramBotProperties bot : botRegistry.getAllBots()) {
            map.put(bot.getId(), new DefaultTelegramSender(bot.getToken(), bot.getRetry()));
        }

        this.defaultBotId = defaultBotId;
        this.sendersByBotId = Map.copyOf(map);
    }

    public TelegramSender getDefaultBotSender() {
        if (defaultBotId == null) {
            throw new IllegalStateException("No default bot configured");
        }
        return getRequiredSender(defaultBotId);
    }

    public TelegramSender getRequiredSender(String botId) {
        TelegramSender sender = sendersByBotId.get(botId);

        if (sender == null) {
            throw new IllegalArgumentException("No telegram sender found for bot id: " + botId);
        }

        return sender;
    }
/*
    public boolean hasSender(String botId) {
        return sendersByBotId.containsKey(botId);
    }

    public Collection<TelegramSender> getAllSenders() {
        return sendersByBotId.values();
    }
    */
}