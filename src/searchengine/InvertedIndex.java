package searchengine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

final class InvertedIndex {
    private final Map<String, Map<Integer, Integer>> postings;
    private final List<DocumentRecord> documents;
    private final long totalDocumentLength;

    InvertedIndex(Map<String, Map<Integer, Integer>> postings, List<DocumentRecord> documents, long totalDocumentLength) {
        this.postings = postings;
        this.documents = documents;
        this.totalDocumentLength = totalDocumentLength;
    }

    int getDocumentCount() {
        return documents.size();
    }

    double getAverageDocumentLength() {
        if (documents.isEmpty()) {
            return 0.0;
        }
        return (double) totalDocumentLength / documents.size();
    }

    DocumentRecord getDocument(int internalId) {
        return documents.get(internalId);
    }

    Map<Integer, Integer> getPostings(String term) {
        return postings.getOrDefault(term, Collections.emptyMap());
    }

    int getDocumentFrequency(String term) {
        return getPostings(term).size();
    }

    void save(Path indexDir) throws IOException {
        Files.createDirectories(indexDir);
        writeMetadata(indexDir.resolve("meta.properties"));
        writeDocuments(indexDir.resolve("documents.tsv"));
        writePostings(indexDir.resolve("postings.tsv"));
    }

    static InvertedIndex load(Path indexDir) throws IOException {
        Properties properties = new Properties();
        try (BufferedReader reader = Files.newBufferedReader(indexDir.resolve("meta.properties"), StandardCharsets.UTF_8)) {
            properties.load(reader);
        }

        List<DocumentRecord> documents = readDocuments(indexDir.resolve("documents.tsv"));
        Map<String, Map<Integer, Integer>> postings = readPostings(indexDir.resolve("postings.tsv"));
        long totalDocumentLength = Long.parseLong(properties.getProperty("totalDocumentLength", "0"));
        return new InvertedIndex(postings, documents, totalDocumentLength);
    }

    private void writeMetadata(Path output) throws IOException {
        Properties properties = new Properties();
        properties.setProperty("documentCount", Integer.toString(documents.size()));
        properties.setProperty("totalDocumentLength", Long.toString(totalDocumentLength));
        properties.setProperty("averageDocumentLength", Double.toString(getAverageDocumentLength()));
        try (BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
            properties.store(writer, "Search Engine Index Metadata");
        }
    }

    private void writeDocuments(Path output) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
            for (DocumentRecord document : documents) {
                writer.write(document.getInternalId() + "\t" + document.getDocumentId() + "\t" + document.getLength());
                writer.newLine();
            }
        }
    }

    private void writePostings(Path output) throws IOException {
        Map<String, Map<Integer, Integer>> sortedPostings = new TreeMap<>(postings);
        try (BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
            for (Map.Entry<String, Map<Integer, Integer>> entry : sortedPostings.entrySet()) {
                writer.write(entry.getKey());
                writer.write('\t');
                boolean first = true;
                for (Map.Entry<Integer, Integer> posting : entry.getValue().entrySet()) {
                    if (!first) {
                        writer.write(' ');
                    }
                    writer.write(posting.getKey() + ":" + posting.getValue());
                    first = false;
                }
                writer.newLine();
            }
        }
    }

    private static List<DocumentRecord> readDocuments(Path input) throws IOException {
        List<DocumentRecord> documents = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(input, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length < 3) {
                    continue;
                }
                documents.add(new DocumentRecord(Integer.parseInt(parts[0]), parts[1], Integer.parseInt(parts[2])));
            }
        }
        return documents;
    }

    private static Map<String, Map<Integer, Integer>> readPostings(Path input) throws IOException {
        Map<String, Map<Integer, Integer>> postings = new HashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(input, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                int split = line.indexOf('\t');
                if (split < 0) {
                    continue;
                }
                String term = line.substring(0, split);
                String body = line.substring(split + 1).trim();
                Map<Integer, Integer> termPostings = new LinkedHashMap<>();
                if (!body.isEmpty()) {
                    String[] postingParts = body.split("\\s+");
                    for (String postingPart : postingParts) {
                        String[] values = postingPart.split(":");
                        if (values.length == 2) {
                            termPostings.put(Integer.parseInt(values[0]), Integer.parseInt(values[1]));
                        }
                    }
                }
                postings.put(term, termPostings);
            }
        }
        return postings;
    }
}
