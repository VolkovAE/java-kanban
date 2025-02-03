import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.model.tasks.Epic;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerEpicsTest {
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
    public void testCreateEpic() throws IOException, InterruptedException {
        // создаём эпик
        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1.");

        // конвертируем её в JSON
        String taskJson = gson.toJson(epic1);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем, что создалась одна задача с корректным именем
        List<Epic> tasksFromManager = taskManager.getEpics();

        assertNotNull(tasksFromManager, "Эпики не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество эпиков");
        assertEquals("Эпик 1", tasksFromManager.get(0).getName(), "Некорректное имя эпика");
    }

    @Test
    public void testGetEpics() throws IOException, InterruptedException {
        TestDataWebAPI.createTask(taskManager); //используем заготовку тестовых задач

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
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
                "    \"endTime\": \"14-01-2025*****05:32:46\",\n" +
                "    \"status\": \"NEW\",\n" +
                "    \"id\": 4,\n" +
                "    \"name\": \"Эпик 1\",\n" +
                "    \"descr\": \"Описание эпика 1.\",\n" +
                "    \"startTime\": \"14-01-2025*****05:17:46\",\n" +
                "    \"duration\": 15\n" +
                "  },\n" +
                "  {\n" +
                "    \"endTime\": \"18-01-2025*****09:51:16\",\n" +
                "    \"status\": \"NEW\",\n" +
                "    \"id\": 7,\n" +
                "    \"name\": \"Эпик 2\",\n" +
                "    \"descr\": \"Описание эпика 2.\",\n" +
                "    \"startTime\": \"18-01-2025*****09:25:16\",\n" +
                "    \"duration\": 26\n" +
                "  }\n" +
                "]";

        assertTrue(etalon.equals(response.body()), "Информация по списку эпиков не корректна.");
    }

    @Test
    public void testGetEpicByID() throws IOException, InterruptedException {
        TestDataWebAPI.createTask(taskManager); //используем заготовку тестовых задач

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/7");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode());

        String etalon = "{\n" +
                "  \"endTime\": \"18-01-2025*****09:51:16\",\n" +
                "  \"status\": \"NEW\",\n" +
                "  \"id\": 7,\n" +
                "  \"name\": \"Эпик 2\",\n" +
                "  \"descr\": \"Описание эпика 2.\",\n" +
                "  \"startTime\": \"18-01-2025*****09:25:16\",\n" +
                "  \"duration\": 26\n" +
                "}";

        assertTrue(etalon.equals(response.body()), "Информация по эпику не корректна.");

        //Проверим, ответ по не существующей задаче.
        url = URI.create("http://localhost:8080/epics/17");
        request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        // вызываем рест, отвечающий за создание задач
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(404, response.statusCode());
    }

    @Test
    public void testDeleteEpic() throws IOException, InterruptedException {
        TestDataWebAPI.createTask(taskManager); //используем заготовку тестовых задач

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/7");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // проверим, что в менеджере нет задачи
        Optional<Epic> taskOpt = taskManager.getEpicByID(7);
        assertTrue(taskOpt.isEmpty(), "Эпик не удален.");

        // проверим, что при обращении к удаленной задаче будет ответ 404
        request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(404, response.statusCode());
    }

    @Test
    public void testGetEpicSubtasks() throws IOException, InterruptedException {
        TestDataWebAPI.createTask(taskManager); //используем заготовку тестовых задач

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/4/subtasks");
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
                "    \"id\": 6,\n" +
                "    \"name\": \"Подзадача 12\",\n" +
                "    \"descr\": \"Описание подзадачи 12.\",\n" +
                "    \"startTime\": \"\",\n" +
                "    \"duration\": 0\n" +
                "  }\n" +
                "]";

        assertTrue(etalon.equals(response.body()), "Информация по списку подзадач эпика не корректна.");
    }

    @Test
    public void testMethodNotAllowed() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .PUT(HttpRequest.BodyPublishers.ofString(""))
                .build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(405, response.statusCode());
    }
}