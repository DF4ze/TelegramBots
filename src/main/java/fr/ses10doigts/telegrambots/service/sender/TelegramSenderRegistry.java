package fr.ses10doigts.telegrambots.service.sender;

import fr.ses10doigts.telegrambots.configuration.TelegramBotProperties;
import fr.ses10doigts.telegrambots.service.bot.TelegramBotRegistry;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class TelegramSenderRegistry {

    private final Map<String, TelegramSender> sendersByBotId;

    public TelegramSenderRegistry(TelegramBotRegistry botRegistry) {
        Map<String, TelegramSender> map = new LinkedHashMap<>();

        for (TelegramBotProperties bot : botRegistry.getAllBots()) {
            map.put(bot.getId(), new DefaultTelegramSender(bot.getToken(), bot.getRetry()));
        }

        this.sendersByBotId = Map.copyOf(map);
    }

    public TelegramSender getRequiredSender(String botId) {
        TelegramSender sender = sendersByBotId.get(botId);

        if (sender == null) {
            throw new IllegalArgumentException("No telegram sender found for bot id: " + botId);
        }

        return sender;
    }

    public boolean hasSender(String botId) {
        return sendersByBotId.containsKey(botId);
    }

    public Collection<TelegramSender> getAllSenders() {
        return sendersByBotId.values();
    }
}