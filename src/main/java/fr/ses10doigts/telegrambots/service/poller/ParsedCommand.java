package fr.ses10doigts.telegrambots.service.poller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class ParsedCommand {

    private final String command;
    private final String argsRaw;
    private final List<String> args;

    public static String extractCommand(String text) {
        if (text == null || text.isBlank() || !text.startsWith("/")) {
            return null;
        }

        String firstToken = text.split("\\s+")[0];

        int arobaseIndex = firstToken.indexOf('@');
        if (arobaseIndex > 0) {
            firstToken = firstToken.substring(0, arobaseIndex);
        }

        return firstToken;
    }

    public static ParsedCommand parse(String text) {

        if (text == null || !text.startsWith("/")) {
            return new ParsedCommand(null, null, List.of());
        }

        String[] parts = text.trim().split("\\s+", 2);

        String command = parts[0];

        int atIndex = command.indexOf('@');
        if (atIndex > 0) {
            command = command.substring(0, atIndex);
        }

        if (parts.length == 1) {
            return new ParsedCommand(command, null, List.of());
        }

        String raw = parts[1];

        List<String> args = Arrays.asList(raw.split("\\s+"));

        return new ParsedCommand(command, raw, args);
    }

}