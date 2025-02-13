package tracker.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import tracker.webapi.handlers.adapters.DurationAdapter;
import tracker.webapi.handlers.adapters.LocalDateTimeAdapter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;

public class Managers {

    private Managers() {
    }

    public static TaskManager getDefault() {

        return new InMemoryTaskManager(getDefaultHistory());
    }

    public static TaskManager getDefault(boolean useFileStorage) {
        if (useFileStorage) {
            File file;
            try {
                file = File.createTempFile("dataTasks", null);
            } catch (IOException e) {
                System.out.println("Ошибка создания файла-хранилища: " + e.getMessage());

                return null;
            }

            Path fileName = file.toPath();

            return new FileBackedTaskManager(getDefaultHistory(), fileName);
        } else {
            return getDefault();
        }
    }

    public static HistoryManager getDefaultHistory() {

        return new InMemoryHistoryManager();
    }

    /**
     * Создаем по единым правилам объект класса Gson.
     */
    public static Gson createGson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
    }
}