package fr.ses10doigts.telegrambots.configuration;

import fr.ses10doigts.telegrambots.service.TelegramStartupValidator;
import fr.ses10doigts.telegrambots.service.poller.TelegramPollingBotAdapter;
import fr.ses10doigts.telegrambots.service.poller.TelegramUpdateDispatcher;
import fr.ses10doigts.telegrambots.service.poller.handler.TelegramHandlerRegistry;
import fr.ses10doigts.telegrambots.service.poller.handler.command.TelegramCommandRegistrar;
import fr.ses10doigts.telegrambots.service.sender.DefaultTelegramSender;
import fr.ses10doigts.telegrambots.service.sender.TelegramSender;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({TelegramProperties.class, TelegramRetryProperties.class})

public class TelegramAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "telegram", name = "bot-token")
    public TelegramSender telegramSender(TelegramProperties properties, TelegramRetryProperties retryProperties) {
        return new DefaultTelegramSender(properties.getBotToken(), retryProperties);
    }

    @Bean
    @ConditionalOnProperty(prefix = "telegram", name = "polling-enabled", havingValue = "true")
    public TelegramHandlerRegistry telegramHandlerRegistry(ApplicationContext applicationContext) {
        return new TelegramHandlerRegistry(applicationContext);
    }

    @Bean
    @ConditionalOnProperty(prefix = "telegram", name = "polling-enabled", havingValue = "true")
    public TelegramUpdateDispatcher telegramUpdateDispatcher(
            TelegramHandlerRegistry registry,
            TelegramSender telegramSender,
            TelegramProperties properties) {

        return new TelegramUpdateDispatcher(registry, telegramSender, properties);
    }

    @Bean
    @ConditionalOnProperty(prefix = "telegram", name = "polling-enabled", havingValue = "true")
    public TelegramPollingBotAdapter telegramPollingBotAdapter(
            TelegramProperties properties,
            TelegramUpdateDispatcher dispatcher) {
        return new TelegramPollingBotAdapter(properties.getBotToken(), dispatcher);
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "telegram",
            name = {"polling-enabled", "auto-register-commands"},
            havingValue = "true")
    public TelegramCommandRegistrar telegramCommandRegistrar(
            TelegramHandlerRegistry registry,
            TelegramProperties properties) {
        return new TelegramCommandRegistrar(registry, properties);
    }

    @Bean
    public TelegramStartupValidator telegramStartupValidator(TelegramProperties properties) {
        return new TelegramStartupValidator(properties);
    }

}