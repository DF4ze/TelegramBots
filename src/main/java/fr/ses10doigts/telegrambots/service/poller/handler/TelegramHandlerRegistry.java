package fr.ses10doigts.telegrambots.service.poller.handler;

import fr.ses10doigts.telegrambots.model.TelegramCommandDefinition;
import fr.ses10doigts.telegrambots.model.TelegramHandlerMethod;
import fr.ses10doigts.telegrambots.model.TelegramUpdateContext;
import fr.ses10doigts.telegrambots.service.poller.handler.annot.CallbackQuery;
import fr.ses10doigts.telegrambots.service.poller.handler.annot.Chat;
import fr.ses10doigts.telegrambots.service.poller.handler.annot.Command;
import fr.ses10doigts.telegrambots.service.poller.handler.annot.TelegramController;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.util.*;

@Slf4j
public class TelegramHandlerRegistry {

    private static final String GLOBAL_BOT_KEY = "";

    private final ApplicationContext applicationContext;

    /**
     * Une registry par bot :
     * - clé vide => global
     * - clé renseignée => bot spécifique
     */
    private final Map<String, BotHandlers> handlersByBot = new HashMap<>();

    @Getter
    private final List<TelegramCommandDefinition> commandDefinitions = new ArrayList<>();

    public TelegramHandlerRegistry(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        Map<String, Object> controllers = applicationContext.getBeansWithAnnotation(TelegramController.class);

        for (Object bean : controllers.values()) {
            Class<?> targetClass = AopUtils.getTargetClass(bean);
            TelegramController controllerAnnotation = targetClass.getAnnotation(TelegramController.class);
            String botId = normalizeBotId(controllerAnnotation.bot());

            for (Method method : targetClass.getMethods()) {
                registerCommandHandler(botId, bean, method);
                registerChatHandler(botId, bean, method);
                registerCallbackHandler(botId, bean, method);
            }
        }

        validateChatHandlers();

        logRegistrySummary();
    }

    public TelegramHandlerMethod findCommandHandler(String botId, String command) {
        if (command == null) {
            return null;
        }

        String normalizedBotId = normalizeBotId(botId);

        TelegramHandlerMethod specific = getBotHandlers(normalizedBotId).commandHandlers.get(command);
        if (specific != null) {
            return specific;
        }

        return getGlobalHandlers().commandHandlers.get(command);
    }

    public TelegramHandlerMethod findCallbackHandler(String botId, String callbackData) {
        if (callbackData == null || callbackData.isBlank()) {
            return null;
        }

        String normalizedBotId = normalizeBotId(botId);

        TelegramHandlerMethod specific = getBotHandlers(normalizedBotId).callbackHandlers.get(callbackData);
        if (specific != null) {
            return specific;
        }

        return getGlobalHandlers().callbackHandlers.get(callbackData);
    }

    public List<TelegramHandlerMethod> findChatHandlers(String botId) {
        String normalizedBotId = normalizeBotId(botId);

        List<TelegramHandlerMethod> specificHandlers = getBotHandlers(normalizedBotId).chatHandlers;
        if (!specificHandlers.isEmpty()) {
            return specificHandlers;
        }

        return getGlobalHandlers().chatHandlers;
    }

    public List<TelegramCommandDefinition> getCommandDefinitions(String botId) {
        String normalizedBotId = normalizeBotId(botId);

        Map<String, TelegramCommandDefinition> merged = new LinkedHashMap<>();

        for (TelegramCommandDefinition definition : getGlobalHandlers().commandDefinitions) {
            merged.put(definition.getCommand(), definition);
        }

        for (TelegramCommandDefinition definition : getBotHandlers(normalizedBotId).commandDefinitions) {
            merged.put(definition.getCommand(), definition);
        }

        return new ArrayList<>(merged.values());
    }

    private void registerCommandHandler(String botId, Object bean, Method method) {
        Command command = method.getAnnotation(Command.class);
        if (command == null) {
            return;
        }

        validateHandlerSignature(method, "@Command");

        String commandValue = normalizeCommand(command.value());
        BotHandlers botHandlers = getOrCreateBotHandlers(botId);

        if (botHandlers.commandHandlers.containsKey(commandValue)) {
            throw new IllegalStateException(
                    "Duplicate Telegram command handler for command '" + commandValue + "' on bot '" + displayBotId(botId) + "'"
            );
        }

        botHandlers.commandHandlers.put(commandValue, new TelegramHandlerMethod(bean, method));

        TelegramCommandDefinition definition =
                new TelegramCommandDefinition(commandValue, command.description());

        botHandlers.commandDefinitions.add(definition);
        commandDefinitions.add(definition);

        log.info(
                "Registered Telegram command handler: bot={}, command={} -> {}#{}",
                displayBotId(botId),
                commandValue,
                bean.getClass().getSimpleName(),
                method.getName()
        );
    }

