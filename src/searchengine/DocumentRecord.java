package searchengine;

final class DocumentRecord {
    private final int internalId;
    private final String documentId;
    private final int length;

    DocumentRecord(int internalId, String documentId, int length) {
        this.internalId = internalId;
        this.documentId = documentId;
        this.length = length;
    }

    int getInternalId() {
        return internalId;
    }

    String getDocumentId() {
        return documentId;
    }

    int getLength() {
        return length;
    }
}
