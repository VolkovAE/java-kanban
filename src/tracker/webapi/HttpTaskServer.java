package tracker.webapi;

import com.sun.net.httpserver.HttpServer;
import tracker.services.TaskManager;
import tracker.webapi.enums.BasePaths;
import tracker.webapi.handlers.*;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    /**
     * Настройки работы HTTP-сервера.
     * При необходимости можно реализовать конфигурационный файл и получать их при запуске программы.
     * - PORT - прослушиваемый порт
     */
    private static final int PORT = 8080;

    private final TaskManager taskManager;

    private HttpServer httpServer;

    public HttpTaskServer(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    /**
     * Запускаем веб-сервер для приема и обработки HTTP-запросов.
     */
    public void runWebServices() throws IOException {
        httpServer = HttpServer.create();
        InetSocketAddress inetSocketAddress = new InetSocketAddress(PORT);  //используем все доступные на ПК сети и принимаем запросы на указанный порт
        httpServer.bind(inetSocketAddress, 0);  //глубину очереди запросов берем из настроек ОС

        //Связываем базовые пути с их обработчиками.
        httpServer.createContext(BasePaths.TASKS.toString(), new HttpHandlerTasks(taskManager));
        httpServer.createContext(BasePaths.SUBTASKS.toString(), new HttpHandlerSubtasks(taskManager));
        httpServer.createContext(BasePaths.EPICS.toString(), new HttpHandlerEpics(taskManager));
        httpServer.createContext(BasePaths.HISTORY.toString(), new HttpHandlerHistory(taskManager));
        httpServer.createContext(BasePaths.PRIORITIZED.toString(), new HttpHandlerPrioritized(taskManager));

        httpServer.start();
    }

    /**
     * Останавливаем веб-сервер для приема и обработки HTTP-запросов.
     */
    public void stopWebServices() throws IOException {
        if (httpServer != null) httpServer.stop(0);
    }
}