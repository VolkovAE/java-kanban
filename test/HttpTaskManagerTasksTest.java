import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.model.enums.Status;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerTasksTest {
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
    public void testCreateTask() throws IOException, InterruptedException {
        // создаём задачу
        Task task = new Task("Test 2", "Testing task 2", LocalDateTime.now(), 5);

        // конвертируем её в JSON
        String taskJson = gson.toJson(task);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем, что создалась одна задача с корректным именем
        List<Task> tasksFromManager = taskManager.getTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 2", tasksFromManager.get(0).getName(), "Некорректное имя задачи");

        //проверяем на пересечение
        // создаём задачу
        Task taskCross = new Task("Test 2 cross", "Testing task 2 cross", LocalDateTime.now(), 5);

        // конвертируем её в JSON
        taskJson = gson.toJson(task);

        // создаём HTTP-клиент и запрос
        request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        // вызываем рест, отвечающий за создание задач
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(406, response.statusCode(), "Допущено пересечение задач.");
    }

    @Test
    public void testUpdateTask() throws IOException, InterruptedException {
        // создаём задачу
        Task task = new Task("Test 2", "Testing task 2", LocalDateTime.now(), 5);

        // конвертируем её в JSON
        String taskJson = gson.toJson(task);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем обновление
        Optional<Task> taskOpt = taskManager.getTaskByID(1);
        assertTrue(taskOpt.isPresent(), "Задача не добавлена.");

        task = taskOpt.get();

        taskJson = gson.toJson(task);

        request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        // вызываем рест, отвечающий за создание задач
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(204, response.statusCode());

        // проверим смену статуса задачи при обновлении
        assertEquals(Status.IN_PROGRESS, task.getStatus(), "При обновлении задачи не сменился статус.");
    }

    @Test
    public void testGetTasks() throws IOException, InterruptedException {
        TestDataWebAPI.createTask(taskManager); //используем заготовку тестовых задач

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
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
                "    \"status\": \"NEW\",\n" +
                "    \"id\": 1,\n" +
                "    \"name\": \"Задача 1\",\n" +
                "    \"descr\": \"Описание задачи 1.\",\n" +
                "    \"startTime\": \"17-01-2025*****14:15:13\",\n" +
                "    \"duration\": 10\n" +
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
                "    \"id\": 3,\n" +
                "    \"name\": \"Задача 3\",\n" +
                "    \"descr\": \"Описание задачи 3.\",\n" +
                "    \"startTime\": \"\",\n" +
                "    \"duration\": 0\n" +
                "  }\n" +
                "]";

        assertTrue(etalon.equals(response.body()), "Информация по списку задач не корректна.");
    }

    @Test
    public void testGetTaskByID() throws IOException, InterruptedException {
        TestDataWebAPI.createTask(taskManager); //используем заготовку тестовых задач

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/2");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode());

        String etalon = "{\n" +
                "  \"status\": \"NEW\",\n" +
                "  \"id\": 2,\n" +
                "  \"name\": \"Задача 2\",\n" +
                "  \"descr\": \"Описание задачи 2.\",\n" +
                "  \"startTime\": \"15-01-2025*****02:03:51\",\n" +
                "  \"duration\": 187\n" +
                "}";

        assertTrue(etalon.equals(response.body()), "Информация по задаче не корректна.");

        //Проверим, ответ по не существующей задаче.
        url = URI.create("http://localhost:8080/tasks/5");
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
    public void testDeleteTask() throws IOException, InterruptedException {
        TestDataWebAPI.createTask(taskManager); //используем заготовку тестовых задач

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/2");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // проверим, что в менеджере нет задачи
        Optional<Task> taskOpt = taskManager.getTaskByID(2);
        assertTrue(taskOpt.isEmpty(), "Задача не удалена.");

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