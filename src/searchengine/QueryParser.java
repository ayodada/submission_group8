package searchengine;

import java.util.List;

final class QueryParser {
    private final TextProcessor textProcessor = new TextProcessor();

    List<String> parse(Topic topic) {
        return parseText(topic.getTitle() + " " + topic.getDescription());
    }

    List<String> parseText(String text) {
        return textProcessor.tokenize(text);
    }
}
