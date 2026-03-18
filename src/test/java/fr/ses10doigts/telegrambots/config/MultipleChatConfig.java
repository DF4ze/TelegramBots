package fr.ses10doigts.telegrambots.config;

import fr.ses10doigts.telegrambots.controller.FirstTelegramController;
import fr.ses10doigts.telegrambots.controller.SecondTelegramController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MultipleChatConfig {

    @Bean
    FirstTelegramController firstTelegramController() {
        return new FirstTelegramController();
    }

    @Bean
    SecondTelegramController secondTelegramController() {
        return new SecondTelegramController();
    }
}