    private void registerCallbackHandler(String botId, Object bean, Method method) {
        CallbackQuery callbackQuery = method.getAnnotation(CallbackQuery.class);
        if (callbackQuery == null) {
            return;
        }

        validateHandlerSignature(method, "@CallbackQuery");

        String callbackValue = callbackQuery.value();
        if (callbackValue == null || callbackValue.isBlank()) {
            throw new IllegalStateException("@CallbackQuery value must not be blank");
        }

        BotHandlers botHandlers = getOrCreateBotHandlers(botId);

        if (botHandlers.callbackHandlers.containsKey(callbackValue)) {
            throw new IllegalStateException(
                    "Duplicate Telegram callback handler for callback '" + callbackValue + "' on bot '" + displayBotId(botId) + "'"
            );
        }

        botHandlers.callbackHandlers.put(callbackValue, new TelegramHandlerMethod(bean, method));

        log.info(
                "Registered Telegram callback handler: bot={}, callback={} -> {}#{}",
                displayBotId(botId),
                callbackValue,
                bean.getClass().getSimpleName(),
                method.getName()
        );
    }

    private void registerChatHandler(String botId, Object bean, Method method) {
        if (!method.isAnnotationPresent(Chat.class)) {
            return;
        }

        validateHandlerSignature(method, "@Chat");

        BotHandlers botHandlers = getOrCreateBotHandlers(botId);
        botHandlers.chatHandlers.add(new TelegramHandlerMethod(bean, method));

        log.info(
                "Registered Telegram chat handler: bot={} -> {}#{}",
                displayBotId(botId),
                bean.getClass().getSimpleName(),
                method.getName()
        );
    }

    private void validateChatHandlers() {
        for (Map.Entry<String, BotHandlers> entry : handlersByBot.entrySet()) {
            String botId = entry.getKey();
            BotHandlers botHandlers = entry.getValue();

            if (botHandlers.chatHandlers.size() > 1) {
                String handlers = botHandlers.chatHandlers.stream()
                        .map(h -> h.bean().getClass().getSimpleName() + "#" + h.method().getName())
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("unknown");

                throw new IllegalStateException(
                        "Only one method annotated with @Chat is allowed for bot '" + displayBotId(botId) + "'. Found: " + handlers
                );
            }
        }
    }

    private void logRegistrySummary() {
        int commandCount = handlersByBot.values().stream()
                .mapToInt(bot -> bot.commandHandlers.size())
                .sum();

        int chatCount = handlersByBot.values().stream()
                .mapToInt(bot -> bot.chatHandlers.size())
                .sum();

        int callbackCount = handlersByBot.values().stream()
                .mapToInt(bot -> bot.callbackHandlers.size())
                .sum();

        log.info(
                "Telegram registry loaded: {} command handler(s), {} chat handler(s), {} callback handler(s), {} bot scope(s)",
                commandCount,
                chatCount,
                callbackCount,
                handlersByBot.size()
        );
    }

    private void validateHandlerSignature(Method method, String annotationName) {
        Class<?>[] parameterTypes = method.getParameterTypes();

        if (parameterTypes.length != 1 || !TelegramUpdateContext.class.equals(parameterTypes[0])) {
            throw new IllegalStateException(
                    "Invalid signature for " + annotationName + " on method "
                            + method.getDeclaringClass().getName() + "#" + method.getName() + ". "
                            + "Expected method signature to be: " + method.getName() + "(TelegramUpdateContext context)"
            );
        }
    }

    private String normalizeCommand(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("@Command value must not be blank");
        }

        if (value.chars().anyMatch(Character::isUpperCase)) {
            throw new IllegalStateException(
                    "Invalid @Command value '" + value + "'. Must be in lower case. Expected format: /[a-z0-9_]{1,32}"
            );
        }

        if (!value.startsWith("/")) {
            throw new IllegalStateException(
                    "Invalid @Command value '" + value + "'. Command must start with '/'. Expected format: /[a-z0-9_]{1,32}"
            );
        }

        String commandName = value.substring(1);
        if (!commandName.matches("[a-z0-9_]{1,32}")) {
            throw new IllegalStateException(
                    "Invalid @Command value '" + value + "'. Invalid character(s) found or too long command. Expected format: /[a-z0-9_]{1,32}"
            );
        }

        return value;
    }

    private String normalizeBotId(String botId) {
        return botId == null ? GLOBAL_BOT_KEY : botId.trim();
    }

    private String displayBotId(String botId) {
        return botId == null || botId.isBlank() ? "<global>" : botId;
    }

    private BotHandlers getGlobalHandlers() {
        return getOrCreateBotHandlers(GLOBAL_BOT_KEY);
    }

    private BotHandlers getBotHandlers(String botId) {
        return handlersByBot.getOrDefault(normalizeBotId(botId), new BotHandlers());
    }

    private BotHandlers getOrCreateBotHandlers(String botId) {
        return handlersByBot.computeIfAbsent(normalizeBotId(botId), key -> new BotHandlers());
    }

    private static class BotHandlers {
        private final Map<String, TelegramHandlerMethod> commandHandlers = new HashMap<>();
        private final List<TelegramHandlerMethod> chatHandlers = new ArrayList<>();
        private final List<TelegramCommandDefinition> commandDefinitions = new ArrayList<>();
        private final Map<String, TelegramHandlerMethod> callbackHandlers = new HashMap<>();
    }
}