package fr.ses10doigts.telegrambots.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TelegramButtonView {
    private String text;
    private String callbackData;
}