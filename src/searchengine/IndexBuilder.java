package searchengine;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

final class IndexBuilder {
    private static final Pattern DOC_PATTERN = Pattern.compile("(?s)<DOC>(.*?)</DOC>");
    private static final Pattern DOCNO_PATTERN = Pattern.compile("(?s)<DOCNO>\\s*(.*?)\\s*</DOCNO>");
    private static final Pattern DOCHDR_PATTERN = Pattern.compile("(?s)<DOCHDR>.*?</DOCHDR>");

    private final TextProcessor textProcessor = new TextProcessor();

    static InvertedIndex build(Path dataDir) throws IOException {
        return new IndexBuilder().buildIndex(dataDir);
    }

    private InvertedIndex buildIndex(Path dataDir) throws IOException {
        Map<String, Map<Integer, Integer>> postings = new HashMap<>();
        List<DocumentRecord> documents = new ArrayList<>();
        long totalDocumentLength = 0L;
        int internalId = 0;

        List<Path> gzFiles = listGzFiles(dataDir);
        for (Path gzFile : gzFiles) {
            // Each WT file is a gzip file that contains many <DOC> blocks.
            String content = decompressFile(gzFile);
            Matcher matcher = DOC_PATTERN.matcher(content);
            while (matcher.find()) {
                String rawDocument = matcher.group(1);
                String documentId = extractDocumentId(rawDocument);
                if (documentId == null || documentId.trim().isEmpty()) {
                    continue;
                }

                String cleanText = stripMarkup(rawDocument);
                List<String> tokens = textProcessor.tokenize(cleanText);
                if (tokens.isEmpty()) {
                    continue;
                }

                // Count term frequency inside this document before adding to the global postings list.
                Map<String, Integer> documentTerms = new HashMap<>();
                for (String token : tokens) {
                    documentTerms.merge(token, 1, Integer::sum);
                }

                for (Map.Entry<String, Integer> entry : documentTerms.entrySet()) {
                    postings.computeIfAbsent(entry.getKey(), key -> new LinkedHashMap<>()).put(internalId, entry.getValue());
                }

                documents.add(new DocumentRecord(internalId, documentId, tokens.size()));
                totalDocumentLength += tokens.size();
                internalId++;
            }
        }

        return new InvertedIndex(postings, documents, totalDocumentLength);
    }

    private static List<Path> listGzFiles(Path dataDir) throws IOException {
        try (Stream<Path> stream = Files.walk(dataDir)) {
            return stream
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().toUpperCase().endsWith(".GZ"))
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
        }
    }

    private static String decompressFile(Path gzFile) throws IOException {
        try (InputStream fileInput = Files.newInputStream(gzFile);
             BufferedInputStream bufferedInput = new BufferedInputStream(fileInput);
             GZIPInputStream gzipInput = new GZIPInputStream(bufferedInput);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = gzipInput.read(buffer)) >= 0) {
                output.write(buffer, 0, read);
            }
            return new String(output.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    private static String extractDocumentId(String rawDocument) {
        Matcher matcher = DOCNO_PATTERN.matcher(rawDocument);
        return matcher.find() ? matcher.group(1).trim() : null;
    }

    private static String stripMarkup(String rawDocument) {
        // I remove the HTTP header block first, then clean up the remaining HTML tags.
        String withoutHeaders = DOCHDR_PATTERN.matcher(rawDocument).replaceAll(" ");
        String withoutTags = withoutHeaders.replaceAll("(?s)<[^>]+>", " ");
        return withoutTags
            .replace("&nbsp;", " ")
            .replace("&amp;", " and ")
            .replace("&lt;", " ")
            .replace("&gt;", " ")
            .replace("&quot;", " ");
    }
}
