package fr.ses10doigts.telegrambots.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TelegramView {
    private String text;
    private List<List<TelegramButtonView>> buttons;
}