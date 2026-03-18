package fr.ses10doigts.telegrambots.configuration;


import fr.ses10doigts.telegrambots.service.bot.CurrentTelegramBotContext;
import fr.ses10doigts.telegrambots.service.bot.TelegramBotRegistry;
import fr.ses10doigts.telegrambots.service.bot.TelegramStartupValidator;
import fr.ses10doigts.telegrambots.service.poller.TelegramPollingBotAdapter;
import fr.ses10doigts.telegrambots.service.poller.TelegramUpdateDispatcher;
import fr.ses10doigts.telegrambots.service.poller.handler.TelegramHandlerRegistry;
import fr.ses10doigts.telegrambots.service.poller.command.TelegramCommandRegistrar;
import fr.ses10doigts.telegrambots.service.sender.ContextAwareTelegramSender;
import fr.ses10doigts.telegrambots.service.sender.TelegramSender;
import fr.ses10doigts.telegrambots.service.sender.TelegramSenderRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableConfigurationProperties(TelegramProperties.class)
public class TelegramAutoConfiguration {

    @Bean
    public TelegramStartupValidator telegramStartupValidator(TelegramProperties properties) {
        return new TelegramStartupValidator(properties);
    }

    @Bean
    @ConditionalOnProperty(prefix = "telegram", name = "enabled", havingValue = "true")
    public CurrentTelegramBotContext currentTelegramBotContext() {
        return new CurrentTelegramBotContext();
    }

    @Bean
    @ConditionalOnProperty(prefix = "telegram", name = "enabled", havingValue = "true")
    public TelegramBotRegistry telegramBotRegistry(TelegramProperties properties) {
        return new TelegramBotRegistry(properties);
    }

    @Bean
    @ConditionalOnProperty(prefix = "telegram", name = "enabled", havingValue = "true")
    public TelegramSenderRegistry telegramSenderRegistry(TelegramBotRegistry botRegistry) {
        return new TelegramSenderRegistry(botRegistry);
    }

    @Bean
    @ConditionalOnProperty(prefix = "telegram", name = "enabled", havingValue = "true")
    public TelegramSender telegramSender(
            TelegramSenderRegistry telegramSenderRegistry,
            CurrentTelegramBotContext currentTelegramBotContext
    ) {
        return new ContextAwareTelegramSender(telegramSenderRegistry, currentTelegramBotContext);
    }

    @Bean
    @ConditionalOnProperty(prefix = "telegram", name = "enabled", havingValue = "true")
    public TelegramHandlerRegistry telegramHandlerRegistry(ApplicationContext applicationContext) {
        return new TelegramHandlerRegistry(applicationContext);
    }

    @Bean
    @ConditionalOnProperty(prefix = "telegram", name = "enabled", havingValue = "true")
    public TelegramUpdateDispatcher telegramUpdateDispatcher(
            TelegramHandlerRegistry registry,
            TelegramSender telegramSender,
            CurrentTelegramBotContext currentTelegramBotContext,
            TelegramBotRegistry telegramBotRegistry
    ) {
        return new TelegramUpdateDispatcher(registry, telegramSender, currentTelegramBotContext, telegramBotRegistry);
    }

    @Bean
    @ConditionalOnProperty(prefix = "telegram", name = "enabled", havingValue = "true")
    public List<SpringLongPollingBot> telegramPollingBots(
            TelegramBotRegistry botRegistry,
            TelegramUpdateDispatcher dispatcher,
            CurrentTelegramBotContext currentTelegramBotContext
    ) {
        List<SpringLongPollingBot> bots = new ArrayList<>();

        for (TelegramBotProperties bot : botRegistry.getAllBots()) {
            if (bot.isPollingEnabled()) {
                bots.add(new TelegramPollingBotAdapter(
                        bot.getId(),
                        bot.getToken(),
                        dispatcher,
                        currentTelegramBotContext
                ));
            }
        }

        return bots;
    }

    @Bean
    @ConditionalOnProperty(prefix = "telegram", name = "enabled", havingValue = "true")
    public TelegramCommandRegistrar telegramCommandRegistrar(
            TelegramHandlerRegistry registry,
            TelegramBotRegistry botRegistry
    ) {
        return new TelegramCommandRegistrar(registry, botRegistry);
    }
}