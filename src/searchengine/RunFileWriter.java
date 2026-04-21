package searchengine;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

final class RunFileWriter {
    private RunFileWriter() {
    }

    static void writeRunFile(
        Path outputFile,
        List<Topic> topics,
        QueryParser queryParser,
        Searcher searcher,
        String groupId,
        int topK
    ) throws IOException {
        if (outputFile.getParent() != null) {
            Files.createDirectories(outputFile.getParent());
        }

        try (BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
            for (Topic topic : topics) {
                List<SearchResult> results = searcher.search(queryParser.parse(topic), topK);
                int rank = 1;
                for (SearchResult result : results) {
                    writer.write(String.format(
                        Locale.ROOT,
                        "%s Q0 %s %d %.12f %s",
                        topic.getNumber(),
                        result.getDocumentId(),
                        rank,
                        result.getScore(),
                        groupId
                    ));
                    writer.newLine();
                    rank++;
                }
            }
        }
    }
}
