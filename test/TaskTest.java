import org.junit.jupiter.api.Test;
import tracker.model.tasks.Task;
import tracker.services.enums.TypeTask;
import tracker.services.exceptions.SetPropertyTaskException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class TaskTest {
    @Test
    public void shouldBeCreateTaskWith_startTimeEqualNull_durationEqual0() {
        //Создаем объект с пустыми свойствами startTime, duration.
        //Проверяем значение полей на корректность.
        Task task = new Task("Имя 1", "Описание 1");

        assertTrue(task.getStartTime().isEmpty(), "При создании Task без указания startTime получено" +
                " не пустое значение в этом поле.");
        assertEquals(0, task.getDuration(), "При создании Task без указания duration получено" +
                " значение отличное от 0.");

        assertTrue(task.getEndTime().isEmpty(), "При создании Task без указания startTime получено" +
                " не пустое значение в поле endTime.");
    }

    @Test
    public void shouldBeCreateTaskWith_startTimeEqualNotNull_durationEqualNot0_endTimeEqualNotNull() {
        //Создаем объект с НЕ пустыми свойствами startTime, duration.
        //Проверяем значение полей на корректность и результат метода getEndTime.
        final long durationMinutes = 187;
        final LocalDateTime startTime = LocalDateTime.of(2026, 9, 25, 13, 35, 56);
        final LocalDateTime endTime = startTime.plusMinutes(durationMinutes);

        Task task = new Task("Имя 2", "Описание 2", startTime, durationMinutes);

        Optional<LocalDateTime> startTimeGet = task.getStartTime();

        startTimeGet.ifPresentOrElse(localDateTime -> assertEquals(startTime, localDateTime,
                        "Возвращено не корректное значение startTime."),
                () -> fail("При создании Task с указанием startTime получено пустое значение в этом поле."));

        assertEquals(durationMinutes, task.getDuration(), "Возвращено не корректное значение duration.");

        Optional<LocalDateTime> endTimeGet = task.getEndTime();
        endTimeGet.ifPresentOrElse(localDateTime -> assertEquals(endTime, localDateTime,
                        "Возвращено не корректное значение endTime."),
                () -> fail("При создании Task с указанием startTime получено пустое значение в поле endTime."));
    }

    @Test
    public void setterFieldstartTime() {
        //Применяем сеттер к свойству startTime.
        //Проверяем значение поля на корректность.
        long durationMinutes = 187;
        final LocalDateTime startTime = LocalDateTime.of(2026, 9, 25, 13, 35, 56);
        final LocalDateTime startTimeSet1 = LocalDateTime.of(2026, 9, 25, 13, 35, 57);

        Task task = new Task("Имя 2", "Описание 2", startTime, durationMinutes);

        task.setStartTime(startTimeSet1);

        Optional<LocalDateTime> startTimeGet = task.getStartTime();

        startTimeGet.ifPresentOrElse(localDateTime -> assertEquals(startTimeSet1, localDateTime,
                        "Возвращено не корректное значение startTime после установки сеттером."),
                () -> fail("При установки startTime сеттером, получено пустое значение в этом поле."));
    }

    @Test
    public void setterFieldduration() {
        //Применяем сеттер к свойству duration, значение > 0.
        //Проверяем значение поля на корректность.
        long durationMinutes = 187;
        final LocalDateTime startTime = LocalDateTime.of(2026, 9, 25, 13, 35, 56);

        Task task = new Task("Имя 2", "Описание 2", startTime, durationMinutes);

        long durationMinutesSet1 = 287;

        task.setDuration(durationMinutesSet1);

        assertEquals(durationMinutesSet1, task.getDuration(), "Возвращено не корректное значение duration после установки сеттером.");
    }

    @Test
    public void shouldBeThrowWithSet_durationLess0() {
        //Применяем сеттер к свойству duration, значение < 0.
        //Проверяем подбрасывание исключения, его тип и значение.
        long durationMinutesSetNegative = -287;

        final Task task1 = new Task("Имя 3", "Описание 3");

        SetPropertyTaskException exception = assertThrows(SetPropertyTaskException.class, () -> {
                    task1.setDuration(durationMinutesSetNegative);
                },
                "Не выброшено исключение при установке отрицательного значения в поле duration.");

        assertEquals(TypeTask.TASK, exception.getTypeTask(), "В исключении не корректный тип задачи.");
        assertEquals(task1.getId(), exception.getIdTask(), "В исключении не корректный ID задачи.");
        assertTrue(exception.getMessage().contains("Не допускается установка отрицательного значения в продолжительность выполнения задачи."),
                "В исключении не корректная информация об ошибке.");
    }

    @Test
    public void shouldBeThrowWith_startTimeEqualNull_durationEqualNot0() {
        //Создаем объект с пустым свойствам startTime и duration != 0.
        //Проверяем подбрасывание исключения, его тип и значение.
        SetPropertyTaskException exception = assertThrows(SetPropertyTaskException.class, () -> {
                    Task task2 = new Task("Задача ...", "Описание ...", null, 10);
                },
                "Не выброшено исключение при создании задачи с startTime == null и duration != 0.");

        assertEquals(TypeTask.TASK, exception.getTypeTask(), "В исключении для ситуации создания объекта при условии" +
                " startTime == null и duration != 0 не корректный тип задачи.");
        assertTrue(exception.getMessage().contains("Не допускается создание задачи с продолжительностью выполнения не равной нулю и startTime равным null."),
                "В исключении не корректная информация об ошибке.");
    }
}