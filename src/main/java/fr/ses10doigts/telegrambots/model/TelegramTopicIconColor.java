package fr.ses10doigts.telegrambots.model;

/**
 * Couleurs d'icône disponibles pour un sujet (topic) de forum Telegram.
 *
 * <p>Telegram impose une liste fermée de 6 couleurs RGB pour l'icône "unie"
 * d'un sujet (indépendante d'un éventuel emoji personnalisé). Cette liste est
 * définie par l'API Telegram elle-même et ne peut pas être étendue côté bot.</p>
 *
 * <p>Cette couleur ne peut être choisie qu'à la création d'un sujet
 * ({@code createForumTopic}) : l'API Telegram ne permet pas de la modifier
 * ensuite via {@code editForumTopic} (seuls le titre et l'emoji d'icône sont
 * modifiables après coup).</p>
 */
public enum TelegramTopicIconColor {

    BLUE(0x6FB9F0),
    YELLOW(0xFFD67E),
    PURPLE(0xCB86DB),
    GREEN(0x8EEE98),
    PINK(0xFF93B2),
    RED(0xFB6F5F);

    private final int rgb;

    TelegramTopicIconColor(int rgb) {
        this.rgb = rgb;
    }

    /**
     * Valeur RGB attendue par l'API Telegram pour cette couleur.
     */
    public int getRgb() {
        return rgb;
    }

    /**
     * Retrouve la couleur correspondant à une valeur RGB renvoyée par Telegram.
     *
     * @param rgb valeur RGB telle que renvoyée par l'API (peut être {@code null})
     * @return la couleur correspondante, ou {@code null} si {@code rgb} est {@code null}
     *         ou ne correspond à aucune valeur connue (Telegram pourrait ajouter de
     *         nouvelles couleurs à l'avenir sans prévenir)
     */
    public static TelegramTopicIconColor fromRgb(Integer rgb) {
        if (rgb == null) {
            return null;
        }

        for (TelegramTopicIconColor color : values()) {
            if (color.rgb == rgb) {
                return color;
            }
        }

        return null;
    }
}
