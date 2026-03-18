package fr.ses10doigts.telegrambots.controllerToRemove;

import fr.ses10doigts.telegrambots.model.TelegramUpdateContext;
import fr.ses10doigts.telegrambots.service.poller.handler.annot.CallbackQuery;
import fr.ses10doigts.telegrambots.service.poller.handler.annot.Chat;
import fr.ses10doigts.telegrambots.service.poller.handler.annot.Command;
import fr.ses10doigts.telegrambots.service.poller.handler.annot.TelegramController;

@TelegramController
public class DemoTelegramController {

    @Command("/ping")
    public String ping(TelegramUpdateContext ctx) {
        System.out.println("PING : " + ctx.getText());
        return "pong";
    }

    @Chat
    public void chat(TelegramUpdateContext ctx) {
        System.out.println("CHAT : " + ctx.getText());
    }

    @Command(value = "/menu", description = "Afficher le menu")
    public String menu(TelegramUpdateContext ctx) {
        return "Choisis une action";
    }

    @CallbackQuery("BUY")
    public String buy(TelegramUpdateContext ctx) {
        return "Achat lancé";
    }

    @CallbackQuery("SELL")
    public String sell(TelegramUpdateContext ctx) {
        return "Vente lancée";
    }
}