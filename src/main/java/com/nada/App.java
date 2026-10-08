package com.nada;

import com.nada.store.DataStore;
import com.nada.handlers.PeopleContextHandler;
import com.nada.handlers.KnowsHandler;
import com.nada.handlers.PathHandler;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class App {
    public static void main(String[] args) throws Exception {
        int port = 8080;
        if (args.length > 0) {
            try { port = Integer.parseInt(args[0]); } catch (Exception e) { /* ignore */ }
        }
        
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        DataStore store = new DataStore();

        server.createContext("/people", new PeopleContextHandler(store));
        server.createContext("/knows", new KnowsHandler(store));
        server.createContext("/path", new PathHandler(store));
        
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port " + port);
    }
}
