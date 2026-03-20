package fr.ses10doigts.telegrambots.service.poller.handler;

import fr.ses10doigts.telegrambots.model.TelegramCommandDefinition;
import fr.ses10doigts.telegrambots.model.TelegramHandlerMethod;
import fr.ses10doigts.telegrambots.model.TelegramUpdateContext;
import fr.ses10doigts.telegrambots.service.poller.handler.annot.CallbackQuery;
import fr.ses10doigts.telegrambots.service.poller.handler.annot.Chat;
import fr.ses10doigts.telegrambots.service.poller.handler.annot.Command;
import fr.ses10doigts.telegrambots.service.poller.handler.annot.TelegramController;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TelegramHandlerRegistryTest {

    @Test
    void shouldRegisterHandlersAndResolveSpecificBotBeforeGlobal() {
        SampleGlobalController globalController = new SampleGlobalController();
        SampleBotController botController = new SampleBotController();

        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBeansWithAnnotation(TelegramController.class))
                .thenReturn(Map.of(
                        "globalController", globalController,
                        "botController", botController
                ));

        TelegramHandlerRegistry registry = new TelegramHandlerRegistry(applicationContext);
        registry.init();

        TelegramHandlerMethod globalCommand = registry.findCommandHandler("unknown-bot", "/global");
        assertThat(globalCommand).isNotNull();
        assertThat(globalCommand.bean()).isSameAs(globalController);
        assertThat(globalCommand.method().getName()).isEqualTo("handleGlobalCommand");

        TelegramHandlerMethod specificCommand = registry.findCommandHandler("bot-1", "/start");
        assertThat(specificCommand).isNotNull();
        assertThat(specificCommand.bean()).isSameAs(botController);
        assertThat(specificCommand.method().getName()).isEqualTo("handleStart");

        TelegramHandlerMethod fallbackCommand = registry.findCommandHandler("bot-1", "/global");
        assertThat(fallbackCommand).isNotNull();
        assertThat(fallbackCommand.bean()).isSameAs(globalController);

        TelegramHandlerMethod callbackHandler = registry.findCallbackHandler("bot-1", "confirm");
        assertThat(callbackHandler).isNotNull();
        assertThat(callbackHandler.bean()).isSameAs(botController);

        List<TelegramHandlerMethod> chatHandlers = registry.findChatHandlers("bot-1");
        assertThat(chatHandlers).hasSize(1);
        assertThat(chatHandlers.getFirst().bean()).isSameAs(botController);

        List<TelegramCommandDefinition> definitions = registry.getCommandDefinitions("bot-1");
        assertThat(definitions)
                .extracting(TelegramCommandDefinition::getCommand)
                .containsExactlyInAnyOrder("/global", "/start");
        assertThat(definitions)
                .filteredOn(def -> def.getCommand().equals("/start"))
                .first()
                .extracting(TelegramCommandDefinition::getDescription)
                .isEqualTo("Start command");
    }

    @Test
    void shouldRejectDuplicateCommandsForSameBot() {
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBeansWithAnnotation(TelegramController.class))
                .thenReturn(Map.of(
                        "first", new DuplicateCommandControllerA(),
                        "second", new DuplicateCommandControllerB()
                ));

        TelegramHandlerRegistry registry = new TelegramHandlerRegistry(applicationContext);

        assertThatThrownBy(registry::init)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate Telegram command handler for command '/dup'");
    }

    @Test
    void shouldRejectMoreThanOneChatHandlerPerBot() {
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBeansWithAnnotation(TelegramController.class))
                .thenReturn(Map.of(
                        "chat1", new ChatControllerA(),
                        "chat2", new ChatControllerB()
                ));

        TelegramHandlerRegistry registry = new TelegramHandlerRegistry(applicationContext);

        assertThatThrownBy(registry::init)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only one method annotated with @Chat is allowed");
    }

    @Test
    void shouldRejectInvalidCommandSignature() {
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBeansWithAnnotation(TelegramController.class))
                .thenReturn(Map.of("invalid", new InvalidSignatureController()));

        TelegramHandlerRegistry registry = new TelegramHandlerRegistry(applicationContext);

        assertThatThrownBy(registry::init)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid signature for @Command");
    }

    @TelegramController
    static class SampleGlobalController {

        @Command(value = "/global", description = "Global command")
        public void handleGlobalCommand(TelegramUpdateContext context) {
        }
    }

    @TelegramController(bot = "bot-1")
    static class SampleBotController {

        @Command(value = "/start", description = "Start command")
        public void handleStart(TelegramUpdateContext context) {
        }

        @Chat
        public void handleChat(TelegramUpdateContext context) {
        }

        @CallbackQuery("confirm")
        public void handleConfirm(TelegramUpdateContext context) {
        }
    }

    @TelegramController(bot = "bot-1")
    static class DuplicateCommandControllerA {

        @Command(value = "/dup", description = "First")
        public void first(TelegramUpdateContext context) {
        }
    }

    @TelegramController(bot = "bot-1")
    static class DuplicateCommandControllerB {

        @Command(value = "/dup", description = "Second")
        public void second(TelegramUpdateContext context) {
        }
    }

    @TelegramController(bot = "bot-1")
    static class ChatControllerA {

        @Chat
        public void first(TelegramUpdateContext context) {
        }
    }

    @TelegramController(bot = "bot-1")
    static class ChatControllerB {

        @Chat
        public void second(TelegramUpdateContext context) {
        }
    }

    @TelegramController
    static class InvalidSignatureController {

        @Command("/bad")
        public void invalidSignature() {
        }
    }
}