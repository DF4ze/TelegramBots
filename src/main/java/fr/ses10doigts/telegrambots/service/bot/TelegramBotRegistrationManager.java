package fr.ses10doigts.telegrambots.service.bot;

import fr.ses10doigts.telegrambots.configuration.TelegramBotProperties;
import fr.ses10doigts.telegrambots.configuration.TelegramProperties;
import fr.ses10doigts.telegrambots.configuration.TelegramRetryProperties;
import fr.ses10doigts.telegrambots.service.poller.TelegramPollingBotAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class TelegramBotRegistrationManager implements ApplicationListener<ContextRefreshedEvent>, AutoCloseable {

    private final TelegramProperties properties;
    private final TelegramBotRegistry botRegistry;
    private final List<TelegramPollingBotAdapter> pollingBots;
    
    private TelegramBotsLongPollingApplication botsApplication;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (!properties.isEnabled() || pollingBots.isEmpty()) {
            return;
        }

        botsApplication = new TelegramBotsLongPollingApplication();

        for (TelegramPollingBotAdapter bot : pollingBots) {
            String botId = bot.getBotId();
            TelegramBotProperties botProps = botRegistry.getRequiredBot(botId);
            TelegramRetryProperties retryProps = botProps.getRetry();

            registerBotWithRetry(bot, botId, retryProps);
        }
    }

    private void registerBotWithRetry(TelegramPollingBotAdapter bot, String botId, TelegramRetryProperties retryProps) {
        int maxAttempts = retryProps.isEnabled() ? Math.max(1, retryProps.getMaxAttempts()) : 1;
        int delaySeconds = retryProps.getDelaySeconds();

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                log.info("Registering bot '{}' (attempt {}/{})", botId, attempt, maxAttempts);
                botsApplication.registerBot(bot.getBotToken(), bot.getUpdatesConsumer());
                log.info("Bot '{}' registered successfully", botId);
                return;
            } catch (Exception e) {
                log.error("Failed to register bot '{}' on attempt {}/{}", botId, attempt, maxAttempts, e);

                if (attempt < maxAttempts) {
                    long currentDelay = (long) delaySeconds * attempt * 1000L; // Délai qui s'allonge
                    log.info("Waiting {}ms before next attempt for bot '{}'", currentDelay, botId);
                    try {
                        Thread.sleep(currentDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted during bot registration retry", ie);
                    }
                } else {
                    log.error("Bot '{}' could not be registered after {} attempts", botId, maxAttempts);
                }
            }
        }
    }

    @Override
    public void close() throws Exception {
        if (botsApplication != null) {
            botsApplication.close();
        }
    }
}
