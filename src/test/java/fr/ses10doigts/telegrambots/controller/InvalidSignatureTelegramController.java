package fr.ses10doigts.telegrambots.controller;

import fr.ses10doigts.telegrambots.service.poller.handler.annot.Command;
import fr.ses10doigts.telegrambots.service.poller.handler.annot.TelegramController;

@TelegramController
public class InvalidSignatureTelegramController {

    @Command("/ping")
    public String bad(String value) {
        return "pong";
    }
}