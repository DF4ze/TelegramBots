package fr.ses10doigts.telegrambots.controller;

import fr.ses10doigts.telegrambots.model.TelegramUpdateContext;
import fr.ses10doigts.telegrambots.service.poller.handler.annot.Chat;
import fr.ses10doigts.telegrambots.service.poller.handler.annot.TelegramController;

@TelegramController
public class SecondTelegramController {
    @Chat
    public void second(TelegramUpdateContext ctx) {
    }
}
