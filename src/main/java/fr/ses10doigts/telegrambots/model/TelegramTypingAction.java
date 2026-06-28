package fr.ses10doigts.telegrambots.model;

/**
 * Actions de présence Telegram affichées temporairement côté client.
 */
public enum TelegramTypingAction {
    TYPING("typing"),
    UPLOAD_PHOTO("upload_photo"),
    UPLOAD_DOCUMENT("upload_document");

    private final String apiValue;

    TelegramTypingAction(String apiValue) {
        this.apiValue = apiValue;
    }

    public String getApiValue() {
        return apiValue;
    }
}
