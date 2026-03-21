package fr.ses10doigts.telegrambots.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "telegram")
public class TelegramProperties {

    private boolean enabled = false;
    private String defaultBotId;

    private List<TelegramBotProperties> bots = new ArrayList<>();
}