package ru.netology;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        ExecutorService threadPool = Executors.newFixedThreadPool(64);
        final var server = new Server();

        server.activate(threadPool);
        server.listen(9999);
    }
}