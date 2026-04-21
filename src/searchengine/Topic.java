package searchengine;

final class Topic {
    private final String number;
    private final String title;
    private final String description;
    private final String narrative;

    Topic(String number, String title, String description, String narrative) {
        this.number = number;
        this.title = title;
        this.description = description;
        this.narrative = narrative;
    }

    String getNumber() {
        return number;
    }

    String getTitle() {
        return title;
    }

    String getDescription() {
        return description;
    }

    String getNarrative() {
        return narrative;
    }
}
