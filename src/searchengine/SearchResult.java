package searchengine;

final class SearchResult {
    private final String documentId;
    private final double score;

    SearchResult(String documentId, double score) {
        this.documentId = documentId;
        this.score = score;
    }

    String getDocumentId() {
        return documentId;
    }

    double getScore() {
        return score;
    }
}
