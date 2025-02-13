package tracker;

import tracker.services.Managers;
import tracker.services.TaskManager;
import tracker.webapi.HttpTaskServer;
import tracker.webapi.handlers.TestDataWebAPI;

import java.io.IOException;

public class Main {
    /**
     * Настройки работы программы.
     * При необходимости можно реализовать конфигурационный файл и получать их при запуске программы.
     * - USE_FILE_STORAGE - признак использования файлового хранилища
     * - USE_TEST_DATA - признак использования демонстрационных данных
     */
    private static final boolean USE_FILE_STORAGE = false;
    private static final boolean USE_TEST_DATA = false;

    /**
     * Поле taskManager используется для хранения менеджера задач текущего экземпляра приложения.
     */
    private static final TaskManager taskManager = USE_FILE_STORAGE ? Managers.getDefault(true) : Managers.getDefault();

    public static void main(String[] args) throws IOException {
        runWebServices();   //запуск web-сервера
    }

    /**
     * Запуск веб-сервер приложения.
     */
    private static void runWebServices() throws IOException {
        HttpTaskServer httpTaskServer = new HttpTaskServer(taskManager);
        httpTaskServer.runWebServices();    //запуск web-сервера

        if (USE_TEST_DATA) TestDataWebAPI.createTask(taskManager);
    }
}