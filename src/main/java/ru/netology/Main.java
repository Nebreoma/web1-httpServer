package ru.netology;

public class Main {

    public static void main(String[] args) {

        final int SERVER_PORT = 9999;
        final int THREAD_POOL_SIZE = 64;

        Server server = new Server();
        server.start(SERVER_PORT, THREAD_POOL_SIZE);

        server.addHandler("GET", "/messages", (request, responseStream) -> {

            // TODO: handlers code
            String response = "{\"/messages\":  [\"Hello, World!\"]}";
            responseStream.write(("HTTP/1.1 200 OK\r\n" +
                    "Content-Type: application/json\r\n" +
                    "Content-Length: " + response.length() + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n").getBytes());
            responseStream.write(response.getBytes());
            responseStream.flush();
        });
        server.addHandler("POST", "/messages", (request, responseStream) -> {
            // TODO: handlers code
            String response = "{\"messages\": [\"Hello, World!\"]}";
            responseStream.write(("HTTP/1.1 200 OK\r\n" +
                    "Connection: close\r\n" +
                    "\r\n").getBytes());
            responseStream.write(response.getBytes());
            responseStream.flush();
        });

//        server.start(SERVER_PORT, THREAD_POOL_SIZE);
//
        server.listen();

        server.stop();

    }
}