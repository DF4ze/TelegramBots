package fr.ses10doigts.telegrambots.service.sender;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TelegramMessageSplitterTest {

    @Test
    void shouldReturnEmptyListForNullText() {
        assertThat(TelegramMessageSplitter.split(null)).isEmpty();
    }

    @Test
    void shouldReturnSingleChunkForShortText() {
        List<String> chunks = TelegramMessageSplitter.split("hello world");

        assertThat(chunks).containsExactly("hello world");
    }

    @Test
    void shouldReturnSingleChunkWhenTextIsExactlyAtTheLimit() {
        String text = "a".repeat(TelegramMessageSplitter.TELEGRAM_MAX_MESSAGE_LENGTH);

        List<String> chunks = TelegramMessageSplitter.split(text);

        assertThat(chunks).hasSize(1);
        assertThat(chunks.getFirst()).hasSize(TelegramMessageSplitter.TELEGRAM_MAX_MESSAGE_LENGTH);
    }

    @Test
    void shouldHardSplitWhenNoSeparatorIsAvailable() {
        String text = "a".repeat(5000);

        List<String> chunks = TelegramMessageSplitter.split(text);

        assertThat(chunks).hasSize(2);
        assertThat(chunks.get(0)).hasSize(4096);
        assertThat(chunks.get(1)).hasSize(904);
        assertThat(String.join("", chunks)).isEqualTo(text);
    }

    @Test
    void shouldPreferSplittingOnASpace() {
        StringBuilder sb = new StringBuilder();
        while (sb.length() < 4300) {
            sb.append("word ");
        }
        String text = sb.toString();

        List<String> chunks = TelegramMessageSplitter.split(text);

        assertThat(chunks).hasSize(2);
        assertThat(chunks.getFirst()).endsWith(" ");
        assertThat(String.join("", chunks)).isEqualTo(text);
    }

    @Test
    void shouldPreferSplittingOnAParagraphBreakOverALineBreak() {
        String para1 = "A".repeat(2500);
        String para2 = "B".repeat(2500);
        String text = para1 + "\n\n" + para2;

        List<String> chunks = TelegramMessageSplitter.split(text);

        assertThat(chunks).hasSize(2);
        assertThat(chunks.get(0)).isEqualTo(para1 + "\n");
        assertThat(chunks.get(1)).isEqualTo(para2);
    }

    @Test
    void shouldNeverSplitInsideAMarkdownV2EscapePair() {
        // Simule un texte échappé (backslash + caractère) répété au-delà de la limite,
        // sans espace ni saut de ligne pour forcer une coupure brute.
        String text = "\\.".repeat(3000); // 6000 caractères, uniquement des paires "\."

        List<String> chunks = TelegramMessageSplitter.split(text);

        assertThat(chunks.size()).isGreaterThan(1);
        for (String chunk : chunks) {
            assertThat(chunk.length() % 2).isEqualTo(0);
            for (int i = 0; i < chunk.length(); i += 2) {
                assertThat(chunk.charAt(i)).isEqualTo('\\');
                assertThat(chunk.charAt(i + 1)).isEqualTo('.');
            }
        }
        assertThat(String.join("", chunks)).isEqualTo(text);
    }

    @Test
    void shouldRespectACustomMaxLength() {
        List<String> chunks = TelegramMessageSplitter.split("abcdefghij", 4);

        assertThat(chunks).containsExactly("abcd", "efgh", "ij");
    }

    @Test
    void shouldProduceExactlyTheExpectedNumberOfChunksForALongText() {
        String text = "x".repeat(4096 * 5 + 123);

        List<String> chunks = TelegramMessageSplitter.split(text);

        assertThat(chunks).hasSize(6);
        for (String chunk : chunks) {
            assertThat(chunk.length()).isLessThanOrEqualTo(4096);
        }
        assertThat(chunks.stream().mapToInt(String::length).sum()).isEqualTo(text.length());
    }

    @Test
    void shouldRejectNonPositiveMaxLength() {
        assertThatThrownBy(() -> TelegramMessageSplitter.split("abc", 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
