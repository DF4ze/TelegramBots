package fr.ses10doigts.telegrambots.controller;

import fr.ses10doigts.telegrambots.model.TelegramUpdateContext;
import fr.ses10doigts.telegrambots.service.poller.handler.annot.Chat;
import fr.ses10doigts.telegrambots.service.poller.handler.annot.Command;
import fr.ses10doigts.telegrambots.service.poller.handler.annot.TelegramController;

@TelegramController
public class DemoTelegramControllerTest {

    @Command(value = "/ping", description = "Ping command")
    public String ping(TelegramUpdateContext ctx) {
        return "pong";
    }

    @Chat
    public void chat(TelegramUpdateContext ctx) {
    }
}