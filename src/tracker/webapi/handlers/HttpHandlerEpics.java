package tracker.webapi.handlers;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tracker.model.tasks.Epic;
import tracker.model.tasks.Subtask;
import tracker.services.TaskManager;
import tracker.webapi.enums.TypesRequests;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Класс обработки запросов с базовым путем EPICS.
 */
public class HttpHandlerEpics extends BaseHttpHandler implements HttpHandler {
    private TaskManager taskManager;

    public HttpHandlerEpics(TaskManager taskManager) {
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
            if (id == 0) getEpics(exchange);    //параметров пути нет
            else if (paramsPath.containsKey("subtasks"))
                getEpicSubtasks(exchange, id); //есть параметры пути: id и subtasks
            else getEpicById(exchange, id); //есть параметр пути: id
        } else if (typeRequests.isPost()) {
            createEpic(exchange);
        } else if (typeRequests.isDelete()) {
            deleteEpic(exchange, id);
        }
    }

    private void getEpics(HttpExchange exchange) throws IOException {
        List<Epic> taskList = taskManager.getEpics();

        String tasksJson = createGson().toJson(taskList);

        sendText(exchange, tasksJson);
    }

    private void getEpicById(HttpExchange exchange, int id) throws IOException, RuntimeException {
        taskManager.getEpicByID(id).ifPresentOrElse((Epic epic) -> {
                    String taskJson = createGson().toJson(epic);

                    try {
                        sendText(exchange, taskJson);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                },
                () -> {
                    try {
                        sendNotFound(exchange, String.format("Не найден эпик с id: %d", id));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private void createEpic(HttpExchange exchange) throws IOException, RuntimeException {
        String body;

        // получаем входящий поток байтов
        try (InputStream inputStream = exchange.getRequestBody()) {
            // дожидаемся получения всех данных в виде массива байтов и конвертируем их в строку
            body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }

        Epic epic;
        try {
            epic = createGson().fromJson(body, Epic.class);
            epic.setId(0);  //игнорируем id эпика, который передал клиент, мы создаем эпик
        } catch (JsonSyntaxException e) {
            sendError(exchange, 400, "Передан некорректный формат эпика.");

            return;
        }

        int id = taskManager.addEpic(epic);

        sendResourceCreated(exchange, String.format("Эпик успешно добавлен. Ему присвоен id: %d", id));
    }

    private void deleteEpic(HttpExchange exchange, int id) throws IOException {
        taskManager.delEpicByID(id);

        sendText(exchange, String.format("Эпик с Id = %d удален.", id));
    }

    private void getEpicSubtasks(HttpExchange exchange, int id) throws IOException {
        taskManager.getEpicByID(id).ifPresentOrElse((Epic epic) -> {
                    List<Subtask> subtaskList = taskManager.getSubtasksByEpic(epic);

                    String taskJson = createGson().toJson(subtaskList);
                    System.out.println(taskJson);

                    try {
                        sendText(exchange, taskJson);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                },
                () -> {
                    try {
                        sendNotFound(exchange, String.format("Не найден эпик с id: %d", id));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}