package fr.ses10doigts.telegrambots.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "telegram.retry")
public class TelegramRetryProperties {

    private boolean enabled = false;
    private int maxAttempts = 1;
    private int delaySeconds = 1;
}