package fr.ses10doigts.telegrambots.service.sender;


@FunctionalInterface
public interface TelegramCall<T> {
    T execute() throws Exception;
}