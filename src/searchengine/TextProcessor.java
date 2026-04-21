package searchengine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

final class TextProcessor {
    private static final Set<String> STOP_WORDS = new HashSet<String>(Arrays.asList(
        "a", "an", "and", "are", "as", "at", "be", "been", "being", "by",
        "for", "from", "had", "has", "have", "he", "her", "his", "how", "i",
        "if", "in", "into", "is", "it", "its", "itself", "me", "more", "most",
        "of", "on", "or", "our", "s", "she", "so", "such", "t", "than", "that",
        "the", "their", "them", "then", "there", "these", "they", "this", "to",
        "was", "we", "were", "what", "when", "where", "which", "who", "will",
        "with", "would", "you", "your"
    ));

    List<String> tokenize(String text) {
        String normalized = text.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ");
        String[] rawTokens = normalized.trim().split("\\s+");
        List<String> tokens = new ArrayList<>(rawTokens.length);
        for (String token : rawTokens) {
            if (token.trim().isEmpty() || STOP_WORDS.contains(token)) {
                continue;
            }
            String stemmed = stem(token);
            if (!stemmed.trim().isEmpty() && stemmed.length() > 1) {
                tokens.add(stemmed);
            }
        }
        return tokens;
    }

    private String stem(String token) {
        if (token.length() > 4 && token.endsWith("ing")) {
            return token.substring(0, token.length() - 3);
        }
        if (token.length() > 3 && token.endsWith("ed")) {
            return token.substring(0, token.length() - 2);
        }
        if (token.length() > 3 && token.endsWith("es")) {
            return token.substring(0, token.length() - 2);
        }
        if (token.length() > 2 && token.endsWith("s")) {
            return token.substring(0, token.length() - 1);
        }
        return token;
    }
}
