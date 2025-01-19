import org.junit.jupiter.api.Test;
import tracker.model.tasks.Epic;
import tracker.model.tasks.Subtask;
import tracker.model.tasks.Task;
import tracker.services.Managers;
import tracker.services.TaskManager;
import tracker.services.enums.TypeTask;
import tracker.services.exceptions.SetPropertyTaskException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class SubtaskTest {
    private static final TaskManager taskManager = Managers.getDefault();

    @Test
    public void shouldBeCreateSubtaskWith_startTimeEqualNull_durationEqual0() {
        //Создаем объект с пустыми свойствами startTime, duration.
        //Проверяем значение полей на корректность.
        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1.");
        taskManager.addEpic(epic1);

        Subtask subtask = new Subtask("Имя 1", "Описание 1", epic1);

        assertTrue(subtask.getStartTime().isEmpty(), "При создании Subtask без указания startTime получено" +
                " не пустое значение в этом поле.");
        assertEquals(0, subtask.getDuration(), "При создании Subtask без указания duration получено" +
                " значение отличное от 0.");

        assertTrue(subtask.getEndTime().isEmpty(), "При создании Subtask без указания startTime получено" +
                " не пустое значение в поле endTime.");
    }

    @Test
    public void shouldBeCreateTaskWith_startTimeEqualNotNull_durationEqualNot0_endTimeEqualNotNull() {
        //Создаем объект с НЕ пустыми свойствами startTime, duration.
        //Проверяем значение полей на корректность и результат метода getEndTime.
        Epic epic2 = new Epic("Эпик 2", "Описание эпика 2.");
        taskManager.addEpic(epic2);

        final long durationMinutes = 187;
        final LocalDateTime startTime = LocalDateTime.of(2026, 9, 25, 13, 35, 56);
        final LocalDateTime endTime = startTime.plusMinutes(durationMinutes);

        Subtask subtask = new Subtask("Имя 2", "Описание 2", epic2, startTime, durationMinutes);

        Optional<LocalDateTime> startTimeGet = subtask.getStartTime();

        startTimeGet.ifPresentOrElse(localDateTime -> assertEquals(startTime, localDateTime,
                        "Возвращено не корректное значение startTime."),
                () -> fail("При создании Subtask с указанием startTime получено пустое значение в этом поле."));

        assertEquals(durationMinutes, subtask.getDuration(), "Возвращено не корректное значение duration.");

        Optional<LocalDateTime> endTimeGet = subtask.getEndTime();
        endTimeGet.ifPresentOrElse(localDateTime -> assertEquals(endTime, localDateTime,
                        "Возвращено не корректное значение endTime."),
                () -> fail("При создании Subtask с указанием startTime получено пустое значение в поле endTime."));
    }

    @Test
    public void setterFieldstartTime() {
        //Применяем сеттер к свойству startTime.
        //Проверяем значение поля на корректность.
        //Проверяем, что Epic отловил изменение значения в свойстве.
        Epic epic5 = new Epic("Эпик 5", "Описание эпика 5.");
        taskManager.addEpic(epic5);

        long durationMinutes = 187;
        final LocalDateTime startTime = LocalDateTime.of(2026, 9, 25, 13, 35, 56);
        final LocalDateTime startTimeSet1 = LocalDateTime.of(2026, 9, 25, 13, 35, 57);

        Subtask subtask = new Subtask("Имя 5", "Описание 5", epic5, startTime, durationMinutes);

        subtask.setStartTime(startTimeSet1);

        Optional<LocalDateTime> startTimeGet = subtask.getStartTime();

        startTimeGet.ifPresentOrElse(localDateTime -> assertEquals(startTimeSet1, localDateTime,
                        "Возвращено не корректное значение startTime после установки сеттером."),
                () -> fail("При установки startTime сеттером, получено пустое значение в этом поле."));

        //В эпике одна подзадача, проверим, что эпик имеет одинаковые значения в одноименных полях:
        //startTime, duration, endTime
        Optional<LocalDateTime> startTimeGetEpic = epic5.getStartTime();

        startTimeGetEpic.ifPresentOrElse(localDateTime -> assertEquals(startTimeSet1, localDateTime,
                        "В эпике не корректное значение startTime после установки сеттером в подзадаче."),
                () -> fail("При установки startTime сеттером, в эпике получено пустое значение в этом поле."));

        assertEquals(subtask.getDuration(), epic5.getDuration(),
                "В эпике не корректное значение duration после установки startTime сеттером в подзадаче.");

        Optional<LocalDateTime> endTimeGetEpic = epic5.getEndTime();
        assertEquals(subtask.getEndTime(), endTimeGetEpic,
                "В эпике не корректное значение endTime после установки сеттером в подзадаче.");
    }

    @Test
    public void setterFieldduration() {
        //Применяем сеттер к свойству duration, значение > 0.
        //Проверяем значение поля на корректность.
        //Проверяем, что Epic отловил изменение значения в свойстве.
        Epic epic6 = new Epic("Эпик 6", "Описание эпика 6.");
        taskManager.addEpic(epic6);

        long durationMinutes = 187;
        final LocalDateTime startTime = LocalDateTime.of(2026, 9, 25, 13, 35, 56);

        Subtask subtask = new Subtask("Имя 6", "Описание 6", epic6, startTime, durationMinutes);

        long durationMinutesSet1 = 287;

        subtask.setDuration(durationMinutesSet1);

        assertEquals(durationMinutesSet1, subtask.getDuration(), "Возвращено не корректное значение duration после установки сеттером.");

        //В эпике одна подзадача, проверим, что эпик имеет одинаковые значения в одноименных полях:
        //startTime, duration, endTime
        Optional<LocalDateTime> startTimeGetEpic = epic6.getStartTime();

        startTimeGetEpic.ifPresentOrElse(localDateTime -> assertEquals(startTime, localDateTime,
                        "В эпике не корректное значение startTime после смены значения duration сеттером в подзадаче."),
                () -> fail("При установки duration сеттером, в эпике получено пустое значение в этом поле."));

        assertEquals(subtask.getDuration(), epic6.getDuration(),
                "В эпике не корректное значение duration после установки duration сеттером в подзадаче.");

        Optional<LocalDateTime> endTimeGetEpic = epic6.getEndTime();
        assertEquals(subtask.getEndTime(), endTimeGetEpic,
                "В эпике не корректное значение endTime после смены значения duration сеттером в подзадаче.");
    }

    @Test
    public void shouldBeThrowWithSet_durationLess0() {
        //Применяем сеттер к свойству duration, значение < 0.
        //Проверяем подбрасывание исключения, его тип и значение.
        Epic epic3 = new Epic("Эпик 3", "Описание эпика 3.");
        taskManager.addEpic(epic3);

        long durationMinutesSetNegative = -287;

        final Subtask subtask = new Subtask("Имя 3", "Описание 3", epic3);

        SetPropertyTaskException exception = assertThrows(SetPropertyTaskException.class, () -> {
                    subtask.setDuration(durationMinutesSetNegative);
                },
                "Не выброшено исключение при установке отрицательного значения в поле duration.");

        assertEquals(TypeTask.SUBTASK, exception.getTypeTask(), "В исключении не корректный тип задачи.");
        assertEquals(subtask.getId(), exception.getIdTask(), "В исключении не корректный ID задачи.");
        assertTrue(exception.getMessage().contains("Не допускается установка отрицательного значения в продолжительность выполнения задачи."),
                "В исключении не корректная информация об ошибке.");
    }

    @Test
    public void shouldBeThrowWith_startTimeEqualNull_durationEqualNot0() {
        //Создаем объект с пустым свойствам startTime и duration != 0.
        //Проверяем подбрасывание исключения, его тип и значение.
        SetPropertyTaskException exception = assertThrows(SetPropertyTaskException.class, () -> {
                    Epic epic4 = new Epic("Эпик 4", "Описание эпика 4.");
                    taskManager.addEpic(epic4);

                    Subtask subtask = new Subtask("Задача 4", "Описание 4", epic4, null, 10);
                },
                "Не выброшено исключение при создании подзадачи с startTime == null и duration != 0.");

        assertEquals(TypeTask.TASK, exception.getTypeTask(), "В исключении для ситуации создания объекта при условии" +
                " startTime == null и duration != 0 не корректный тип задачи (здесь именно тип родителя подзадачи).");
        assertTrue(exception.getMessage().contains("Не допускается создание задачи с продолжительностью выполнения не равной нулю и startTime равным null."),
                "В исключении не корректная информация об ошибке.");
    }
}