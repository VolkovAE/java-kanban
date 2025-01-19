import org.junit.jupiter.api.Test;
import tracker.model.tasks.Epic;
import tracker.model.tasks.Subtask;
import tracker.model.tasks.Task;
import tracker.services.FileBackedTaskManager;
import tracker.services.Managers;
import tracker.services.TaskManager;
import tracker.services.enums.TypeTask;
import tracker.services.exceptions.SetPropertyTaskException;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class EpicTest {
    private static final TaskManager taskManager = Managers.getDefault();

    @Test
    public void shouldBeSDEEquals0WithDeleteAllSubtasksAtAllEpics() {
        //Проверим, что при удалении всех подзадач, у всех эпиков параметры SDE = 0.

        //Порядок:
        //  - создаем новый менеджер использующий файловое хранилище
        //  - создаем задачи:
        //      - Task1
        //      - Task2
        //      - Epic1
        //      - Subtask11(Epic1)
        //      - Subtask12(Epic1)
        //      - Epic2
        //      - Subtask21(Epic2)

        TaskManager taskManager = Managers.getDefault(true);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy|HH.mm.ss");

        //Создаем две задачи, а также эпик с двумя подзадачами и эпик с одной подзадачей.
        //id = 1
        Task task1 = new Task("Задача 1", "Описание задачи 1.",
                LocalDateTime.parse("17.01.2025|14.15.13", formatter), 10);
        taskManager.addTask(task1);
        //id = 2
        Task task2 = new Task("Задача 2", "Описание задачи 2.",
                LocalDateTime.parse("15.01.2025|02.03.51", formatter), 187);
        taskManager.addTask(task2);

        //id = 3
        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1.");
        taskManager.addEpic(epic1);

        //id = 4
        Subtask subtask11 = new Subtask("Подзадача 11", "Описание подзадачи 11.", epic1,
                LocalDateTime.parse("14.01.2025|05.17.46", formatter), 15);
        taskManager.addSubtask(subtask11);
        //id = 5
        Subtask subtask12 = new Subtask("Подзадача 12", "Описание подзадачи 12.", epic1);
        taskManager.addSubtask(subtask12);

        //id = 6
        Epic epic2 = new Epic("Эпик 2", "Описание эпика 2.");
        taskManager.addEpic(epic2);

        //id = 7
        Subtask subtask21 = new Subtask("Подзадача 21", "Описание подзадачи 21.", epic2,
                LocalDateTime.parse("18.01.2025|09.25.16", formatter), 26);
        taskManager.addSubtask(subtask21);

        //Удаляем все подзадачи.
        taskManager.delAllSubtasks();

        //Проверяем корректность параметров SDE у всех эпиков: startTime == null, duration == 0, endTime == null.
        List<Epic> epics = taskManager.getEpics();
        List<Epic> epicsProblem = epics.stream()
                .filter((epic) -> {
                    if (epic.getStartTime().isPresent()) return true;
                    else if (epic.getDuration() != 0) return true;
                    else if (epic.getEndTime().isPresent()) return true;
                    else return false;
                })
                .toList();

        assertEquals(0, epicsProblem.size(), "При удалении всех подзадач, у эпиков параметры SDE не равны 0.");
    }

    @Test
    public void shouldBeSDECorrectWithDeleteSubtaskAtEpic() {
        //Проверим, что при удалении подзадачи у эпика, его параметры SDE будут корректны и не будут затронуты у остальных эпиков.

        //Порядок:
        //  - создаем новый менеджер использующий файловое хранилище
        //  - создаем задачи:
        //      - Task1
        //      - Task2
        //      - Epic1
        //      - Subtask11(Epic1)
        //      - Subtask12(Epic1)
        //      - Epic2
        //      - Subtask21(Epic2)

        TaskManager taskManager = Managers.getDefault(true);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy|HH.mm.ss");

        //Создаем две задачи, а также эпик с двумя подзадачами и эпик с одной подзадачей.
        //id = 1
        Task task1 = new Task("Задача 1", "Описание задачи 1.",
                LocalDateTime.parse("17.01.2025|14.15.13", formatter), 10);
        taskManager.addTask(task1);
        //id = 2
        Task task2 = new Task("Задача 2", "Описание задачи 2.",
                LocalDateTime.parse("15.01.2025|02.03.51", formatter), 187);
        taskManager.addTask(task2);

        //id = 3
        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1.");
        taskManager.addEpic(epic1);

        LocalDateTime startTimeEtalonEpic1 = LocalDateTime.parse("14.01.2025|05.17.46", formatter);
        long durationEtalonEpic1 = 140;
        LocalDateTime endTimeEtalonEpic1 = startTimeEtalonEpic1.plusMinutes(durationEtalonEpic1);

        //id = 4
        Subtask subtask11 = new Subtask("Подзадача 11", "Описание подзадачи 11.", epic1,
                LocalDateTime.parse("14.01.2025|05.17.46", formatter), 15);
        taskManager.addSubtask(subtask11);
        //id = 5
        Subtask subtask12 = new Subtask("Подзадача 12", "Описание подзадачи 12.", epic1,
                LocalDateTime.parse("15.01.2025|05.17.46", formatter), 125);
        taskManager.addSubtask(subtask12);

        //id = 6
        Epic epic2 = new Epic("Эпик 2", "Описание эпика 2.");
        taskManager.addEpic(epic2);

        LocalDateTime startTimeEtalonEpic2 = LocalDateTime.parse("18.01.2025|09.25.16", formatter);
        long durationEtalonEpic2 = 26;
        LocalDateTime endTimeEtalonEpic2 = startTimeEtalonEpic2.plusMinutes(durationEtalonEpic2);

        //id = 7
        Subtask subtask21 = new Subtask("Подзадача 21", "Описание подзадачи 21.", epic2,
                LocalDateTime.parse("18.01.2025|09.25.16", formatter), 26);
        taskManager.addSubtask(subtask21);

        //Проверяем изначальное значение параметров SDE у эпиков на корректность.
        //Эпик 1.
        epic1.getStartTime().ifPresentOrElse(localDateTime -> assertEquals(startTimeEtalonEpic1, localDateTime,
                        "В эпике не корректно рассчитано значение startTime после добавления подзадач."),
                () -> fail("При добавлении подзадач в эпик, с не пустыми параметрами SDE, в startTime получено пустое значение."));

        assertEquals(durationEtalonEpic1, epic1.getDuration(),
                "В эпике не корректное значение duration после добавления подзадач.");

        epic1.getEndTime().ifPresentOrElse(localDateTime -> assertEquals(endTimeEtalonEpic1, localDateTime,
                        "В эпике не корректно рассчитано значение endTime после добавления подзадач."),
                () -> fail("При добавлении подзадач в эпик, с не пустыми параметрами SDE, в endTime получено пустое значение."));

        //Эпик 2.
        epic2.getStartTime().ifPresentOrElse(localDateTime -> assertEquals(startTimeEtalonEpic2, localDateTime,
                        "В эпике не корректно рассчитано значение startTime после добавления подзадач."),
                () -> fail("При добавлении подзадач в эпик, с не пустыми параметрами SDE, в startTime получено пустое значение."));

        assertEquals(durationEtalonEpic2, epic2.getDuration(),
                "В эпике не корректное значение duration после добавления подзадач.");

        epic2.getEndTime().ifPresentOrElse(localDateTime -> assertEquals(endTimeEtalonEpic2, localDateTime,
                        "В эпике не корректно рассчитано значение endTime после добавления подзадач."),
                () -> fail("При добавлении подзадач в эпик, с не пустыми параметрами SDE, в endTime получено пустое значение."));


        //Удаляем подзадачу subtask11.
        taskManager.delSubtaskByID(subtask11.getId());

        //Проверяем значение параметров SDE у эпиков на корректность.
        //Эпик 1.
        LocalDateTime startTimeEtalonEpic1New = LocalDateTime.parse("15.01.2025|05.17.46", formatter);
        long durationEtalonEpic1New = 125;
        LocalDateTime endTimeEtalonEpic1New = startTimeEtalonEpic1New.plusMinutes(durationEtalonEpic1New);

        epic1.getStartTime().ifPresentOrElse(localDateTime -> assertEquals(startTimeEtalonEpic1New, localDateTime,
                        "В эпике не корректно рассчитано значение startTime после удаления подзадачи."),
                () -> fail("При удалении подзадачи в эпике, с не пустыми параметрами SDE, в startTime получено пустое значение."));

        assertEquals(durationEtalonEpic1New, epic1.getDuration(),
                "В эпике не корректное значение duration после удаления подзадачи.");

        epic1.getEndTime().ifPresentOrElse(localDateTime -> assertEquals(endTimeEtalonEpic1New, localDateTime,
                        "В эпике не корректно рассчитано значение endTime после удаления подзадачи."),
                () -> fail("При удаления подзадачи в эпике, с не пустыми параметрами SDE, в endTime получено пустое значение."));

        //Эпик 2.
        epic2.getStartTime().ifPresentOrElse(localDateTime -> assertEquals(startTimeEtalonEpic2, localDateTime,
                        "В эпике не корректно рассчитано значение startTime после добавления подзадач в другом эпике."),
                () -> fail("При добавлении подзадач в другом эпике, с не пустыми параметрами SDE, в startTime получено пустое значение."));

        assertEquals(durationEtalonEpic2, epic2.getDuration(),
                "В эпике не корректное значение duration после добавления подзадач в другом эпике.");

        epic2.getEndTime().ifPresentOrElse(localDateTime -> assertEquals(endTimeEtalonEpic2, localDateTime,
                        "В эпике не корректно рассчитано значение endTime после добавления подзадач в другом эпике."),
                () -> fail("При добавлении подзадач в другом эпике, с не пустыми параметрами SDE, в endTime получено пустое значение."));
    }

    @Test
    public void shouldBeSDECorrectWithDeleteEpic() {
        //Проверим, что при удалении эпика, параметры SDE будут корректны у оставшихся эпиков и подзадач.

        //Порядок:
        //  - создаем новый менеджер использующий файловое хранилище
        //  - создаем задачи:
        //      - Task1
        //      - Task2
        //      - Epic1
        //      - Subtask11(Epic1)
        //      - Subtask12(Epic1)
        //      - Epic2
        //      - Subtask21(Epic2)

        TaskManager taskManager = Managers.getDefault(true);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy|HH.mm.ss");

        //Создаем две задачи, а также эпик с двумя подзадачами и эпик с одной подзадачей.
        //id = 1
        Task task1 = new Task("Задача 1", "Описание задачи 1.",
                LocalDateTime.parse("17.01.2025|14.15.13", formatter), 10);
        taskManager.addTask(task1);
        //id = 2
        Task task2 = new Task("Задача 2", "Описание задачи 2.",
                LocalDateTime.parse("15.01.2025|02.03.51", formatter), 187);
        taskManager.addTask(task2);

        //id = 3
        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1.");
        taskManager.addEpic(epic1);

        LocalDateTime startTimeEtalonEpic1 = LocalDateTime.parse("14.01.2025|05.17.46", formatter);
        long durationEtalonEpic1 = 140;
        LocalDateTime endTimeEtalonEpic1 = startTimeEtalonEpic1.plusMinutes(durationEtalonEpic1);

        //id = 4
        Subtask subtask11 = new Subtask("Подзадача 11", "Описание подзадачи 11.", epic1,
                LocalDateTime.parse("14.01.2025|05.17.46", formatter), 15);
        taskManager.addSubtask(subtask11);
        //id = 5
        Subtask subtask12 = new Subtask("Подзадача 12", "Описание подзадачи 12.", epic1,
                LocalDateTime.parse("15.01.2025|05.17.46", formatter), 125);
        taskManager.addSubtask(subtask12);

        //id = 6
        Epic epic2 = new Epic("Эпик 2", "Описание эпика 2.");
        taskManager.addEpic(epic2);

        LocalDateTime startTimeEtalonEpic2 = LocalDateTime.parse("18.01.2025|09.25.16", formatter);
        long durationEtalonEpic2 = 26;
        LocalDateTime endTimeEtalonEpic2 = startTimeEtalonEpic2.plusMinutes(durationEtalonEpic2);

        //id = 7
        Subtask subtask21 = new Subtask("Подзадача 21", "Описание подзадачи 21.", epic2,
                LocalDateTime.parse("18.01.2025|09.25.16", formatter), 26);
        taskManager.addSubtask(subtask21);

        LocalDateTime startTimeEtalonSubtask21 = LocalDateTime.parse("18.01.2025|09.25.16", formatter);
        long durationEtalonSubtask21 = 26;
        LocalDateTime endTimeEtalonSubtask21 = startTimeEtalonSubtask21.plusMinutes(durationEtalonSubtask21);

        //Удаляем эпик 1.
        taskManager.delEpicByID(epic1.getId());

        //Проверяем значение параметров SDE у эпиков на корректность.
        //Эпик 2.
        epic2.getStartTime().ifPresentOrElse(localDateTime -> assertEquals(startTimeEtalonEpic2, localDateTime,
                        "В эпике не корректно рассчитано значение startTime после удаления другого эпика."),
                () -> fail("При удалении другого эпика, с не пустыми параметрами SDE, в startTime эпика получено пустое значение."));

        assertEquals(durationEtalonEpic2, epic2.getDuration(),
                "В эпике не корректное значение duration после удаления другого эпика.");

        epic2.getEndTime().ifPresentOrElse(localDateTime -> assertEquals(endTimeEtalonEpic2, localDateTime,
                        "В эпике не корректно рассчитано значение endTime после удаления другого эпика."),
                () -> fail("При удалении другого эпика, с не пустыми параметрами SDE, в endTime эпика получено пустое значение."));

        //Подзадача 21.
        subtask21.getStartTime().ifPresentOrElse(localDateTime -> assertEquals(startTimeEtalonSubtask21, localDateTime,
                        "В подзадаче не корректно рассчитано значение startTime после удаления другого эпика."),
                () -> fail("При удалении другого эпика, с не пустыми параметрами SDE, в startTime подзадачи получено пустое значение."));

        assertEquals(durationEtalonSubtask21, subtask21.getDuration(),
                "В подзадаче не корректное значение duration после удаления другого эпика.");

        subtask21.getEndTime().ifPresentOrElse(localDateTime -> assertEquals(endTimeEtalonSubtask21, localDateTime,
                        "В подзадаче не корректно рассчитано значение endTime после удаления другого эпика."),
                () -> fail("При удалении другого эпика, с не пустыми параметрами SDE, в endTime подзадачи получено пустое значение."));
    }
}