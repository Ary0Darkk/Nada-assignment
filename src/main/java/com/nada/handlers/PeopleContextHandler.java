package com.nada.handlers;

import com.nada.store.DataStore;
import com.nada.models.Person;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PeopleContextHandler extends BaseHandler implements HttpHandler {
    private final DataStore store;
    private final Pattern contactsPattern = Pattern.compile("^/people/(\\d+)/contacts$");

    public PeopleContextHandler(DataStore store) {
        this.store = store;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            if ("/people".equals(path)) {
                if ("POST".equals(method)) {
                    handleCreatePerson(exchange);
                } else {
                    sendError(exchange, 405, "Method Not Allowed");
                }
            } else {
                Matcher matcher = contactsPattern.matcher(path);
                if (matcher.matches()) {
                    if ("GET".equals(method)) {
                        int id = Integer.parseInt(matcher.group(1));
                        handleGetContacts(exchange, id);
                    } else {
                        sendError(exchange, 405, "Method Not Allowed");
                    }
                } else {
                    sendError(exchange, 404, "Not Found");
                }
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Internal Server Error");
        }
    }

    private void handleCreatePerson(HttpExchange exchange) throws IOException {
        CreatePersonRequest req = parseBody(exchange, CreatePersonRequest.class);
        if (req == null || req.name == null || req.name.trim().isEmpty()) {
            sendError(exchange, 400, "Bad Request: missing or empty name");
            return;
        }
        
        Person p = store.createPerson(req.name);
        sendResponse(exchange, 201, p);
    }

    private void handleGetContacts(HttpExchange exchange, int id) throws IOException {
        List<Person> contacts = store.getContacts(id);
        if (contacts == null) {
            sendError(exchange, 404, "no person " + id);
            return;
        }
        sendResponse(exchange, 200, contacts);
    }

    private static class CreatePersonRequest {
        public String name;
    }
}
