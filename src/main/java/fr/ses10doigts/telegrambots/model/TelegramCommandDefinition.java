package fr.ses10doigts.telegrambots.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TelegramCommandDefinition {
    private final String command;
    private final String description;
}