package fr.ses10doigts.telegrambots.service.sender;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TelegramMarkdownUtils {

    private static final String ESCAPE_CHARS = "_*[]()~`>#+-=|{}.!";

    private TelegramMarkdownUtils() {
    }

    public static String escapeMarkdownV2(String input) {
        if (input == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder(input.length() * 2);
        for (char c : input.toCharArray()) {
            if (c == '\\' || ESCAPE_CHARS.indexOf(c) >= 0) {
                sb.append('\\');
            }
            sb.append(c);
        }
        return sb.toString();
    }

    public static String escapeMarkdownV2PreservingLinks(String input) {
        if (input == null) {
            return null;
        }

        Pattern linkPattern = Pattern.compile("\\[(.+?)\\]\\((.+?)\\)");
        Matcher matcher = linkPattern.matcher(input);

        StringBuilder result = new StringBuilder();
        int lastEnd = 0;

        while (matcher.find()) {
            result.append(escapeMarkdownV2(input.substring(lastEnd, matcher.start())));
            result.append(matcher.group());
            lastEnd = matcher.end();
        }

        result.append(escapeMarkdownV2(input.substring(lastEnd)));
        return result.toString();
    }
}