package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

import static ru.netology.Server.badRequest;

public class Request {

    public static final String GET = "GET";
    public static final String POST = "POST";


    private static String method;
    private static String path;
    private static List<String> headers;
    private static List<NameValuePair> queryParams;
    private static byte[] body;


    public Request(String method, String path, List<String> headers, List<NameValuePair> params, byte[] body) {
        Request.method = method;
        Request.path = path;
        Request.headers = headers;
        Request.queryParams = params;
        Request.body = body;
    }


    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public List<NameValuePair> getQueryParam(String name) {
        return queryParams.stream()
                .filter(o -> o.getName().equals(name))
                .collect(Collectors.toList());
    }

    public List<NameValuePair> getQueryParams() {
        return queryParams;
    }

    public byte[] getBody() {
        return body;
    }

    public void setQueryParams(List<NameValuePair> queryParams) {
        Request.queryParams = queryParams;
    }

    public byte[] setBody() {
        return body;
    }


    static Request createRequest(BufferedInputStream in, BufferedOutputStream out) throws IOException, URISyntaxException {

        System.out.println("Внутри createRequest 1");
        //проблема с потоками? Они не передаются, как обычные параметры?
        final List<String> allowedMethods = List.of(GET, POST);
        // лимит на request line + заголовки
        final int limit = 4096;

        in.mark(limit);
        final var buffer = new byte[limit];
        final var read = in.read(buffer);

        // ищем request line
        System.out.println("Внутри createRequest 2");
        final var requestLineDelimiter = new byte[]{'\r', '\n'};
        final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
        if (requestLineEnd == -1) {
            badRequest(out);
            System.out.println("Ошибка при чтении запроса!");
            Thread.currentThread().interrupt();
            return null;
        }

        // читаем request line
        System.out.println("Внутри createRequest 3");
        final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
        if (requestLine.length != 3) {
            badRequest(out);
            System.out.println("Ошибка при чтении запроса!");
            Thread.currentThread().interrupt();
            return null;
        }

//ищем метод get или post
        System.out.println("Внутри createRequest 4");
        final var method = requestLine[0];
        if (!allowedMethods.contains(method)) {
            badRequest(out);
            System.out.println("Ошибка при чтении запроса!");
            Thread.currentThread().interrupt();
            return null;
        }
        System.out.println(method);


//ищем путь для файла
        System.out.println("Внутри createRequest 5");
        final var path = requestLine[1];
        if (!path.startsWith("/")) {
            badRequest(out);
            System.out.println("Ошибка при чтении запроса!");
            Thread.currentThread().interrupt();
            return null;
        }
        System.out.println(path);

//ищем query params
        //List<NameValuePair> params;
        if (path.contains("?")) {
            queryParams = URLEncodedUtils.parse(new URI(path), StandardCharsets.UTF_8);
//        System.out.println("queryParams");
            System.out.println(queryParams);
        }

// ищем заголовки
        System.out.println("Внутри createRequest 6");
        final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
        final var headersStart = requestLineEnd + requestLineDelimiter.length;
        final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
        if (headersEnd == -1) {
            badRequest(out);
            Thread.currentThread().interrupt();
            return null;
        }


        System.out.println("отматываем на начало буфера");
        // отматываем на начало буфера
        in.reset();
        // пропускаем requestLine
        in.skip(headersStart);

        final var headersBytes = in.readNBytes(headersEnd - headersStart);


        final var headers = Arrays.asList(new String(headersBytes).split("\r\n"));
        System.out.println(headers);


// ищем тело. для GET тела нет
        if (!method.equals(GET)) {
            System.out.println("читаем боди");
            in.skip(headersDelimiter.length);
            // вычитываем Content-Length, чтобы прочитать body
            final var contentLength = extractHeader(headers, "Content-Length");
            System.out.println(contentLength);
            if (contentLength.isPresent()) {
                final var length = Integer.parseInt(contentLength.get());
                body = in.readNBytes(length);
                System.out.println("Внутри боди");

                System.out.println(new String(body));
            }
        }

        return new Request(method, path, headers, queryParams, body);

    } //конец createRequest


    private static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }


    // from google guava with modifications
    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        System.out.println("Внутри indexOf");
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

}//конец класс Request