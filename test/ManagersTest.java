import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tracker.services.FileBackedTaskManager;
import tracker.services.Managers;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ManagersTest {

    @Test
    public void shouldGetDefaultIsNotNull() {
        //убедитесь, что утилитарный класс всегда возвращает проинициализированные и готовые к работе экземпляры менеджеров;
        Assertions.assertNotNull(Managers.getDefault(), "");
    }

    @Test
    public void shouldGetDefaultHistoryIsNotNull() {
        //убедитесь, что утилитарный класс всегда возвращает проинициализированные и готовые к работе экземпляры менеджеров;
        Assertions.assertNotNull(Managers.getDefaultHistory(), "");
    }

    @Test
    public void checkGetTaskManager() {
        //Проверим, что менеджеры создаются успешно и нужного типа (расширение предыдущих тестов).

        //Менеджер использующий файловое хранилище.
        Object obj = Managers.getDefault(true);

        assertNotNull(obj, "Менеджер задач использующий файловое хранилище не создан.");

        assertInstanceOf(FileBackedTaskManager.class, obj, "Созданный менеджер не является " +
                "менеджером использующим файловое хранилище.");

        //Менеджер использующий оперативную память.
        obj = Managers.getDefault(false);

        assertNotNull(obj, "Менеджер задач использующий только оперативную память не создан.");

        assertInstanceOf(FileBackedTaskManager.class.getSuperclass(), obj, "Созданный менеджер не является " +
                "менеджером использующим только оперативную память.");
    }
}