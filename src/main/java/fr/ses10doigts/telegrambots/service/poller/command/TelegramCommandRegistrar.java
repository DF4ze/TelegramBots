package fr.ses10doigts.telegrambots.service.poller.command;

import fr.ses10doigts.telegrambots.configuration.TelegramBotProperties;
import fr.ses10doigts.telegrambots.model.TelegramCommandDefinition;
import fr.ses10doigts.telegrambots.service.bot.TelegramBotRegistry;
import fr.ses10doigts.telegrambots.service.poller.handler.TelegramHandlerRegistry;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class TelegramCommandRegistrar implements SmartInitializingSingleton {

    private final TelegramHandlerRegistry registry;
    private final TelegramBotRegistry telegramBotRegistry;

    @Override
    public void afterSingletonsInstantiated() {
        List<BotCommand> commands = registry.getCommandDefinitions().stream()
                .map(this::toBotCommand)
                .toList();

        if (commands.isEmpty()) {
            log.info("No Telegram commands to register");
            return;
        }

        for (TelegramBotProperties bot : telegramBotRegistry.getAllBots()) {
            List<BotCommand> botCommands = registry.getCommandDefinitions(bot.getId()).stream()
                    .map(this::toBotCommand)
                    .toList();
            registerCommandsForBot(bot, botCommands);
        }
    }

    private void registerCommandsForBot(TelegramBotProperties bot, List<BotCommand> commands) {
        if (!bot.isAutoRegisterCommands()) {
            log.info("Skipping Telegram command registration for bot '{}' because autoRegisterCommands=false", bot.getId());
            return;
        }

        TelegramClient telegramClient = new OkHttpTelegramClient(bot.getToken());

        try {
            telegramClient.execute(SetMyCommands.builder()
                    .commands(commands)
                    .build());

            log.info("Telegram menu commands registered for bot '{}' : {}", bot.getId(), commands.size());

            if (bot.isConfigureMenuButton()) {
                telegramClient.execute(SetChatMenuButton.builder()
                        .menuButton(new MenuButtonCommands())
                        .build());

                log.info("Telegram menu button configured to commands for bot '{}'", bot.getId());
            }

        } catch (TelegramApiException e) {
            log.error("Failed to register Telegram commands/menu for bot '{}'", bot.getId(), e);
        }
    }

    private BotCommand toBotCommand(TelegramCommandDefinition definition) {
        return new BotCommand(
                stripSlash(definition.getCommand()),
                safeDescription(definition)
        );
    }

    private String stripSlash(String command) {
        return command != null && command.startsWith("/")
                ? command.substring(1)
                : command;
    }

    private String safeDescription(TelegramCommandDefinition definition) {
        if (definition.getDescription() == null || definition.getDescription().isBlank()) {
            return definition.getCommand();
        }
        return definition.getDescription();
    }

}