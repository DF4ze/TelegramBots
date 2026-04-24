package fr.ses10doigts.telegrambots.configuration;

public class TelegramPollingLoggingProperties {

    /**
     * Controls how Telegram BotSession 409 conflict polling errors are logged.
     * ERROR: keep library default error log
     * WARN:  demote to warning
     * OFF:   silence this specific log line
     */
    private Conflict409LogMode conflict409LogMode = Conflict409LogMode.ERROR;

    public Conflict409LogMode getConflict409LogMode() {
        return conflict409LogMode;
    }

    public void setConflict409LogMode(Conflict409LogMode conflict409LogMode) {
        this.conflict409LogMode = conflict409LogMode;
    }

    public enum Conflict409LogMode {
        ERROR,
        WARN,
        OFF
    }
}
