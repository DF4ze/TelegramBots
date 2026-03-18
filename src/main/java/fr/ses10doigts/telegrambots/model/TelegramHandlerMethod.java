package fr.ses10doigts.telegrambots.model;

import java.lang.reflect.Method;

public record TelegramHandlerMethod(
        Object bean,
        Method method
) {
}