package fr.ses10doigts.telegrambots;

import fr.ses10doigts.telegrambots.service.sender.TelegramSender;
import fr.ses10doigts.telegrambots.service.sender.TelegramSenderRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class TelegramApplication implements CommandLineRunner {
    @Autowired
    private TelegramSenderRegistry telegramSenderRegistry;

    public static void main(String[] args) {
        SpringApplication.run(TelegramApplication.class, args);
    }

    public void run(String... args){ // TODO remove with CommandLineRunner implements
        log.info("call is called");
        TelegramSender sender = telegramSenderRegistry.getRequiredSender("main-bot");
        sender.sendMessage(1595302518L, "Qu'allons nous faire ce soir?");
    }
}
