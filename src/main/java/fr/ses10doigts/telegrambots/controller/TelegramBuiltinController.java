package fr.ses10doigts.telegrambots.controller;

import fr.ses10doigts.telegrambots.model.TelegramUpdateContext;
import fr.ses10doigts.telegrambots.service.poller.handler.annot.Command;
import fr.ses10doigts.telegrambots.service.poller.handler.annot.TelegramController;

@TelegramController
public class TelegramBuiltinController {

    @Command(value="/whoami", description = "Get the 'lib' bot name")
    public String whoami(TelegramUpdateContext context) {
        return "I am " + context.getBotId();
    }

}