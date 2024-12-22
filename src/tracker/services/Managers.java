package tracker.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

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
}