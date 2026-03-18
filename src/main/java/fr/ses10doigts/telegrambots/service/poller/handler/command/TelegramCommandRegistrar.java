package fr.ses10doigts.telegrambots.service.poller.handler.command;

import fr.ses10doigts.telegrambots.configuration.TelegramProperties;
import fr.ses10doigts.telegrambots.model.TelegramCommandDefinition;
import fr.ses10doigts.telegrambots.service.poller.handler.TelegramHandlerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.menubutton.SetChatMenuButton;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.menubutton.MenuButtonCommands;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

@Slf4j
public class TelegramCommandRegistrar implements SmartInitializingSingleton {

    private final TelegramHandlerRegistry registry;
    private final TelegramProperties properties;
    private final TelegramClient telegramClient;

    public TelegramCommandRegistrar(TelegramHandlerRegistry registry,
                                    TelegramProperties properties) {
        this.registry = registry;
        this.properties = properties;
        this.telegramClient = new OkHttpTelegramClient(properties.getBotToken());
    }

    @Override
    public void afterSingletonsInstantiated() {
        if (!properties.isAutoRegisterCommands()) {
            return;
        }

        List<BotCommand> commands = registry.getCommandDefinitions().stream()
                .map(def -> new BotCommand(stripSlash(def.getCommand()), safeDescription(def)))
                .toList();

        if (commands.isEmpty()) {
            log.info("No Telegram commands to register");
            return;
        }

        try {
            telegramClient.execute(SetMyCommands.builder()
                    .commands(commands)
                    .build());

            log.info("Telegram commands registered: {}", commands.size());

            if (properties.isConfigureMenuButton()) {
                telegramClient.execute(SetChatMenuButton.builder()
                        .menuButton(new MenuButtonCommands())
                        .build());

                log.info("Telegram menu button configured to commands");
            }

        } catch (TelegramApiException e) {
            log.error("Failed to register Telegram commands/menu", e);
        }
    }

    private String stripSlash(String command) {
        return command.startsWith("/") ? command.substring(1) : command;
    }

    private String safeDescription(TelegramCommandDefinition def) {
        if (def.getDescription() == null || def.getDescription().isBlank()) {
            return def.getCommand();
        }
        return def.getDescription();
    }
}