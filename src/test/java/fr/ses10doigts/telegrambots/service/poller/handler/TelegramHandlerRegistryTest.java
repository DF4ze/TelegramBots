package fr.ses10doigts.telegrambots.service.poller.handler;

import fr.ses10doigts.telegrambots.config.InvalidSignatureConfig;
import fr.ses10doigts.telegrambots.config.MultipleChatConfig;
import fr.ses10doigts.telegrambots.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TelegramHandlerRegistryTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class);

    private final ApplicationContextRunner contextRunner2 = new ApplicationContextRunner()
            .withUserConfiguration(MultipleChatConfig.class);

    private final ApplicationContextRunner contextRunner3 = new ApplicationContextRunner()
            .withUserConfiguration(InvalidSignatureConfig.class);

    @Test
    void shouldFailWhenCommandSignatureIsInvalid() {
        contextRunner3.run(context -> {
            TelegramHandlerRegistry registry = new TelegramHandlerRegistry(context);

            assertThatThrownBy(registry::init)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Invalid signature for @Command");
        });
    }

    @Test
    void shouldFailWhenMoreThanOneChatHandlerExists() {
        contextRunner2.run(context -> {
            TelegramHandlerRegistry registry = new TelegramHandlerRegistry(context);

            assertThatThrownBy(registry::init)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Only one method annotated with @Chat is allowed");
        });
    }

    @Test
    void shouldRegisterCommandAndChatHandlers() {
        contextRunner.run(context -> {
            TelegramHandlerRegistry registry = new TelegramHandlerRegistry(context);
            registry.init();

            assertThat(registry.getCommandHandlers()).containsKey("/ping");
            assertThat(registry.getChatHandlers()).hasSize(1);
            assertThat(registry.getCommandDefinitions()).hasSize(1);
        });
    }
}