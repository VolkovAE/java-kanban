import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.services.Managers;
import tracker.services.TaskManager;
import tracker.webapi.HttpTaskServer;
import tracker.webapi.handlers.TestDataWebAPI;
import tracker.webapi.handlers.adapters.DurationAdapter;
import tracker.webapi.handlers.adapters.LocalDateTimeAdapter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpTaskManagerPrioritizedTest {
    TaskManager taskManager = Managers.getDefault();
    HttpTaskServer httpTaskServer = new HttpTaskServer(taskManager);
    Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .create();

    @BeforeEach
    public void setUp() throws IOException {
        taskManager.delAllTasks();
        taskManager.delAllSubtasks();
        taskManager.delAllEpics();
        httpTaskServer.runWebServices();
    }

    @AfterEach
    public void shutDown() throws IOException {
        httpTaskServer.stopWebServices();
    }

    @Test
    public void testGetPrioritizedTasks() throws IOException, InterruptedException {
        TestDataWebAPI.createTask(taskManager); //используем заготовку тестовых задач

        //Порядок получения: Эпик 1 (4), Подзадача 11 (5), Задача 1 (1).
        //Для получения задач также буду использовать сервисы.

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode());

        String etalon = "[\n" +
                "  {\n" +
                "    \"epic\": {\n" +
                "      \"endTime\": \"14-01-2025*****05:32:46\",\n" +
                "      \"status\": \"NEW\",\n" +
                "      \"id\": 4,\n" +
                "      \"name\": \"Эпик 1\",\n" +
                "      \"descr\": \"Описание эпика 1.\",\n" +
                "      \"startTime\": \"14-01-2025*****05:17:46\",\n" +
                "      \"duration\": 15\n" +
                "    },\n" +
                "    \"status\": \"NEW\",\n" +
                "    \"id\": 5,\n" +
                "    \"name\": \"Подзадача 11\",\n" +
                "    \"descr\": \"Описание подзадачи 11.\",\n" +
                "    \"startTime\": \"14-01-2025*****05:17:46\",\n" +
                "    \"duration\": 15\n" +
                "  },\n" +
                "  {\n" +
                "    \"status\": \"NEW\",\n" +
                "    \"id\": 2,\n" +
                "    \"name\": \"Задача 2\",\n" +
                "    \"descr\": \"Описание задачи 2.\",\n" +
                "    \"startTime\": \"15-01-2025*****02:03:51\",\n" +
                "    \"duration\": 187\n" +
                "  },\n" +
                "  {\n" +
                "    \"status\": \"NEW\",\n" +
                "    \"id\": 1,\n" +
                "    \"name\": \"Задача 1\",\n" +
                "    \"descr\": \"Описание задачи 1.\",\n" +
                "    \"startTime\": \"17-01-2025*****14:15:13\",\n" +
                "    \"duration\": 10\n" +
                "  },\n" +
                "  {\n" +
                "    \"epic\": {\n" +
                "      \"endTime\": \"18-01-2025*****09:51:16\",\n" +
                "      \"status\": \"NEW\",\n" +
                "      \"id\": 7,\n" +
                "      \"name\": \"Эпик 2\",\n" +
                "      \"descr\": \"Описание эпика 2.\",\n" +
                "      \"startTime\": \"18-01-2025*****09:25:16\",\n" +
                "      \"duration\": 26\n" +
                "    },\n" +
                "    \"status\": \"NEW\",\n" +
                "    \"id\": 8,\n" +
                "    \"name\": \"Подзадача 21\",\n" +
                "    \"descr\": \"Описание подзадачи 21.\",\n" +
                "    \"startTime\": \"18-01-2025*****09:25:16\",\n" +
                "    \"duration\": 26\n" +
                "  }\n" +
                "]";

        assertTrue(etalon.equals(response.body()), "Информация по приоритету задач не корректна.");
    }

    @Test
    public void testMethodNotAllowed() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(405, response.statusCode());
    }
}