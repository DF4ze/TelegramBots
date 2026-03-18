package fr.ses10doigts.telegrambots.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashSet;
import java.util.Set;

@Data
@ConfigurationProperties(prefix = "telegram")
public class TelegramProperties {

    private String botToken;
    private boolean pollingEnabled = false;
    private boolean enabled = false;

    private Set<Long> allowedUserIds = new HashSet<>();

    private boolean autoRegisterCommands = true;
    private boolean configureMenuButton = true;
}