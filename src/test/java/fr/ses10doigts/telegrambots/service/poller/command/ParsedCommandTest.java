package fr.ses10doigts.telegrambots.service.poller.command;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ParsedCommandTest {

    @Test
    void extractCommandShouldHandleBotSuffixAndNonCommandTexts() {
        assertThat(ParsedCommand.extractCommand("/start@my_bot arg1 arg2")).isEqualTo("/start");
        assertThat(ParsedCommand.extractCommand("hello world")).isNull();
        assertThat(ParsedCommand.extractCommand("   ")).isNull();
    }

    @Test
    void parseShouldExtractRawArgumentsAndSplitArguments() {
        ParsedCommand parsed = ParsedCommand.parse("/search one two three");

        assertThat(parsed.getCommand()).isEqualTo("/search");
        assertThat(parsed.getArgsRaw()).isEqualTo("one two three");
        assertThat(parsed.getArgs()).containsExactly("one", "two", "three");
    }

    @Test
    void parseShouldStripBotSuffixAndKeepNoArgContract() {
        ParsedCommand parsed = ParsedCommand.parse("/help@my_bot");

        assertThat(parsed.getCommand()).isEqualTo("/help");
        assertThat(parsed.getArgsRaw()).isNull();
        assertThat(parsed.getArgs()).isEmpty();
    }
}
