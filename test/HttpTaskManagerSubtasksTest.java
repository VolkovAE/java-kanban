import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.model.enums.Status;
import tracker.model.tasks.Epic;
import tracker.model.tasks.Subtask;
import tracker.model.tasks.Task;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerSubtasksTest {
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
    public void testCreateSubtask() throws IOException, InterruptedException {
        // создаём эпик и подзадачу
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy|HH.mm.ss");

        //id = 1
        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1.");
        taskManager.addEpic(epic1);

        //id = 2
        Subtask subtask11 = new Subtask("Подзадача 11", "Описание подзадачи 11.", epic1,
                LocalDateTime.parse("14.01.2025|05.17.46", formatter), 15);

        // конвертируем её в JSON
        String taskJson = gson.toJson(subtask11);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем, что создалась одна задача с корректным именем
        List<Subtask> tasksFromManager = taskManager.getSubtasks();

        assertNotNull(tasksFromManager, "Подзадачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество подзадач");
        assertEquals("Подзадача 11", tasksFromManager.get(0).getName(), "Некорректное имя подзадачи");

        //проверяем на пересечение
        // создаём задачу
        Subtask subtask11Cross = new Subtask("Подзадача 11 cross", "Описание подзадачи 11 cross.", epic1,
                LocalDateTime.parse("14.01.2025|05.17.46", formatter), 15);

        // конвертируем её в JSON
        taskJson = gson.toJson(subtask11Cross);

        // создаём HTTP-клиент и запрос
        request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        // вызываем рест, отвечающий за создание задач
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(406, response.statusCode(), "Допущено пересечение подзадач.");
    }

    @Test
    public void testUpdateSubtask() throws IOException, InterruptedException {
        // создаём задачу
        // создаём эпик и подзадачу
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy|HH.mm.ss");

        //id = 1
        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1.");
        taskManager.addEpic(epic1);

        //id = 2
        Subtask subtask11 = new Subtask("Подзадача 11", "Описание подзадачи 11.", epic1,
                LocalDateTime.parse("14.01.2025|05.17.46", formatter), 15);

        // конвертируем её в JSON
        String taskJson = gson.toJson(subtask11);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем обновление
        Optional<Subtask> taskOpt = taskManager.getSubtaskByID(2);
        assertTrue(taskOpt.isPresent(), "Подзадача не добавлена.");

        subtask11 = taskOpt.get();

        taskJson = gson.toJson(subtask11);

        request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        // вызываем рест, отвечающий за создание задач
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(204, response.statusCode());

        // проверим смену статуса задачи при обновлении
        assertEquals(Status.IN_PROGRESS, subtask11.getStatus(), "При обновлении подзадачи не сменился статус.");
    }

    @Test
    public void testGetSubtasks() throws IOException, InterruptedException {
        TestDataWebAPI.createTask(taskManager); //используем заготовку тестовых задач

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
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

        assertTrue(etalon.equals(response.body()), "Информация по списку подзадач не корректна.");
    }

    @Test
    public void testGetTaskByID() throws IOException, InterruptedException {
        TestDataWebAPI.createTask(taskManager); //используем заготовку тестовых задач

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/5");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode());

        String etalon = "{\n" +
                "  \"epic\": {\n" +
                "    \"endTime\": \"14-01-2025*****05:32:46\",\n" +
                "    \"status\": \"NEW\",\n" +
                "    \"id\": 4,\n" +
                "    \"name\": \"Эпик 1\",\n" +
                "    \"descr\": \"Описание эпика 1.\",\n" +
                "    \"startTime\": \"14-01-2025*****05:17:46\",\n" +
                "    \"duration\": 15\n" +
                "  },\n" +
                "  \"status\": \"NEW\",\n" +
                "  \"id\": 5,\n" +
                "  \"name\": \"Подзадача 11\",\n" +
                "  \"descr\": \"Описание подзадачи 11.\",\n" +
                "  \"startTime\": \"14-01-2025*****05:17:46\",\n" +
                "  \"duration\": 15\n" +
                "}";

        assertTrue(etalon.equals(response.body()), "Информация по подзадаче не корректна.");

        //Проверим, ответ по не существующей задаче.
        url = URI.create("http://localhost:8080/subtasks/15");
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
    public void testDeleteSubtask() throws IOException, InterruptedException {
        TestDataWebAPI.createTask(taskManager); //используем заготовку тестовых задач

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/5");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // проверим, что в менеджере нет задачи
        Optional<Task> taskOpt = taskManager.getTaskByID(5);
        assertTrue(taskOpt.isEmpty(), "Подзадача не удалена.");

        // проверим, что при обращении к удаленной задаче будет ответ 404
        request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(404, response.statusCode());
    }
}