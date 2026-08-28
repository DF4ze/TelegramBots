package fr.ses10doigts.telegrambots.service.sender;

import java.util.ArrayList;
import java.util.List;

/**
 * Découpe un texte trop long pour un seul message Telegram (limite de
 * {@value #TELEGRAM_MAX_MESSAGE_LENGTH} caractères) en plusieurs morceaux
 * envoyables séquentiellement, au lieu de laisser Telegram rejeter l'envoi
 * (ou tronquer silencieusement le contenu).
 *
 * <p>Le découpage cherche le meilleur point de coupure avant la limite, dans
 * cet ordre de préférence : saut de paragraphe ({@code "\n\n"}), saut de
 * ligne ({@code "\n"}), espace, puis coupure brute en dernier recours. Il
 * évite systématiquement de couper au milieu d'une séquence d'échappement
 * MarkdownV2 (un backslash suivi du caractère qu'il échappe), ce qui
 * produirait sinon un message invalide côté Telegram.</p>
 *
 * <p>Cette classe ne connaît rien des entités MarkdownV2 (gras, italique,
 * liens, blocs de code, etc.) : une mise en forme qui chevauche un point de
 * coupure peut donc apparaître incomplète sur l'un des morceaux. Pour du
 * contenu généré automatiquement et potentiellement long, il est préférable
 * de structurer le texte en paragraphes ({@code "\n\n"}) afin de guider le
 * découpage vers des frontières logiques.</p>
 */
public final class TelegramMessageSplitter {

    /**
     * Longueur maximale d'un message texte Telegram, en caractères.
     */
    public static final int TELEGRAM_MAX_MESSAGE_LENGTH = 4096;

    private TelegramMessageSplitter() {
    }

    /**
     * Découpe le texte selon la limite standard de Telegram
     * ({@value #TELEGRAM_MAX_MESSAGE_LENGTH} caractères).
     */
    public static List<String> split(String text) {
        return split(text, TELEGRAM_MAX_MESSAGE_LENGTH);
    }

    /**
     * Découpe le texte en morceaux d'au plus {@code maxLength} caractères.
     *
     * @return la liste des morceaux, dans l'ordre ; une liste à un seul
     * élément (le texte inchangé) si aucun découpage n'est nécessaire, ou une
     * liste vide si {@code text} est {@code null}.
     */
    public static List<String> split(String text, int maxLength) {
        if (maxLength <= 0) {
            throw new IllegalArgumentException("maxLength must be positive");
        }

        if (text == null) {
            return List.of();
        }

        if (text.length() <= maxLength) {
            return List.of(text);
        }

        List<String> chunks = new ArrayList<>();
        int start = 0;
        int length = text.length();

        while (start < length) {
            int remaining = length - start;

            if (remaining <= maxLength) {
                chunks.add(text.substring(start));
                break;
            }

            int splitAt = findSplitPoint(text, start, start + maxLength);
            chunks.add(text.substring(start, splitAt));
            start = skipLeadingNewlines(text, splitAt);
        }

        return chunks;
    }

    /**
     * Cherche le meilleur point de coupure entre {@code start} (exclu) et
     * {@code hardLimit} (position maximale, exclue), en préférant un saut de
     * paragraphe, puis un saut de ligne, puis un espace, avant de se
     * rabattre sur une coupure brute — tout en garantissant que le point
     * choisi ne tombe jamais au milieu d'une paire d'échappement MarkdownV2.
     */
    private static int findSplitPoint(String text, int start, int hardLimit) {
        int paragraphBreak = lastIndexOfBounded(text, "\n\n", start, hardLimit);
        if (paragraphBreak >= 0) {
            return snapToValidBoundary(text, start, paragraphBreak + 1);
        }

        int lineBreak = lastIndexOfCharBounded(text, '\n', start, hardLimit);
        if (lineBreak >= 0) {
            return snapToValidBoundary(text, start, lineBreak + 1);
        }

        int spaceBreak = lastIndexOfCharBounded(text, ' ', start, hardLimit);
        if (spaceBreak >= 0) {
            return snapToValidBoundary(text, start, spaceBreak + 1);
        }

        return snapToValidBoundary(text, start, hardLimit);
    }

    /**
     * Recherche la dernière occurrence de {@code c} dans l'intervalle
     * {@code (start, endExclusive)}, sans jamais remonter avant {@code start}
     * (contrairement à {@link String#lastIndexOf(int, int)}), afin de borner
     * le coût de la recherche à {@code maxLength} quelle que soit la
     * position dans un texte très long.
     */
    private static int lastIndexOfCharBounded(String text, char c, int start, int endExclusive) {
        int limit = Math.min(endExclusive, text.length());
        for (int i = limit - 1; i > start; i--) {
            if (text.charAt(i) == c) {
                return i;
            }
        }
        return -1;
    }

    private static int lastIndexOfBounded(String text, String pattern, int start, int endExclusive) {
        int limit = Math.min(endExclusive, text.length()) - pattern.length();
        for (int i = limit; i > start; i--) {
            if (text.startsWith(pattern, i)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Ramène {@code candidate} à la plus grande frontière "valide" à sa
     * gauche (ou égale), en repartant de {@code start} — une position
     * toujours valide par construction. Une frontière est valide si elle ne
     * sépare jamais un backslash d'échappement du caractère qu'il échappe :
     * chaque backslash produit par {@link TelegramMarkdownUtils} est toujours
     * immédiatement suivi d'exactement un caractère formant une paire
     * indivisible.
     */
    private static int snapToValidBoundary(String text, int start, int candidate) {
        int i = start;

        while (i < candidate) {
            int next = (text.charAt(i) == '\\' && i + 1 < text.length()) ? i + 2 : i + 1;
            if (next > candidate) {
                break;
            }
            i = next;
        }

        return i;
    }

    private static int skipLeadingNewlines(String text, int index) {
        int cursor = index;
        while (cursor < text.length() && text.charAt(cursor) == '\n') {
            cursor++;
        }
        return cursor;
    }
}
