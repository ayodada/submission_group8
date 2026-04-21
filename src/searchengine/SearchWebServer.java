package searchengine;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class SearchWebServer {
    private final InvertedIndex index;
    private final QueryParser queryParser;
    private final Searcher searcher;
    private final int port;
    private final int topK;

    SearchWebServer(InvertedIndex index, int port, int topK) {
        this.index = index;
        this.port = port;
        this.topK = topK;
        this.queryParser = new QueryParser();
        this.searcher = new Searcher(index);
    }

    void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new SearchHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Search UI running at http://localhost:" + port);
        System.out.println("Indexed documents: " + index.getDocumentCount());
    }

    private final class SearchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            if (!"GET".equalsIgnoreCase(method)) {
                send(exchange, 405, "text/plain; charset=UTF-8", "Method Not Allowed");
                return;
            }

            Map<String, String> params = parseQuery(exchange.getRequestURI().getRawQuery());
            String query = params.containsKey("q") ? params.get("q").trim() : "";
            List<SearchResult> results = query.isEmpty()
                ? Collections.<SearchResult>emptyList()
                : searcher.search(queryParser.parseText(query), topK);

            String page = renderPage(query, results);
            send(exchange, 200, "text/html; charset=UTF-8", page);
        }
    }

    private void send(HttpExchange exchange, int status, String contentType, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
        }
    }

    private Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> params = new HashMap<String, String>();
        if (rawQuery == null || rawQuery.trim().isEmpty()) {
            return params;
        }

        String[] pairs = rawQuery.split("&");
        for (String pair : pairs) {
            int split = pair.indexOf('=');
            String key = split >= 0 ? pair.substring(0, split) : pair;
            String value = split >= 0 ? pair.substring(split + 1) : "";
            params.put(urlDecode(key), urlDecode(value));
        }
        return params;
    }

    private String urlDecode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private String renderPage(String query, List<SearchResult> results) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\">");
        html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        html.append("<title>Ali Search Engine</title>");
        html.append("<style>");
        html.append(":root{");
        html.append("--bg:#f4efe6;--ink:#1f2a1f;--muted:#5c665c;--card:#fffdf8;");
        html.append("--line:#d9cfbf;--accent:#0f766e;--accent2:#d97706;--shadow:0 18px 50px rgba(31,42,31,.12);}");
        html.append("*{box-sizing:border-box}body{margin:0;font-family:Georgia,'Times New Roman',serif;color:var(--ink);");
        html.append("background:radial-gradient(circle at top left,#fff8e8 0%,#f4efe6 45%,#ebe2d3 100%);} ");
        html.append(".shell{max-width:1100px;margin:0 auto;padding:32px 20px 56px;} ");
        html.append(".hero{padding:28px;border:1px solid var(--line);border-radius:28px;background:linear-gradient(135deg,rgba(255,253,248,.96),rgba(247,239,222,.92));box-shadow:var(--shadow);position:relative;overflow:hidden;} ");
        html.append(".hero:before,.hero:after{content:'';position:absolute;border-radius:999px;opacity:.55;} ");
        html.append(".hero:before{width:280px;height:280px;background:rgba(15,118,110,.08);top:-120px;right:-60px;} ");
        html.append(".hero:after{width:220px;height:220px;background:rgba(217,119,6,.10);bottom:-120px;left:-80px;} ");
        html.append(".eyebrow{letter-spacing:.18em;text-transform:uppercase;font:700 12px/1.2 Arial,sans-serif;color:var(--accent);} ");
        html.append("h1{margin:10px 0 8px;font-size:clamp(32px,7vw,64px);line-height:.95;} ");
        html.append(".sub{max-width:700px;color:var(--muted);font:400 17px/1.6 Arial,sans-serif;margin-bottom:24px;} ");
        html.append("form{display:grid;grid-template-columns:1fr auto;gap:12px;margin-top:18px;} ");
        html.append("input[type=text]{width:100%;padding:18px 20px;border-radius:18px;border:1px solid var(--line);background:#fff;font:400 17px Arial,sans-serif;box-shadow:inset 0 1px 0 rgba(255,255,255,.8);} ");
        html.append("button{border:none;border-radius:18px;padding:0 22px;background:linear-gradient(135deg,var(--accent),#115e59);color:#fff;font:700 15px Arial,sans-serif;cursor:pointer;min-height:58px;} ");
        html.append(".meta{display:flex;gap:14px;flex-wrap:wrap;margin-top:18px;font:600 13px Arial,sans-serif;color:var(--muted);} ");
        html.append(".pill{padding:8px 12px;border:1px solid var(--line);border-radius:999px;background:rgba(255,255,255,.7);} ");
        html.append(".results{margin-top:28px;display:grid;gap:16px;} ");
        html.append(".card{background:var(--card);border:1px solid var(--line);border-radius:22px;padding:18px 20px;box-shadow:var(--shadow);animation:rise .35s ease both;} ");
        html.append(".rank{display:inline-block;min-width:34px;padding:7px 10px;border-radius:999px;background:rgba(15,118,110,.1);color:var(--accent);font:700 12px Arial,sans-serif;margin-bottom:10px;} ");
        html.append(".docid{font-size:24px;margin:0 0 8px;} ");
        html.append(".score{font:600 14px Arial,sans-serif;color:var(--accent2);} ");
        html.append(".helper{margin-top:24px;padding:16px 18px;border-left:4px solid var(--accent2);background:rgba(255,250,240,.85);border-radius:16px;font:400 15px/1.6 Arial,sans-serif;color:var(--muted);} ");
        html.append("@keyframes rise{from{opacity:0;transform:translateY(10px)}to{opacity:1;transform:none}} ");
        html.append("@media (max-width:760px){form{grid-template-columns:1fr}.hero{padding:22px}h1{line-height:1.02}.docid{font-size:20px}}");
        html.append("</style></head><body>");
        html.append("<div class=\"shell\">");
        html.append("<section class=\"hero\">");
        html.append("<div class=\"eyebrow\">Search Demo</div>");
        html.append("<h1>Search the WT Web Collection</h1>");
        html.append("<div class=\"sub\">This page is a simple front end for the local index. Type a query below and the system will show BM25 ranked results.</div>");
        html.append("<form method=\"get\" action=\"/\">");
        html.append("<input type=\"text\" name=\"q\" placeholder=\"Example: foreign minorities germany\" value=\"").append(escapeHtml(query)).append("\">");
        html.append("<button type=\"submit\">Search</button>");
        html.append("</form>");
        html.append("<div class=\"meta\">");
        html.append("<div class=\"pill\">Documents indexed: ").append(index.getDocumentCount()).append("</div>");
        html.append("<div class=\"pill\">Ranking: BM25</div>");
        html.append("<div class=\"pill\">Top results shown: ").append(topK).append("</div>");
        html.append("</div>");
        html.append("</section>");

        if (query.trim().isEmpty()) {
            html.append("<div class=\"helper\">Use the box above to test queries, take screenshots, or quickly check whether the ranking looks reasonable before submission.</div>");
        } else {
            html.append("<section class=\"results\">");
            if (results.isEmpty()) {
                html.append("<div class=\"card\"><div class=\"docid\">No results found</div><div class=\"score\">Try a shorter query or different words.</div></div>");
            } else {
                for (int i = 0; i < results.size(); i++) {
                    SearchResult result = results.get(i);
                    html.append("<article class=\"card\">");
                    html.append("<div class=\"rank\">#").append(i + 1).append("</div>");
                    html.append("<h2 class=\"docid\">").append(escapeHtml(result.getDocumentId())).append("</h2>");
                    html.append("<div class=\"score\">Score: ").append(String.format(Locale.ROOT, "%.6f", result.getScore())).append("</div>");
                    html.append("</article>");
                }
            }
            html.append("</section>");
        }

        html.append("</div></body></html>");
        return html.toString();
    }

    private String escapeHtml(String value) {
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }
}
