package fr.ses10doigts.telegrambots.configuration;

import lombok.Data;

@Data
public class TelegramRetryProperties {

    private boolean enabled = false;
    private int maxAttempts = 3;
    private int delaySeconds = 10;
}