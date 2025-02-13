package tracker.webapi.handlers;

import com.sun.net.httpserver.HttpExchange;
import tracker.model.tasks.Task;
import tracker.services.HistoryManager;
import tracker.services.Managers;
import tracker.services.TaskManager;
import tracker.webapi.enums.TypesRequests;

import java.io.IOException;
import java.util.List;

/**
 * Класс обработки запросов с базовым путем HISTORY.
 */
public class HttpHandlerHistory extends BaseHttpHandler {
    private TaskManager taskManager;

    public HttpHandlerHistory(TaskManager taskManager) {
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

        if (typeRequests.isGet()) getHistory(exchange);
        else sendError(exchange, 405, "Метод не разрешен.");    //если не GET запрос
    }

    public void getHistory(HttpExchange exchange) throws IOException {
        HistoryManager historyManager = taskManager.getHistoryManager();
        List<Task> taskList = historyManager.getHistory();

        String tasksJson = Managers.createGson().toJson(taskList);

        sendText(exchange, tasksJson);
    }
}