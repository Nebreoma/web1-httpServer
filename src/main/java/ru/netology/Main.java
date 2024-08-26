package ru.netology;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        final var validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
        ExecutorService threadPool = Executors.newFixedThreadPool(64);
        final var server = new Server();
        server.activate(validPaths, threadPool);
        server.listen(9999);
        threadPool.shutdown();
    }
}

