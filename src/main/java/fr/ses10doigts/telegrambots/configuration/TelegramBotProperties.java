package fr.ses10doigts.telegrambots.configuration;

import lombok.Data;

@Data
public class TelegramBotProperties {

    private String id;
    private String token;

    private boolean pollingEnabled = true;
    private boolean autoRegisterCommands = true;
    private boolean configureMenuButton = true;

    private TelegramSecurityProperties security = new TelegramSecurityProperties();
    private TelegramRetryProperties retry = new TelegramRetryProperties();
}