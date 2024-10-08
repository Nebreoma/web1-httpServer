package ru.netology;

import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;

public class Server {
    final static List validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");;
    static ExecutorService threadPool;

    //методами для: запуска;
    public static void activate(ExecutorService threadPool) {
        Server.threadPool = threadPool;
    }

    //метод для: обработки конкретного подключения.
    public static void listen(int port) { //9999
        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                threadPool.execute(() -> {
                    try (
                            final var socket = serverSocket.accept();
                            final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            final var out = new BufferedOutputStream(socket.getOutputStream());
                    ) {
                        // read only request line for simplicity
                        // must be in form GET /path HTTP/1.1
                        final var requestLine = in.readLine();
                        final var parts = requestLine.split(" ");

                        if (parts.length != 3) {
                            // just close socket
                            Thread.currentThread().interrupt();
                        }

                        final var path = parts[1];
                        if (!validPaths.contains(path)) {
                            out.write((
                                    "HTTP/1.1 404 Not Found\r\n" +
                                            "Content-Length: 0\r\n" +
                                            "Connection: close\r\n" +
                                            "\r\n"
                            ).getBytes());
                            out.flush();
                            Thread.currentThread().interrupt();
                        }

                        final var filePath = Path.of(".", "public", path);
                        final var mimeType = Files.probeContentType(filePath);

                        // special case for classic
                        if (path.equals("/classic.html")) {
                            final var template = Files.readString(filePath);
                            final var content = template.replace(
                                    "{time}",
                                    LocalDateTime.now().toString()
                            ).getBytes();
                            out.write((
                                    "HTTP/1.1 200 OK\r\n" +
                                            "Content-Type: " + mimeType + "\r\n" +
                                            "Content-Length: " + content.length + "\r\n" +
                                            "Connection: close\r\n" +
                                            "\r\n"
                            ).getBytes());
                            out.write(content);
                            out.flush();
                            Thread.currentThread().interrupt();
                        }

                        final var length = Files.size(filePath);
                        out.write((
                                "HTTP/1.1 200 OK\r\n" +
                                        "Content-Type: " + mimeType + "\r\n" +
                                        "Content-Length: " + length + "\r\n" +
                                        "Connection: close\r\n" +
                                        "\r\n"
                        ).getBytes());
                        Files.copy(filePath, out);
                        out.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Thread.currentThread().interrupt();
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }//конец метода
}//конец класса