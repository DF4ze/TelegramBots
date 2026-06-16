package fr.ses10doigts.telegrambots.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "telegram")
public class TelegramProperties {

    private boolean enabled = false;
    private boolean builtinControllerEnabled = true;
    private String defaultBotId;
    @NestedConfigurationProperty
    private TelegramPollingLoggingProperties pollingLogging = new TelegramPollingLoggingProperties();

    private List<TelegramBotProperties> bots = new ArrayList<>();
}
