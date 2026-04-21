package searchengine;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class TopicParser {
    private static final Pattern TOPIC_PATTERN = Pattern.compile("(?s)<top>(.*?)</top>");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("<num>\\s*Number:\\s*(\\d+)");
    private static final Pattern TITLE_PATTERN = Pattern.compile("<title>\\s*(.*?)(?:<desc>|$)", Pattern.DOTALL);
    private static final Pattern DESCRIPTION_PATTERN = Pattern.compile("<desc>\\s*Description:\\s*(.*?)(?:<narr>|$)", Pattern.DOTALL);
    private static final Pattern NARRATIVE_PATTERN = Pattern.compile("<narr>\\s*Narrative:\\s*(.*)$", Pattern.DOTALL);

    List<Topic> parse(Path topicsFile) throws IOException {
        String content = new String(Files.readAllBytes(topicsFile), StandardCharsets.UTF_8);
        List<Topic> topics = new ArrayList<>();
        Matcher matcher = TOPIC_PATTERN.matcher(content);
        while (matcher.find()) {
            String block = matcher.group(1);
            String number = capture(NUMBER_PATTERN, block);
            String title = normalizeWhitespace(capture(TITLE_PATTERN, block));
            String description = normalizeWhitespace(capture(DESCRIPTION_PATTERN, block));
            String narrative = normalizeWhitespace(capture(NARRATIVE_PATTERN, block));
            if (number != null) {
                topics.add(new Topic(number, title, description, narrative));
            }
        }
        return topics;
    }

    private static String capture(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1).trim() : "";
    }

    private static String normalizeWhitespace(String text) {
        return text.replaceAll("\\s+", " ").trim();
    }
}
