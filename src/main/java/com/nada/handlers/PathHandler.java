package com.nada.handlers;

import com.nada.store.DataStore;
import com.nada.models.Person;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.List;

public class PathHandler extends BaseHandler implements HttpHandler {
    private final DataStore store;

    public PathHandler(DataStore store) {
        this.store = store;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!"/path".equals(exchange.getRequestURI().getPath())) {
                sendError(exchange, 404, "Not Found");
                return;
            }

            if (!"GET".equals(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method Not Allowed");
                return;
            }

            String query = exchange.getRequestURI().getQuery();
            if (query == null) {
                sendError(exchange, 400, "Bad Request: missing query parameters");
                return;
            }

            Integer from = null;
            Integer to = null;

            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] kv = pair.split("=");
                if (kv.length == 2) {
                    if ("from".equals(kv[0])) {
                        try { from = Integer.parseInt(kv[1]); } catch (Exception e) { /* ignored */ }
                    } else if ("to".equals(kv[0])) {
                        try { to = Integer.parseInt(kv[1]); } catch (Exception e) { /* ignored */ }
                    }
                }
            }

            if (from == null || to == null) {
                sendError(exchange, 400, "Bad Request: from or to is missing or malformed");
                return;
            }

            if (store.getPerson(from) == null) {
                sendError(exchange, 404, "no person " + from);
                return;
            }
            if (store.getPerson(to) == null) {
                sendError(exchange, 404, "no person " + to);
                return;
            }

            List<Person> path = store.findShortestPath(from, to, 3);
            if (path == null || path.isEmpty()) {
                sendResponse(exchange, 200, new PathResponse(false, null, null));
            } else {
                sendResponse(exchange, 200, new PathResponse(true, path.size() - 1, path));
            }

        } catch (Exception e) {
            sendError(exchange, 500, "Internal Server Error");
        }
    }

    private static class PathResponse {
        public Boolean connected;
        public Integer hops;
        public List<Person> path;

        public PathResponse(Boolean connected, Integer hops, List<Person> path) {
            this.connected = connected;
            this.hops = hops;
            this.path = path;
        }
    }
}
