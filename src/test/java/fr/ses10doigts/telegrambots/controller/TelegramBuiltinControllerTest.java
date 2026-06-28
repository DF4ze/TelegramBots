package fr.ses10doigts.telegrambots.controller;

import fr.ses10doigts.telegrambots.model.TelegramCommandDefinition;
import fr.ses10doigts.telegrambots.model.TelegramUpdateContext;
import fr.ses10doigts.telegrambots.service.poller.handler.TelegramHandlerRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TelegramBuiltinControllerTest {

    @Test
    void helpShouldRenderMergedCommandList() {
        TelegramHandlerRegistry registry = mock(TelegramHandlerRegistry.class);
        when(registry.getCommandDefinitions("bot-1")).thenReturn(List.of(
                new TelegramCommandDefinition("/whoami", "Get the 'lib' bot name"),
                new TelegramCommandDefinition("/start", "Start command"),
                new TelegramCommandDefinition("/help", "List available commands")
        ));

        TelegramBuiltinController controller = new TelegramBuiltinController(providerOf(registry));

        String result = controller.help(context("bot-1"));

        assertThat(result).isEqualTo(
                """
                        Available commands:
                        - /whoami : Get the 'lib' bot name
                        - /start : Start command
                        - /help : List available commands"""
        );
    }

    @Test
    void helpShouldFallbackToCommandWhenDescriptionIsMissing() {
        TelegramHandlerRegistry registry = mock(TelegramHandlerRegistry.class);
        when(registry.getCommandDefinitions("bot-1")).thenReturn(List.of(
                new TelegramCommandDefinition("/help", "")
        ));

        TelegramBuiltinController controller = new TelegramBuiltinController(providerOf(registry));

        String result = controller.help(context("bot-1"));

        assertThat(result).isEqualTo("Available commands:\n- /help : /help");
    }

    private TelegramUpdateContext context(String botId) {
        return new TelegramUpdateContext(
                botId,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(),
                false,
                null
        );
    }

    private ObjectProvider<TelegramHandlerRegistry> providerOf(TelegramHandlerRegistry registry) {
        @SuppressWarnings("unchecked")
        ObjectProvider<TelegramHandlerRegistry> provider = mock(ObjectProvider.class);
        when(provider.getObject()).thenReturn(registry);
        return provider;
    }
}
