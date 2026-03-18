package fr.ses10doigts.telegrambots.controllerToRemove;

import fr.ses10doigts.telegrambots.model.TelegramButtonView;
import fr.ses10doigts.telegrambots.model.TelegramUpdateContext;
import fr.ses10doigts.telegrambots.model.TelegramView;
import fr.ses10doigts.telegrambots.service.poller.handler.annot.CallbackQuery;
import fr.ses10doigts.telegrambots.service.poller.handler.annot.Chat;
import fr.ses10doigts.telegrambots.service.poller.handler.annot.Command;
import fr.ses10doigts.telegrambots.service.poller.handler.annot.TelegramController;

import java.util.List;

@TelegramController
public class DemoTelegramController {

    @Command("/ping")
    public String ping(TelegramUpdateContext ctx) {
        System.out.println("PING : " + ctx.getText());
        return "pong";
    }

    @Chat
    public String chat(TelegramUpdateContext ctx) {
        System.out.println("CHAT : " + ctx.getText());
        return ctx.getText();
    }

    @Command(value = "/menu", description = "Afficher le menu")
    public TelegramView menu(TelegramUpdateContext ctx) {
        return TelegramView.builder()
                .text("Que veux-tu faire ?")
                .buttons(List.of(
                        List.of(
                                new TelegramButtonView("Buy", "buy"),
                                new TelegramButtonView("Sell", "sell")
                        )
                ))
                .build();
    }

    @CallbackQuery("buy")
    public TelegramView buy(TelegramUpdateContext ctx) {
        return TelegramView.builder()
                .buttons(List.of(
                        List.of(
                                new TelegramButtonView("Market", "market"),
                                new TelegramButtonView("Limit", "limit")
                        )
                ))
                .build();
    }

    @CallbackQuery("sell")
    public String sell(TelegramUpdateContext ctx) {
        return "Vente lancée";
    }

    @CallbackQuery("market")
    public String market(TelegramUpdateContext ctx) {
        return "Achat Market";
    }

    @CallbackQuery("limit")
    public String limit(TelegramUpdateContext ctx) {
        return "Achat Limit";
    }
}