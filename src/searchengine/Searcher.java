package searchengine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class Searcher {
    private static final double K1 = 1.2;
    private static final double B = 0.75;

    private final InvertedIndex index;

    Searcher(InvertedIndex index) {
        this.index = index;
    }

    List<SearchResult> search(List<String> queryTerms, int topK) {
        Map<Integer, Double> scores = new HashMap<>();
        int documentCount = index.getDocumentCount();
        double averageDocumentLength = index.getAverageDocumentLength();

        for (String term : queryTerms) {
            Map<Integer, Integer> postings = index.getPostings(term);
            if (postings.isEmpty()) {
                continue;
            }

            int documentFrequency = postings.size();
            double inverseDocumentFrequency = Math.log(1.0 + (documentCount - documentFrequency + 0.5) / (documentFrequency + 0.5));
            for (Map.Entry<Integer, Integer> entry : postings.entrySet()) {
                DocumentRecord document = index.getDocument(entry.getKey());
                int termFrequency = entry.getValue();
                double numerator = termFrequency * (K1 + 1.0);
                double denominator = termFrequency + K1 * (1.0 - B + B * document.getLength() / averageDocumentLength);
                double score = inverseDocumentFrequency * (numerator / denominator);
                scores.merge(document.getInternalId(), score, Double::sum);
            }
        }

        List<SearchResult> results = new ArrayList<>();
        scores.entrySet().stream()
            .sorted(Comparator
                .<Map.Entry<Integer, Double>>comparingDouble(Map.Entry::getValue)
                .reversed()
                .thenComparing(entry -> index.getDocument(entry.getKey()).getDocumentId()))
            .limit(topK)
            .forEach(entry -> results.add(new SearchResult(index.getDocument(entry.getKey()).getDocumentId(), entry.getValue())));
        return results;
    }
}
