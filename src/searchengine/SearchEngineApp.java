package searchengine;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class SearchEngineApp {
    private SearchEngineApp() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            printUsage();
            return;
        }

        String command = args[0].toLowerCase();
        switch (command) {
            case "index":
                runIndex(args);
                break;
            case "search":
                runSearch(args);
                break;
            case "run":
                runAllTopics(args);
                break;
            case "serve":
                runServer(args);
                break;
            default:
                printUsage();
        }
    }

    private static void runIndex(String[] args) throws IOException {
        if (args.length < 3) {
            printUsage();
            return;
        }

        Path dataDir = Paths.get(args[1]);
        Path indexDir = Paths.get(args[2]);
        InvertedIndex index = IndexBuilder.build(dataDir);
        index.save(indexDir);
        System.out.println("Indexed " + index.getDocumentCount() + " documents into " + indexDir);
    }

    private static void runSearch(String[] args) throws IOException {
        if (args.length < 5) {
            printUsage();
            return;
        }

        Path indexDir = Paths.get(args[1]);
        Path topicsFile = Paths.get(args[2]);
        Path outputFile = Paths.get(args[3]);
        String groupId = args[4];
        int topK = args.length >= 6 ? Integer.parseInt(args[5]) : 1000;

        InvertedIndex index = InvertedIndex.load(indexDir);
        QueryParser queryParser = new QueryParser();
        TopicParser topicParser = new TopicParser();
        Searcher searcher = new Searcher(index);
        RunFileWriter.writeRunFile(outputFile, topicParser.parse(topicsFile), queryParser, searcher, groupId, topK);
        System.out.println("Run file written to " + outputFile);
    }

    private static void runAllTopics(String[] args) throws IOException {
        if (args.length < 5) {
            printUsage();
            return;
        }
        runSearch(args);
    }

    private static void runServer(String[] args) throws IOException {
        if (args.length < 2) {
            printUsage();
            return;
        }

        Path indexDir = Paths.get(args[1]);
        int port = args.length >= 3 ? Integer.parseInt(args[2]) : 8080;
        int topK = args.length >= 4 ? Integer.parseInt(args[3]) : 20;

        InvertedIndex index = InvertedIndex.load(indexDir);
        SearchWebServer server = new SearchWebServer(index, port, topK);
        server.start();
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("  java searchengine.SearchEngineApp index <dataDir> <indexDir>");
        System.out.println("  java searchengine.SearchEngineApp search <indexDir> <topicsFile> <outputFile> <groupId> [topK]");
        System.out.println("  java searchengine.SearchEngineApp run <indexDir> <topicsFile> <outputFile> <groupId> [topK]");
        System.out.println("  java searchengine.SearchEngineApp serve <indexDir> [port] [topK]");
    }
}
