package tracker.webapi.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tracker.webapi.enums.TypesRequests;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseHttpHandler implements HttpHandler {
    /**
     * Константы значений заголовков HTTP-запросов.
     */
    private static final String HEADER_CONTENT_TYPE_JSON = "application/json;charset=utf-8";
    private static final String HEADER_CONTENT_TYPE_PLANE = "text/plane;charset=utf-8";

    /**
     * Выделяем значения параметры пути из строки запроса при их наличии.
     * Структура пути запроса:
     * 0 - базовый адрес
     * 1 - ресурс, например /tasks
     * 2 - {id}
     * 3 - subtasks
     */
    protected Map<String, String> getPathParameters(HttpExchange httpExchange) {
        URI requestURI = httpExchange.getRequestURI();  //получаем URI по которому был отправлен запрос
        String path = requestURI.getPath(); //из экземпляра URI получаем path
        String[] splitStrings = path.split("/");    // пример, /hello/{Alex} получаем массив

        Map<String, String> params = new HashMap<>();
        if (splitStrings.length == 3) {
            params.put("id", splitStrings[2]);  //{id}
        } else if (splitStrings.length == 4) {
            params.put("id", splitStrings[2]);  //{id}
            params.put(splitStrings[3], splitStrings[3]);   //subtasks
        }

        return params;
    }

    /**
     * Получаем тип запроса (метод).
     */
    protected TypesRequests getTypeRequest(HttpExchange httpExchange) throws IllegalArgumentException {
        String nameMethod = httpExchange.getRequestMethod();

        return TypesRequests.fromString(nameMethod);
    }

    /**
     * Для отправки общего ответа в случае ошибки.
     */
    protected void sendError(HttpExchange h, int errorCode, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", HEADER_CONTENT_TYPE_PLANE);
        h.sendResponseHeaders(errorCode, resp.length);

        try (OutputStream outputStream = h.getResponseBody()) {
            outputStream.write(resp);
        }

        h.close();
    }

    /**
     * Для отправки общего ответа в случае успеха.
     */
    protected void sendText(HttpExchange h, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", HEADER_CONTENT_TYPE_JSON);
        h.sendResponseHeaders(200, resp.length);

        try (OutputStream outputStream = h.getResponseBody()) {
            outputStream.write(resp);
        }

        h.close();
    }

    /**
     * Для отправки ответа об успешном создании ресурса.
     */
    protected void sendResourceCreated(HttpExchange h, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", HEADER_CONTENT_TYPE_PLANE);
        h.sendResponseHeaders(201, resp.length);

        try (OutputStream outputStream = h.getResponseBody()) {
            outputStream.write(resp);
        }

        h.close();
    }

    /**
     * Для отправки ответа в случае успеха без тела.
     */
    protected void sendTextWithoutBody(HttpExchange h) throws IOException {
        h.sendResponseHeaders(204, -1);

        h.close();
    }

    /**
     * Для отправки ответа в случае, если объект не был найден.
     */
    protected void sendNotFound(HttpExchange h, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", HEADER_CONTENT_TYPE_PLANE);
        h.sendResponseHeaders(404, resp.length);

        try (OutputStream outputStream = h.getResponseBody()) {
            outputStream.write(resp);
        }

        h.close();
    }

    /**
     * Для отправки ответа, если при создании или обновлении задача пересекается с уже существующими.
     */
    protected void sendHasInteractions(HttpExchange h, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", HEADER_CONTENT_TYPE_PLANE);
        h.sendResponseHeaders(406, resp.length);

        try (OutputStream outputStream = h.getResponseBody()) {
            outputStream.write(resp);
        }

        h.close();
    }
}