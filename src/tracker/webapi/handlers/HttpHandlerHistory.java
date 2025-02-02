package tracker.webapi.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tracker.model.tasks.Task;
import tracker.services.HistoryManager;
import tracker.services.TaskManager;
import tracker.webapi.enums.TypesRequests;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Класс обработки запросов с базовым путем HISTORY.
 */
public class HttpHandlerHistory extends BaseHttpHandler implements HttpHandler {
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
            sendError(exchange, 400, e.getMessage());   //клиент указал не корректный тип запроса (метод)
            return;
        }

        if (typeRequests.isGet()) getHistory(exchange);
    }

    public void getHistory(HttpExchange exchange) throws IOException {
        HistoryManager historyManager = taskManager.getHistoryManager();
        List<Task> taskList = historyManager.getHistory();

        String tasksJson = createGson().toJson(taskList);

        sendText(exchange, tasksJson);
    }
}