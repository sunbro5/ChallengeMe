package org.jan.config;

import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/**
 * Loads a word list from {@code profanity.txt} on the classpath and exposes
 * a single {@link #check(String)} method that throws
 * {@link IllegalArgumentException} when the text contains a banned word.
 *
 * <p>Normalisation applied before matching:
 * <ul>
 *   <li>Lowercase</li>
 *   <li>Common leet-speak digit substitutions (3→e, 0→o, 1→i, …)</li>
 *   <li>Repeated characters collapsed (fuuuck → fuck)</li>
 * </ul>
 *
 * <p>To add or remove words edit {@code src/main/resources/profanity.txt}.
 */
@Component
public class ProfanityFilter {

    private final Set<String> banned = new HashSet<>();

    @PostConstruct
    void load() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource("profanity.txt").getInputStream(), StandardCharsets.UTF_8))) {
            reader.lines()
                    .map(String::trim)
                    .filter(l -> !l.isEmpty() && !l.startsWith("#"))
                    .map(String::toLowerCase)
                    .forEach(banned::add);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load profanity.txt", e);
        }
    }

    /**
     * Throws {@link IllegalArgumentException} with a user-friendly message if
     * {@code text} contains any banned word.  Does nothing if text is null or blank.
     */
    public void check(String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        String normalised = normalise(text);
        if (banned.stream().anyMatch(word -> containsWord(normalised, word))) {
            throw new IllegalArgumentException("Text contains inappropriate language");
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String normalise(String input) {
        String s = input.toLowerCase();

        // Leet-speak substitutions
        s = s.replace("@", "a")
             .replace("3", "e")
             .replace("1", "i")
             .replace("!", "i")
             .replace("0", "o")
             .replace("5", "s")
             .replace("$", "s")
             .replace("7", "t")
             .replace("+", "t")
             .replace("4", "a");

        // Collapse runs of the same character (fuuuck → fuck, shhit → shit)
        s = s.replaceAll("(.)\\1+", "$1");

        return s;
    }

    /**
     * Returns true if {@code text} contains {@code word} as a whole token
     * (not as part of a longer word).  Boundary = any non-letter character or
     * start/end of string.
     */
    private boolean containsWord(String text, String word) {
        int idx = 0;
        while ((idx = text.indexOf(word, idx)) != -1) {
            boolean startOk = (idx == 0) || !Character.isLetter(text.charAt(idx - 1));
            int end = idx + word.length();
            boolean endOk = (end == text.length()) || !Character.isLetter(text.charAt(end));
            if (startOk && endOk) {
                return true;
            }
            idx++;
        }
        return false;
    }
}
