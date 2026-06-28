package fr.ses10doigts.telegrambots.model;

import lombok.Builder;
import lombok.Value;

/**
 * Identifie un message Telegram par son chat et son identifiant technique.
 */
@Value
@Builder
public class TelegramMessageReference {
    Long chatId;
    Integer messageId;
}
