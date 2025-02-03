package tracker.webapi.handlers;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import tracker.model.tasks.Task;
import tracker.services.Managers;
import tracker.services.TaskManager;
import tracker.services.exceptions.CrossTimeExecution;
import tracker.webapi.enums.TypesRequests;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Класс обработки запросов с базовым путем TASKS.
 */
public class HttpHandlerTasks extends BaseHttpHandler {
    private TaskManager taskManager;

    public HttpHandlerTasks(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException, RuntimeException {
        TypesRequests typeRequests;
        try {
            typeRequests = getTypeRequest(exchange);
        } catch (IllegalArgumentException e) {
            sendError(exchange, 405, e.getMessage());   //клиент указал не корректный тип запроса (метод)
            return;
        }

        Map<String, String> paramsPath = getPathParameters(exchange);
        int id = 0;
        if (!paramsPath.isEmpty()) {
            try {
                id = Integer.parseInt(paramsPath.get("id"));

                if (id <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                sendError(exchange, 400, String.format("Не корректное значение id задачи: %s", paramsPath.get("id")));  //клиент указал не корректное значение id
                return;
            }
        }

        if (typeRequests.isGet()) {
            if (id == 0) getTasks(exchange);
            else getTaskById(exchange, id);
        } else if (typeRequests.isPost()) {
            createUpdateTask(exchange);
        } else if (typeRequests.isDelete()) {
            deleteTask(exchange, id);
        } else sendError(exchange, 405, "Метод не разрешен.");
    }

    private void getTasks(HttpExchange exchange) throws IOException {
        List<Task> taskList = taskManager.getTasks();

        String tasksJson = Managers.createGson().toJson(taskList);

        sendText(exchange, tasksJson);
    }

    private void getTaskById(HttpExchange exchange, int id) throws IOException, RuntimeException {
        taskManager.getTaskByID(id).ifPresentOrElse((Task task) -> {
                    String taskJson = Managers.createGson().toJson(task);

                    try {
                        sendText(exchange, taskJson);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                },
                () -> {
                    try {
                        sendNotFound(exchange, String.format("Не найдена задача с id: %d", id));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private void createUpdateTask(HttpExchange exchange) throws IOException, RuntimeException {
        String body;

        // получаем входящий поток байтов
        try (InputStream inputStream = exchange.getRequestBody()) {
            // дожидаемся получения всех данных в виде массива байтов и конвертируем их в строку
            body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }

        Task task;
        try {
            task = Managers.createGson().fromJson(body, Task.class);
        } catch (JsonSyntaxException e) {
            sendError(exchange, 400, "Передан некорректный формат задачи.");

            return;
        }

        int id = task.getId();
        if (id == 0) createTask(exchange, task);
        else {
            //Работаем не с новым объектом (задачей) созданным из JSON, а с объектом класса Task менеджера,
            //у которого id равен переданному значению.
            taskManager.getTaskByID(id).ifPresentOrElse(
                    (Task taskGet) -> {
                        try {
                            updateTask(exchange, taskGet);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    },
                    () -> {
                        try {
                            sendNotFound(exchange, String.format("Не найдена задача с id: %d", id));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }

    private void createTask(HttpExchange exchange, Task task) throws IOException {
        try {
            int id = taskManager.addTask(task);

            sendResourceCreated(exchange, String.format("Задача успешно добавлена. Ей присвоен id: %d", id));
        } catch (CrossTimeExecution e) {
            sendHasInteractions(exchange, "Задача пересекается с существующими.");
        }
    }

    private void updateTask(HttpExchange exchange, Task task) throws IOException {
        boolean update = taskManager.updateTask(task);

        if (update) sendTextWithoutBody(exchange);
        else sendError(exchange, 500, "Сервис не смог обновить задачу. Попробуйте позже.");
    }

    private void deleteTask(HttpExchange exchange, int id) throws IOException {
        taskManager.delTaskByID(id);

        sendText(exchange, String.format("Задача с Id = %d удалена.", id));
    }
}