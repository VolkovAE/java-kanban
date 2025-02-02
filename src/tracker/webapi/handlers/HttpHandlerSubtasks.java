package tracker.webapi.handlers;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tracker.model.tasks.Subtask;
import tracker.services.TaskManager;
import tracker.services.exceptions.CrossTimeExecution;
import tracker.webapi.enums.TypesRequests;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Класс обработки запросов с базовым путем SUBTASKS.
 */
public class HttpHandlerSubtasks extends BaseHttpHandler implements HttpHandler {
    private TaskManager taskManager;

    public HttpHandlerSubtasks(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException, RuntimeException {
        TypesRequests typeRequests;
        try {
            typeRequests = getTypeRequest(exchange);
        } catch (IllegalArgumentException e) {
            sendError(exchange, 400, e.getMessage());   //клиент указал не корректный тип запроса (метод)
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
            if (id == 0) getSubtasks(exchange);
            else getSubtaskById(exchange, id);
        } else if (typeRequests.isPost()) {
            createUpdateSubtask(exchange);
        } else if (typeRequests.isDelete()) {
            deleteSubtask(exchange, id);
        }
    }

    private void getSubtasks(HttpExchange exchange) throws IOException {
        List<Subtask> taskList = taskManager.getSubtasks();

        String tasksJson = createGson().toJson(taskList);

        sendText(exchange, tasksJson);
    }

    private void getSubtaskById(HttpExchange exchange, int id) throws IOException, RuntimeException {
        taskManager.getSubtaskByID(id).ifPresentOrElse((Subtask subtask) -> {
                    String taskJson = createGson().toJson(subtask);

                    try {
                        sendText(exchange, taskJson);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                },
                () -> {
                    try {
                        sendNotFound(exchange, String.format("Не найдена подзадача с id: %d", id));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private void createUpdateSubtask(HttpExchange exchange) throws IOException, RuntimeException {
        String body;

        // получаем входящий поток байтов
        try (InputStream inputStream = exchange.getRequestBody()) {
            // дожидаемся получения всех данных в виде массива байтов и конвертируем их в строку
            body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }

        Subtask subtask;
        try {
            subtask = createGson().fromJson(body, Subtask.class);
        } catch (JsonSyntaxException e) {
            sendError(exchange, 400, "Передан некорректный формат задачи.");

            return;
        }

        int id = subtask.getId();
        if (id == 0) createSubtask(exchange, subtask);
        else {
            //Работаем не с новым объектом (подзадачей) созданным из JSON, а с объектом класса Subtask менеджера,
            //у которого id равен переданному значению.
            taskManager.getSubtaskByID(id).ifPresentOrElse(
                    (Subtask subtaskGet) -> {
                        try {
                            updateSubtask(exchange, subtaskGet);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    },
                    () -> {
                        try {
                            sendNotFound(exchange, String.format("Не найдена подзадача с id: %d", id));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }

    private void createSubtask(HttpExchange exchange, Subtask subtask) throws IOException {
        try {
            int id = taskManager.addSubtask(subtask);

            sendResourceCreated(exchange, String.format("Подзадача успешно добавлена. Ей присвоен id: %d", id));
        } catch (CrossTimeExecution e) {
            sendHasInteractions(exchange, "Подзадача пересекается с существующими.");
        }
    }

    private void updateSubtask(HttpExchange exchange, Subtask subtask) throws IOException {
        boolean update = taskManager.updateSubtask(subtask);

        if (update) sendTextWithoutBody(exchange);
        else sendError(exchange, 500, "Сервис не смог обновить подзадачу. Попробуйте позже.");
    }

    private void deleteSubtask(HttpExchange exchange, int id) throws IOException {
        taskManager.delSubtaskByID(id);

        sendText(exchange, String.format("Подзадача с Id = %d удалена.", id));
    }
}