package fr.ses10doigts.telegrambots.config;

import fr.ses10doigts.telegrambots.controller.DemoTelegramControllerTest;
import fr.ses10doigts.telegrambots.controllerToRemove.DemoTelegramController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestConfig {

    @Bean
    DemoTelegramControllerTest demoTelegramController() {
        return new DemoTelegramControllerTest();
    }
}