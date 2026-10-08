package com.nada;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nada.handlers.*;
import com.nada.store.DataStore;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AppTest {
    private static HttpServer server;
    private static final int PORT = 8081;
    private static final String BASE_URL = "http://localhost:" + PORT;
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Gson gson = new Gson();

    @BeforeAll
    public static void startServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        DataStore store = new DataStore();
        server.createContext("/people", new PeopleContextHandler(store));
        server.createContext("/knows", new KnowsHandler(store));
        server.createContext("/path", new PathHandler(store));
        server.start();
    }

    @AfterAll
    public static void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    public void testThreeHopLimitAPI() throws Exception {
        int idA = createPerson("A");
        int idB = createPerson("B");
        int idC = createPerson("C");
        int idD = createPerson("D");
        int idE = createPerson("E");

        addConnection(idA, idB);
        addConnection(idB, idC);
        addConnection(idC, idD);
        addConnection(idD, idE);

        // Test A to D (3 hops)
        HttpRequest reqD = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/path?from=" + idA + "&to=" + idD))
                .GET().build();
        HttpResponse<String> respD = client.send(reqD, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, respD.statusCode());
        Map<String, Object> mapD = gson.fromJson(respD.body(), new TypeToken<Map<String, Object>>(){}.getType());
        assertEquals(true, mapD.get("connected"));
        assertEquals(3.0, ((Number)mapD.get("hops")).doubleValue());

        // Test A to E (4 hops)
        HttpRequest reqE = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/path?from=" + idA + "&to=" + idE))
                .GET().build();
        HttpResponse<String> respE = client.send(reqE, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, respE.statusCode());
        Map<String, Object> mapE = gson.fromJson(respE.body(), new TypeToken<Map<String, Object>>(){}.getType());
        assertEquals(false, mapE.get("connected"));
        assertNull(mapE.get("hops"));
    }
    
    @Test
    public void testHindiNamesAndContacts() throws Exception {
        String hindiName = "आशा";
        int idA = createPerson(hindiName);
        int idB = createPerson("Rohan");
        addConnection(idA, idB);
        
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/people/" + idA + "/contacts"))
                .GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        
        List<Map<String, Object>> contacts = gson.fromJson(resp.body(), new TypeToken<List<Map<String, Object>>>(){}.getType());
        assertEquals(1, contacts.size());
        assertEquals("Rohan", contacts.get(0).get("name"));
        assertEquals((double)idB, ((Number)contacts.get(0).get("id")).doubleValue());
    }

    private int createPerson(String name) throws Exception {
        String json = "{\"name\": \"" + name + "\"}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/people"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        Map<String, Object> map = gson.fromJson(response.body(), new TypeToken<Map<String, Object>>(){}.getType());
        return ((Number) map.get("id")).intValue();
    }

    private void addConnection(int a, int b) throws Exception {
        String json = "{\"a\": " + a + ", \"b\": " + b + "}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/knows"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(204, response.statusCode());
    }
}
