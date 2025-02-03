package tracker.webapi.handlers;

import com.sun.net.httpserver.HttpExchange;
import tracker.model.tasks.Task;
import tracker.services.Managers;
import tracker.services.TaskManager;
import tracker.webapi.enums.TypesRequests;

import java.io.IOException;
import java.util.List;

/**
 * Класс обработки запросов с базовым путем PRIORITIZED.
 */
public class HttpHandlerPrioritized extends BaseHttpHandler {
    private TaskManager taskManager;

    public HttpHandlerPrioritized(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        TypesRequests typeRequests;
        try {
            typeRequests = getTypeRequest(exchange);
        } catch (IllegalArgumentException e) {
            sendError(exchange, 405, e.getMessage());   //клиент указал не корректный тип запроса (метод)
            return;
        }

        if (typeRequests.isGet()) getPrioritizedTasks(exchange);
        else sendError(exchange, 405, "Метод не разрешен.");    //если не GET запрос
    }

    public void getPrioritizedTasks(HttpExchange exchange) throws IOException {
        List<Task> taskList = taskManager.getPrioritizedTasks();

        String tasksJson = Managers.createGson().toJson(taskList);

        sendText(exchange, tasksJson);
    }
}