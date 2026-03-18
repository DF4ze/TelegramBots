package fr.ses10doigts.telegrambots.service.bot;

public class CurrentTelegramBotContext {

    private static final ThreadLocal<String> CURRENT_BOT_ID = new ThreadLocal<>();

    public void setCurrentBotId(String botId) {
        CURRENT_BOT_ID.set(botId);
    }

    public String getCurrentBotId() {
        return CURRENT_BOT_ID.get();
    }

    public String getRequiredCurrentBotId() {
        String botId = CURRENT_BOT_ID.get();

        if (botId == null || botId.isBlank()) {
            throw new IllegalStateException("No current Telegram bot is bound to the current thread");
        }

        return botId;
    }

    public boolean hasCurrentBotId() {
        String botId = CURRENT_BOT_ID.get();
        return botId != null && !botId.isBlank();
    }

    public void clear() {
        CURRENT_BOT_ID.remove();
    }
}