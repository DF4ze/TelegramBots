package fr.ses10doigts.telegrambots.controller;

import fr.ses10doigts.telegrambots.model.TelegramCommandDefinition;
import fr.ses10doigts.telegrambots.model.TelegramUpdateContext;
import fr.ses10doigts.telegrambots.service.poller.handler.TelegramHandlerRegistry;
import fr.ses10doigts.telegrambots.service.poller.handler.annot.Command;
import fr.ses10doigts.telegrambots.service.poller.handler.annot.TelegramController;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;

@TelegramController
@RequiredArgsConstructor
public class TelegramBuiltinController {

    private final ObjectProvider<TelegramHandlerRegistry> registryProvider;

    @Command(value="/whoami", description = "Get the 'lib' bot name")
    public String whoami(TelegramUpdateContext context) {
        return "I am " + context.getBotId();
    }

    @Command(value = "/help", description = "List available commands")
    public String help(TelegramUpdateContext context) {
        TelegramHandlerRegistry registry = registryProvider.getObject();
        List<TelegramCommandDefinition> commands = registry.getCommandDefinitions(context.getBotId());

        if (commands.isEmpty()) {
            return "No command available.";
        }

        StringBuilder message = new StringBuilder("Available commands:\n");
        for (TelegramCommandDefinition definition : commands) {
            message.append("- ")
                    .append(definition.getCommand())
                    .append(" : ")
                    .append(safeDescription(definition))
                    .append('\n');
        }

        return message.toString().trim();
    }

    private String safeDescription(TelegramCommandDefinition definition) {
        if (definition.getDescription() == null || definition.getDescription().isBlank()) {
            return definition.getCommand();
        }

        return definition.getDescription();
    }

}
