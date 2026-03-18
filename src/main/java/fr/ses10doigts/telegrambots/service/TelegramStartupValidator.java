package fr.ses10doigts.telegrambots.service;

import fr.ses10doigts.telegrambots.configuration.TelegramProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.SmartInitializingSingleton;

@RequiredArgsConstructor
public class TelegramStartupValidator implements SmartInitializingSingleton {

    private final TelegramProperties properties;

    @Override
    public void afterSingletonsInstantiated() {
        if (properties.isPollingEnabled() && isBlank(properties.getBotToken())) {
            throw new IllegalStateException(
                    "telegram.bot-token must be defined when telegram.polling-enabled=true"
            );
        }

        if (properties.getAllowedUserIds() != null) {
            for (Long userId : properties.getAllowedUserIds()) {
                if (userId == null) {
                    throw new IllegalStateException(
                            "telegram.allowed-user-ids contains an invalid value: " + userId
                    );
                }
            }
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}