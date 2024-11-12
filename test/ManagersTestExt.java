import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tracker.services.Managers;

public class ManagersTestExt {

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
}