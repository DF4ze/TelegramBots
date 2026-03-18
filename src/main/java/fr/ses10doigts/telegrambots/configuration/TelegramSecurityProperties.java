package fr.ses10doigts.telegrambots.configuration;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class TelegramSecurityProperties {
    private Set<Long> allowedUserIds = new HashSet<>();
}