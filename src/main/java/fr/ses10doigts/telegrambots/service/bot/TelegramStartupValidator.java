package fr.ses10doigts.telegrambots.service.bot;

import fr.ses10doigts.telegrambots.configuration.TelegramBotProperties;
import fr.ses10doigts.telegrambots.configuration.TelegramProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
public class TelegramStartupValidator implements SmartInitializingSingleton {

    private final TelegramProperties properties;

    @Override
    public void afterSingletonsInstantiated() {
        if (!properties.isEnabled()) {
            return;
        }

        if (properties.getBots() == null || properties.getBots().isEmpty()) {
            throw new IllegalStateException("Telegram is enabled but no bots are configured");
        }

        Set<String> ids = new HashSet<>();
        Set<String> pollingTokens = new HashSet<>();

        for (TelegramBotProperties bot : properties.getBots()) {

            // id obligatoire
            if (!StringUtils.hasText(bot.getId())) {
                throw new IllegalStateException("Telegram bot id must not be empty");
            }

            // unicité id
            if (!ids.add(bot.getId())) {
                throw new IllegalStateException("Duplicate telegram bot id: " + bot.getId());
            }

            // token obligatoire
            if (!StringUtils.hasText(bot.getToken())) {
                throw new IllegalStateException("Telegram bot token must not be empty (botId=" + bot.getId() + ")");
            }

            // En long polling, un token ne doit etre enregistre qu'une seule fois par application.
            if (bot.isPollingEnabled() && !pollingTokens.add(bot.getToken())) {
                throw new IllegalStateException(
                        "Duplicate telegram bot token with polling enabled detected (botId=" + bot.getId()
                                + "). Use a unique token per polling bot or disable polling on duplicates."
                );
            }
        }
    }

}
