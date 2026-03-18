package fr.ses10doigts.telegrambots.service.bot;

import fr.ses10doigts.telegrambots.configuration.TelegramBotProperties;
import fr.ses10doigts.telegrambots.configuration.TelegramProperties;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class TelegramBotRegistry {

    private final Map<String, TelegramBotProperties> botsById;

    public TelegramBotRegistry(TelegramProperties properties) {
        this.botsById = indexBots(properties);
    }

    private Map<String, TelegramBotProperties> indexBots(TelegramProperties properties) {
        Map<String, TelegramBotProperties> map = new LinkedHashMap<>();

        if (properties == null || properties.getBots() == null) {
            return map;
        }

        for (TelegramBotProperties bot : properties.getBots()) {
            map.put(bot.getId(), bot);
        }

        return Map.copyOf(map);
    }

    public TelegramBotProperties getRequiredBot(String botId) {
        TelegramBotProperties bot = botsById.get(botId);

        if (bot == null) {
            throw new IllegalArgumentException("Unknown telegram bot id: " + botId);
        }

        return bot;
    }

    public TelegramBotProperties getBot(String botId) {
        return botsById.get(botId);
    }

    public boolean hasBot(String botId) {
        return botsById.containsKey(botId);
    }

    public Collection<TelegramBotProperties> getAllBots() {
        return botsById.values();
    }

    public boolean isEmpty() {
        return botsById.isEmpty();
    }

    public int size() {
        return botsById.size();
    }
}