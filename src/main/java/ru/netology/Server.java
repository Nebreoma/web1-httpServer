package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private final List<String> validPaths = List.of("/index.html",
            "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js",
            "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");

    static private ServerSocket serverSocket;
    static int SERVER_PORT;
    static private ExecutorService threadPool;
    static private ConcurrentHashMap<String, Map<String, Handler>> handlers;


    //запуск сервера
    public void start(int serverPort, int threadPoolSize) {
//        try {
        SERVER_PORT = serverPort;
        threadPool = Executors.newFixedThreadPool(threadPoolSize);
        handlers = new ConcurrentHashMap<>();
        System.out.println("Server start!");

//        } catch (IOException e) {
//            System.out.println("Ошибка при запуске сервера!");
//        }
    }


    //остановка сервера
    public void stop() {
        try {
            threadPool.shutdown();
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("Ошибка при остановке сервера!");
        }
    }

    //работа сервера
    public void listen() {
        try {
            serverSocket = new ServerSocket(SERVER_PORT);

            while (!serverSocket.isClosed()) {
                try {
                    final var socket = serverSocket.accept();
                    threadPool.execute(() -> {
                        try (final var in = new BufferedInputStream(socket.getInputStream());
                             final var out = new BufferedOutputStream(socket.getOutputStream())) {
                            System.out.println("Подключение клиента!");
                            connected(in, out);
                            System.out.println("Запуск потока!");
                        } catch (IOException ex) {
                            System.out.println("Ошибка при подключении потока клиента!");
                            Thread.currentThread().interrupt();
                        }
                    });
                } catch (IOException ex) {
                    System.out.println("Ошибка при подключении потока клиента!");
                    break;
                }
            } //конец while

        } catch (IOException e) {
            System.out.println("Ошибка при запуске сервера!");
        }
    }


    public void addHandler(String method, String path, Handler handler) {

        if (!handlers.containsKey(method)) {
            handlers.put(method, new ConcurrentHashMap<>());
        }
        handlers.get(method).put(path, handler);
    }


    private static void connected(BufferedInputStream in, BufferedOutputStream out) {

        try {
            System.out.println("Внутри потока1");
            Request request = Request.createRequest(in, out);
            System.out.println("Внутри потока2");
            // Check for bad requests and drop connection

            if (request == null || !handlers.containsKey(request.getMethod())) {
                badRequest(out);
                Thread.currentThread().interrupt();
            } else {
                Handler handler = handlers.get(request.getMethod()).get(request.getPath());
                handler.handle(request, out);
//            } else {
//                out.write((
//                        "HTTP/1.1 200 OK\r\n" +
//                                "Content-Length: 0\r\n" +
//                                "Connection: close\r\n" +
//                                "\r\n"
//                ).getBytes());
//                out.flush();
            }

        } catch (IOException | URISyntaxException e) {
            System.out.println("Ошибка!!!");
            Thread.currentThread().interrupt();
        }

    }//конец метода connected


    public static void badRequest(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 400 Bad Request\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
        Thread.currentThread().interrupt();
    }

}