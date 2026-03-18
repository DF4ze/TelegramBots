package fr.ses10doigts.telegrambots.service.poller.handler;

import fr.ses10doigts.telegrambots.model.TelegramHandlerMethod;
import fr.ses10doigts.telegrambots.model.TelegramUpdateContext;
import fr.ses10doigts.telegrambots.service.poller.handler.annot.CallbackQuery;
import fr.ses10doigts.telegrambots.service.poller.handler.annot.Chat;
import fr.ses10doigts.telegrambots.service.poller.handler.annot.Command;
import fr.ses10doigts.telegrambots.service.poller.handler.annot.TelegramController;
import fr.ses10doigts.telegrambots.model.TelegramCommandDefinition;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.util.*;

@Slf4j
public class TelegramHandlerRegistry {

    private final ApplicationContext applicationContext;

    @Getter
    private final Map<String, TelegramHandlerMethod> commandHandlers = new HashMap<>();

    @Getter
    private final List<TelegramHandlerMethod> chatHandlers = new ArrayList<>();

    @Getter
    private final List<TelegramCommandDefinition> commandDefinitions = new ArrayList<>();

    @Getter
    private final Map<String, TelegramHandlerMethod> callbackHandlers = new HashMap<>();

    public TelegramHandlerRegistry(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        Map<String, Object> controllers = applicationContext.getBeansWithAnnotation(TelegramController.class);

        for (Object bean : controllers.values()) {
            Class<?> targetClass = AopUtils.getTargetClass(bean);

            for (Method method : targetClass.getMethods()) {
                registerCommandHandler(bean, method);
                registerChatHandler(bean, method);
                registerCallbackHandler(bean, method);
            }
        }

        if (chatHandlers.size() > 1) {
            String handlers = chatHandlers.stream()
                    .map(h -> h.bean().getClass().getSimpleName() + "#" + h.method().getName())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("unknown");

            throw new IllegalStateException(
                    "Only one method annotated with @Chat is allowed. Found: " + handlers
            );
        }

        log.info("Telegram registry loaded: {} command handler(s), {} chat handler(s), {} callback handler(s)",
                commandHandlers.size(),
                chatHandlers.size(),
                callbackHandlers.size());
    }

    private void registerCommandHandler(Object bean, Method method) {
        Command command = method.getAnnotation(Command.class);
        if (command == null) {
            return;
        }

        validateHandlerSignature(method, "@Command");
        String commandValue = normalizeCommand(command.value());

        if (commandHandlers.containsKey(commandValue)) {
            throw new IllegalStateException("Duplicate Telegram command handler for command: " + commandValue);
        }

        commandHandlers.put(commandValue, new TelegramHandlerMethod(bean, method));
        log.info("Registered Telegram command handler: {} -> {}#{}",
                commandValue,
                bean.getClass().getSimpleName(),
                method.getName());
    }

    private void registerCallbackHandler(Object bean, Method method) {
        CallbackQuery callbackQuery = method.getAnnotation(CallbackQuery.class);
        if (callbackQuery == null) return;

        validateHandlerSignature(method, "@Callback");

        String callbackValue = callbackQuery.value();

        if (callbackValue == null || callbackValue.isBlank()) {
            throw new IllegalStateException("@Callback value must not be blank");
        }

        if (callbackHandlers.containsKey(callbackValue)) {
            throw new IllegalStateException("Duplicate Telegram callback handler for callback: " + callbackValue);
        }

        callbackHandlers.put(callbackValue, new TelegramHandlerMethod(bean, method));

        log.info("Registered Telegram callback handler: {} -> {}#{}",
                callbackValue,
                bean.getClass().getSimpleName(),
                method.getName());
    }

    private void registerChatHandler(Object bean, Method method) {
        if (!method.isAnnotationPresent(Chat.class)) {
            return;
        }

        validateHandlerSignature(method, "@Chat");

        chatHandlers.add(new TelegramHandlerMethod(bean, method));
        log.info("Registered Telegram chat handler: {}#{}",
                bean.getClass().getSimpleName(),
                method.getName());
    }

    private void validateHandlerSignature(Method method, String annotationName) {
        Class<?>[] parameterTypes = method.getParameterTypes();

        if (parameterTypes.length != 1 || !TelegramUpdateContext.class.equals(parameterTypes[0])) {
            throw new IllegalStateException(
                    "Invalid signature for " + annotationName + " on method "
                            + method.getDeclaringClass().getName() + "#" + method.getName()
                            + ". Expected signature: (TelegramUpdateContext)"
            );
        }
    }

    private String normalizeCommand(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("@Command value must not be blank");
        }

        if(value.chars().anyMatch(Character::isUpperCase) ){
            throw new IllegalStateException(
                    "Invalid @Command value '" + value + "'. " +
                            "Must be in lower case. " +
                            "Expected format: /[a-z0-9_]{1,32}"
            );
        }

        if (!value.startsWith("/")) {
            throw new IllegalStateException(
                    "Invalid @Command value '" + value + "'. " +
                            "Command must start with '/'. " +
                            "Expected format: /[a-z0-9_]{1,32}"
            );
        }

        String commandName = value.substring(1);

        if (!commandName.matches("[a-z0-9_]{1,32}")) {
            throw new IllegalStateException(
                    "Invalid @Command value '" + value + "'. " +
                            "Invalid character(s) found or too long command. " +
                            "Expected format: /[a-z0-9_]{1,32}"
            );
        }

        return value;
    }
}