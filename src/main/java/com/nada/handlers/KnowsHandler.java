package com.nada.handlers;

import com.nada.store.DataStore;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

public class KnowsHandler extends BaseHandler implements HttpHandler {
    private final DataStore store;

    public KnowsHandler(DataStore store) {
        this.store = store;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!"/knows".equals(exchange.getRequestURI().getPath())) {
                sendError(exchange, 404, "Not Found");
                return;
            }

            if (!"POST".equals(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method Not Allowed");
                return;
            }

            KnowsRequest req = parseBody(exchange, KnowsRequest.class);
            if (req == null || req.a == null || req.b == null) {
                sendError(exchange, 400, "Bad Request: missing a or b");
                return;
            }
            if (req.a.equals(req.b)) {
                sendError(exchange, 400, "Bad Request: a cannot equal b");
                return;
            }

            boolean success = store.addConnection(req.a, req.b);
            if (!success) {
                int missing = store.getPerson(req.a) == null ? req.a : req.b;
                sendError(exchange, 404, "no person " + missing);
                return;
            }

            sendResponse(exchange, 204, null);
        } catch (Exception e) {
            sendError(exchange, 500, "Internal Server Error");
        }
    }

    private static class KnowsRequest {
        public Integer a;
        public Integer b;
    }
}
