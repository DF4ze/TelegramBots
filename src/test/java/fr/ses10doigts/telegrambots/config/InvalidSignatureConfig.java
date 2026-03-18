package fr.ses10doigts.telegrambots.config;

import fr.ses10doigts.telegrambots.controller.InvalidSignatureTelegramController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InvalidSignatureConfig {

    @Bean
    InvalidSignatureTelegramController invalidSignatureTelegramController() {
        return new InvalidSignatureTelegramController();
    }
}