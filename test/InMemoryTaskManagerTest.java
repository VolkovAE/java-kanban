import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tracker.model.tasks.Epic;
import tracker.model.tasks.Subtask;
import tracker.model.tasks.Task;
import tracker.services.HistoryManager;
import tracker.services.Managers;
import tracker.services.TaskManager;
import tracker.services.enums.TypeTask;
import tracker.services.exceptions.CrossTimeExecution;
import tracker.services.exceptions.SetPropertyTaskException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static tracker.model.enums.Status.NEW;
import static tracker.model.enums.Status.IN_PROGRESS;
import static tracker.model.enums.Status.DONE;

public class InMemoryTaskManagerTest {
    private static final TaskManager taskManager = Managers.getDefault();
    private static final HistoryManager historyManager = taskManager.getHistoryManager();

    @BeforeAll
    public static void init() {
    }

    @Test
    public void addTask() {
        String name = "Задача 1";
        String descr = "Описание задачи 1";

        Task task = new Task(name, descr);
        int id = taskManager.addTask(task);

        assertTrue(id > 0, "Задача не добавлена.");

        Task taskGet = taskManager.getTaskByID(id);
        assertNotNull(taskGet, "Задача не найдена.");

        assertEquals(task, taskGet, "Задачи не совпадают.");

        assertTrue(name.equals(taskGet.getName()) && descr.equals(taskGet.getDescr()),
                "При размещении задача изменилась (имя, наименование).");
    }

    @Test
    public void updateTask() {
        String name = "Задача 1";
        String descr = "Описание задачи 1";

        Task task = new Task(name, descr);
        int id = taskManager.addTask(task);

        assertTrue(taskManager.updateTask(task), "Обновление задачи не выполнено.");

        assertEquals(IN_PROGRESS, task.getStatus(), "Статус задачи не корректен после обновления.");

        Task taskGet = taskManager.getTaskByID(id);

        assertEquals(task, taskGet, "Задачи не совпадают после обновления.");

        assertTrue(name.equals(taskGet.getName()) && descr.equals(taskGet.getDescr()),
                "При обновлении задача изменилась (имя, наименование).");
    }

    @Test
    public void delTaskByID() {
        String name = "Задача 1";
        String descr = "Описание задачи 1";

        Task task = new Task(name, descr);
        int id = taskManager.addTask(task);

        Task taskDel = taskManager.delTaskByID(id);

        assertEquals(task, taskDel, "Не возвращена ссылка на удаляемую задачу.");

        List<Task> tasks = taskManager.getTasks();

        assertFalse(tasks.contains(task), "Задача не удалена.");

        List<Task> history = historyManager.getHistory();
        assertFalse(history.contains(task), "Задача не удалена из истории.");
    }

    @Test
    public void delAllTasks() {
        String name = "Задача 1";
        String descr = "Описание задачи 1";

        Task task = new Task(name, descr);
        taskManager.addTask(task);

        taskManager.delAllTasks();

        assertTrue(taskManager.getTasks().isEmpty(), "Задачи не удалены.");
    }

    @Test
    public void addSubtask() {
        String nameEpic = "Эпик 1";
        String descrEpic = "Описание эпика 1";

        Epic epic = new Epic(nameEpic, descrEpic);

        String nameSub = "Подзадача 1";
        String descrSub = "Описание подзадачи 1";

        Subtask subtask = new Subtask(nameSub, descrSub, epic);
        int id = taskManager.addSubtask(subtask);

        assertTrue(id > 0, "Подзадача не добавлена.");

        Subtask subtaskGet = taskManager.getSubtaskByID(id);
        assertNotNull(subtaskGet, "Подзадача не найдена.");

        assertEquals(subtask, subtaskGet, "Подзадачи не совпадают.");

        assertTrue(nameSub.equals(subtaskGet.getName()) &&
                        descrSub.equals(subtaskGet.getDescr()) &&
                        (subtask.getEpic() == subtaskGet.getEpic()),
                "При размещении подзадача изменилась (имя, наименование, эпик).");

        assertTrue(epic.getSubtasks().contains(subtaskGet), "Подзадача не добавлены в эпик.");
    }

    @Test
    public void updateSubtask() {
        String nameEpic = "Эпик 1";
        String descrEpic = "Описание эпика 1";

        Epic epic = new Epic(nameEpic, descrEpic);

        String nameSub = "Подзадача 1";
        String descrSub = "Описание подзадачи 1";

        Subtask subtask = new Subtask(nameSub, descrSub, epic);
        int id = taskManager.addSubtask(subtask);

        assertTrue(taskManager.updateSubtask(subtask), "Обновление подзадачи не выполнено.");

        assertEquals(IN_PROGRESS, subtask.getStatus(), "Статус подзадачи не корректен после обновления.");

        assertEquals(IN_PROGRESS, epic.getStatus(), "Статус эпика не корректен после обновления подзадачи.");

        assertTrue(taskManager.updateSubtask(subtask), "Обновление подзадачи не выполнено.");

        assertEquals(DONE, subtask.getStatus(), "Статус подзадачи не корректен после обновления.");

        assertEquals(DONE, epic.getStatus(), "Статус эпика не корректен после обновления подзадачи.");

        Subtask subtaskGet = taskManager.getSubtaskByID(id);

        assertEquals(subtask, subtaskGet, "Подзадачи не совпадают после обновления.");

        assertTrue(nameSub.equals(subtaskGet.getName()) &&
                        descrSub.equals(subtaskGet.getDescr()) &&
                        (subtask.getEpic() == subtaskGet.getEpic()),
                "При обновлении подзадача изменилась (имя, наименование, эпик).");
    }

    @Test
    public void delSubtaskByID() {
        String nameEpic = "Эпик 1";
        String descrEpic = "Описание эпика 1";

        Epic epic = new Epic(nameEpic, descrEpic);

        String nameSub = "Подзадача 1";
        String descrSub = "Описание подзадачи 1";

        Subtask subtask = new Subtask(nameSub, descrSub, epic);
        int id = taskManager.addSubtask(subtask);

        Subtask subtaskDel = taskManager.delSubtaskByID(id);

        assertEquals(subtask, subtaskDel, "Не возвращена ссылка на удаляемую подзадачу.");

        List<Subtask> subtasks = taskManager.getSubtasks();

        assertFalse(subtasks.contains(subtask), "Подзадача не удалена.");

        subtasks = epic.getSubtasks();

        assertFalse(subtasks.contains(subtask), "Подзадача не удалена из эпика.");

        List<Task> history = historyManager.getHistory();
        assertFalse(history.contains(subtask), "Подзадача не удалена из истории.");
    }

    @Test
    public void delAllSubtasks() {
        String nameEpic = "Эпик 1";
        String descrEpic = "Описание эпика 1";

        Epic epic = new Epic(nameEpic, descrEpic);

        String nameSub = "Подзадача 1";
        String descrSub = "Описание подзадачи 1";

        Subtask subtask = new Subtask(nameSub, descrSub, epic);
        taskManager.addSubtask(subtask);

        taskManager.delAllSubtasks();

        assertTrue(taskManager.getSubtasks().isEmpty(), "Подзадачи не удалены.");

        boolean isClear = true;
        for (Epic e : taskManager.getEpics()) {
            List<Subtask> subtasks = e.getSubtasks();

            if (!subtasks.isEmpty()) {
                isClear = false;

                break;
            }
        }
        assertTrue(isClear, "В эпиках подзадачи не удалены.");
    }

    @Test
    public void addEpic() {
        String nameEpic = "Эпик 1";
        String descrEpic = "Описание эпика 1";

        Epic epic = new Epic(nameEpic, descrEpic);

        int id = taskManager.addEpic(epic);

        assertTrue(id > 0, "Эпик не добавлен.");

        Epic epicGet = taskManager.getEpicByID(id);
        assertNotNull(epicGet, "Эпик не найден.");

        assertEquals(epic, epicGet, "Эпики не совпадают.");

        assertTrue(epicGet.getSubtasks().isEmpty(), "В эпике появились подзадачи.");

        assertTrue(nameEpic.equals(epicGet.getName()) && descrEpic.equals(epicGet.getDescr()),
                "При размещении эпик изменился (имя, наименование).");
    }

    @Test
    public void updateEpic() {
        String nameEpic = "Эпик 1";
        String descrEpic = "Описание эпика 1";

        Epic epic = new Epic(nameEpic, descrEpic);

        int id = taskManager.addEpic(epic);

        assertTrue(taskManager.updateEpic(epic), "Обновление эпика не выполнено.");

        assertEquals(NEW, epic.getStatus(), "Статус эпика не корректен после обновления.");

        Epic epicGet = taskManager.getEpicByID(id);

        assertEquals(epic, epicGet, "Эпики не совпадают после обновления.");

        assertTrue(nameEpic.equals(epicGet.getName()) && descrEpic.equals(epicGet.getDescr()),
                "При обновлении эпик изменился (имя, наименование).");
    }

    @Test
    public void delEpicByID() {
        String nameEpic = "Эпик 1";
        String descrEpic = "Описание эпика 1";

        Epic epic = new Epic(nameEpic, descrEpic);

        int id = taskManager.addEpic(epic);

        Epic epicDel = taskManager.delEpicByID(id);

        assertEquals(epic, epicDel, "Не возвращена ссылка на удаляемый эпик.");

        assertTrue(epicDel.getSubtasks().isEmpty(), "Список подзадач эпика не пустой.");

        List<Epic> epics = taskManager.getEpics();

        assertFalse(epics.contains(epic), "Эпик не удален.");

        boolean isDelSubtasks = true;
        for (Subtask subtask : taskManager.getSubtasks()) {
            if (subtask.getEpic().equals(epicDel)) {
                isDelSubtasks = false;

                break;
            }
        }
        assertTrue(isDelSubtasks, "Подзадачи эпика не удалены из списка подзадач.");

        List<Task> history = historyManager.getHistory();
        assertFalse(history.contains(epicDel), "Эпик не удален из истории.");

        boolean isDelHistory = true;
        for (Task task : history) {
            if (task instanceof Subtask) {
                if (((Subtask) task).getEpic().equals(epicDel)) {
                    isDelHistory = false;

                    break;
                }
            }
        }
        assertTrue(isDelHistory, "Подзадачи эпика не удалены из истории.");
    }

    @Test
    public void delAllEpics() {
        String nameEpic = "Эпик 1";
        String descrEpic = "Описание эпика 1";

        Epic epic = new Epic(nameEpic, descrEpic);

        taskManager.addEpic(epic);

        taskManager.delAllEpics();

        assertTrue(taskManager.getEpics().isEmpty(), "Эпики не удалены.");

        assertTrue(taskManager.getSubtasks().isEmpty(), "Подзадачи не удалены.");
    }

    @Test
    public void getSubtasksByEpic() {
        String nameEpic = "Эпик 1";
        String descrEpic = "Описание эпика 1";

        Epic epic = new Epic(nameEpic, descrEpic);
        taskManager.addEpic(epic);

        String nameSub = "Подзадача 1";
        String descrSub = "Описание подзадачи 1";

        Subtask subtask = new Subtask(nameSub, descrSub, epic);
        taskManager.addSubtask(subtask);

        List<Subtask> subtasks = new ArrayList<>();
        for (Subtask o : taskManager.getSubtasks()) {
            if (o.getEpic().equals(epic)) subtasks.add(o);
        }

        List<Subtask> subtasksEpic = taskManager.getSubtasksByEpic(epic);

        assertEquals(subtasks, subtasksEpic, "Список подзадач эпика не соответствует списку подзадач.");
    }

    @Test
    public void shouldBeEqualTasksWithEqualID() {
        //проверьте, что экземпляры класса Task равны друг другу, если равен их id;

        String name = "Задача 1";
        String descr = "Описание задачи 1";

        Task task = new Task(name, descr);
        int id = taskManager.addTask(task);

        Task task1 = taskManager.getTaskByID(id);
        Task task2 = taskManager.getTaskByID(id);

        assertEquals(task1, task2, "Задачи с одинаковыми идентификаторами не равны друг другу.");
    }

    @Test
    public void shouldBeEqualSubtasksWithEqualID() {
        //проверьте, что наследники класса Task равны друг другу, если равен их id;

        String nameEpic = "Эпик 1";
        String descrEpic = "Описание эпика 1";

        Epic epic = new Epic(nameEpic, descrEpic);

        String nameSub = "Подзадача 1";
        String descrSub = "Описание подзадачи 1";

        Subtask subtask = new Subtask(nameSub, descrSub, epic);
        int id = taskManager.addSubtask(subtask);

        Subtask subtask1 = taskManager.getSubtaskByID(id);
        Subtask subtask2 = taskManager.getSubtaskByID(id);

        assertEquals(subtask1, subtask2, "Подзадачи с одинаковыми идентификаторами не равны друг другу.");
    }

    @Test
    public void shouldBeEqualEpicsWithEqualID() {
        //проверьте, что наследники класса Task равны друг другу, если равен их id;

        String nameEpic = "Эпик 1";
        String descrEpic = "Описание эпика 1";

        Epic epic = new Epic(nameEpic, descrEpic);

        int id = taskManager.addEpic(epic);

        Epic epic1 = taskManager.getEpicByID(id);
        Epic epic2 = taskManager.getEpicByID(id);

        assertEquals(epic1, epic2, "Эпики с одинаковыми идентификаторами не равны друг другу.");
    }

    @Test
    public void checkPrioritizedTasks() {
        //Проверка приоритизации задач.

        //Порядок:
        //  - создаем новый менеджер задач не использующий файловое хранилище
        //  - создаем задачи:
        //      - Task1
        //      - Task2
        //      - Epic1
        //      - Subtask11(Epic1)
        //      - Subtask12(Epic1)
        //      - Epic2
        //      - Subtask21(Epic2)

        TaskManager taskManager = Managers.getDefault();

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

        //Проверяем приоритизированный список задач после составления (добавление task/subtask).
        List<Task> etalonAllTasks = List.of(subtask11, task2, task1, subtask21);

        List<Task> tasksSortedByStartTime = taskManager.getPrioritizedTasks();

        assertEquals(etalonAllTasks, tasksSortedByStartTime, "Список задач после составления приоритизирован не верно.");

        //Проверяем приоритизированный список задач после удаления task1.
        etalonAllTasks = List.of(subtask11, task2, subtask21);

        taskManager.delTaskByID(task1.getId());

        tasksSortedByStartTime = taskManager.getPrioritizedTasks();

        assertEquals(etalonAllTasks, tasksSortedByStartTime, "Список задач после удаления задачи приоритизирован не верно.");

        //Проверяем приоритизированный список задач после удаления subtask11.
        etalonAllTasks = List.of(task2, subtask21);

        taskManager.delSubtaskByID(subtask11.getId());

        tasksSortedByStartTime = taskManager.getPrioritizedTasks();

        assertEquals(etalonAllTasks, tasksSortedByStartTime, "Список задач после удаления подзадачи приоритизирован не верно.");

        //Проверяем приоритизированный список задач после добавления task1 и subtask11.
        etalonAllTasks = List.of(subtask11, task2, task1, subtask21);

        taskManager.addTask(task1);
        taskManager.addSubtask(subtask11);

        tasksSortedByStartTime = taskManager.getPrioritizedTasks();

        assertEquals(etalonAllTasks, tasksSortedByStartTime, "Список задач после добавления задачи и подзадачи приоритизирован не верно.");

        //Проверяем приоритизированный список задач после удаления всех задач.
        etalonAllTasks = List.of(subtask11, subtask21);

        taskManager.delAllTasks();

        tasksSortedByStartTime = taskManager.getPrioritizedTasks();

        assertEquals(etalonAllTasks, tasksSortedByStartTime, "Список задач после удаления всех задач приоритизирован не верно.");

        //Проверяем приоритизированный список задач после удаления всех подзадач.
        etalonAllTasks = List.of(task2, task1);

        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.delAllSubtasks();

        tasksSortedByStartTime = taskManager.getPrioritizedTasks();

        assertEquals(etalonAllTasks, tasksSortedByStartTime, "Список задач после удаления всех подзадач приоритизирован не верно.");

        //Проверяем приоритизированный список задач после изменения startTime в подзадаче.
        etalonAllTasks = List.of(subtask21, subtask11, task2, task1);

        taskManager.addSubtask(subtask11);
        taskManager.addSubtask(subtask21);

        LocalDateTime newStartTime = LocalDateTime.of(2_025, 1, 1, 0, 0, 0);
        subtask21.setStartTime(newStartTime);

        tasksSortedByStartTime = taskManager.getPrioritizedTasks();

        assertEquals(etalonAllTasks, tasksSortedByStartTime, "Список задач после изменения начала работ в подзадаче приоритизирован не верно.");

        //Проверяем приоритизированный список задач после изменения startTime в задаче.
        etalonAllTasks = List.of(subtask21, task1, subtask11, task2);

        newStartTime = LocalDateTime.of(2_025, 1, 10, 0, 0, 0);
        task1.setStartTime(newStartTime);

        tasksSortedByStartTime = taskManager.getPrioritizedTasks();

        assertEquals(etalonAllTasks, tasksSortedByStartTime, "Список задач после изменения начала работ в задаче приоритизирован не верно.");

        //Проверяем приоритизированный список задач после изменения startTime в задаче (в null).
        etalonAllTasks = List.of(subtask21, subtask11, task2);

        newStartTime = null;
        task1.setStartTime(newStartTime);

        tasksSortedByStartTime = taskManager.getPrioritizedTasks();

        assertEquals(etalonAllTasks, tasksSortedByStartTime, "Список задач после изменения начала работ (в null) в задаче приоритизирован не верно.");

        //Проверяем приоритизированный список задач после изменения startTime в задаче (из null).
        etalonAllTasks = List.of(subtask21, task1, subtask11, task2);

        newStartTime = LocalDateTime.of(2_025, 1, 10, 0, 0, 0);
        task1.setStartTime(newStartTime);

        tasksSortedByStartTime = taskManager.getPrioritizedTasks();

        assertEquals(etalonAllTasks, tasksSortedByStartTime, "Список задач после изменения начала работ (из null) в задаче приоритизирован не верно.");

        //Проверяем приоритизированный список задач после удаления задачи и изменения у нее startTime (то что больше ее не слушаем).
        etalonAllTasks = List.of(subtask21, subtask11, task2);

        taskManager.delTaskByID(task1.getId());

        newStartTime = LocalDateTime.of(2_025, 1, 17, 0, 0, 0);
        task1.setStartTime(newStartTime);

        tasksSortedByStartTime = taskManager.getPrioritizedTasks();

        assertEquals(etalonAllTasks, tasksSortedByStartTime, "Список задач после удаления задачи и изменения у нее начала работ приоритизирован не верно.");
    }

    @Test
    public void checkCrossTimeExecution() {
        //Проверка функционала по контролю пересечения времени выполнения задач.

        //Порядок:
        //  - создаем новый менеджер задач не использующий файловое хранилище
        //  - создаем задачи:
        //      - Task1
        //      - Task2
        //      - Epic1
        //      - Subtask11(Epic1)
        //      - Subtask12(Epic1)
        //      - Epic2
        //      - Subtask21(Epic2)

        TaskManager taskManager = Managers.getDefault();

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

        //Обертка для использования в лямбде.
        interface Wrapper {
        }

        var wrapper = new Wrapper() {
            Task task;
            LocalDateTime startTime;
            long durationMinutes;
        };
        //AtomicReference<Task> sneaky = new AtomicReference<>(taskTest);

        //1. Добавляем новую не пересекающуюся задачу и проверяем успех добавления.
        LocalDateTime startTimeTest = LocalDateTime.parse("19.01.2025|09.25.16", formatter);
        long durationTest = 15;
        wrapper.task = new Task("Задача Тест", "Описание задачи Тест", startTimeTest, durationTest);

        assertDoesNotThrow(() -> {
            taskManager.addTask(wrapper.task);
        }, "При добавлении не пересекающейся по времени выполнения задачи возникла ошибка.");

        taskManager.delTaskByID(wrapper.task.getId());

        //2. Добавляем новую пересекающуюся задачу и проверяем подкидывание исключения CrossTimeExecution.
        startTimeTest = LocalDateTime.parse("18.01.2025|09.40.16", formatter);
        durationTest = 25;
        wrapper.task = new Task("Задача Тест", "Описание задачи Тест", startTimeTest, durationTest);

        CrossTimeExecution execution = assertThrows(CrossTimeExecution.class, () -> {
                    taskManager.addTask(wrapper.task);
                },
                "Не выброшено исключение при добавлении задачи пересекающейся по времени выполнения.");

        taskManager.delTaskByID(wrapper.task.getId());

        //3. Добавляем новую не пересекающуюся подзадачу и проверяем успех добавления.
        startTimeTest = LocalDateTime.parse("19.01.2025|09.25.16", formatter);
        durationTest = 15;
        wrapper.task = new Subtask("Подзадача Тест", "Описание подзадачи Тест", epic1, startTimeTest, durationTest);

        assertDoesNotThrow(() -> {
            taskManager.addSubtask((Subtask) wrapper.task);
        }, "При добавлении не пересекающейся по времени выполнения подзадачи возникла ошибка.");

        taskManager.delSubtaskByID(wrapper.task.getId());

        //4. Добавляем новую пересекающуюся подзадачу и проверяем подкидывание исключения CrossTimeExecution.
        startTimeTest = LocalDateTime.parse("18.01.2025|09.40.16", formatter);
        durationTest = 25;
        wrapper.task = new Subtask("Задача Тест", "Описание задачи Тест", epic1, startTimeTest, durationTest);

        execution = assertThrows(CrossTimeExecution.class, () -> {
                    taskManager.addSubtask((Subtask) wrapper.task);
                },
                "Не выброшено исключение при добавлении подзадачи пересекающейся по времени выполнения.");

        taskManager.delSubtaskByID(wrapper.task.getId());

        //5. Изменяем startTime у задачи, не пересекаемое, и проверяем успех добавления.
        wrapper.startTime = LocalDateTime.parse("13.01.2025|09.40.16", formatter);
        wrapper.task = task2;

        assertDoesNotThrow(() -> {
            Optional<LocalDateTime> oldValue = wrapper.task.getStartTime();
            wrapper.task.setStartTime(wrapper.startTime);
            oldValue.ifPresent(localDateTime -> wrapper.task.setStartTime(localDateTime));
        }, "При изменении значения поля startTime у задачи, не пересекаемое, возникла ошибка.");

        //6. Изменяем startTime у подзадачи, не пересекаемое, и проверяем успех добавления.
        wrapper.startTime = LocalDateTime.parse("13.01.2025|09.40.16", formatter);
        wrapper.task = subtask21;

        assertDoesNotThrow(() -> {
            Optional<LocalDateTime> oldValue = wrapper.task.getStartTime();
            wrapper.task.setStartTime(wrapper.startTime);
            oldValue.ifPresent(localDateTime -> wrapper.task.setStartTime(localDateTime));
        }, "При изменении значения поля startTime у подзадачи, не пересекаемое, возникла ошибка.");

        //7. Изменяем startTime у задачи, пересекаемое, и проверяем подкидывание исключения CrossTimeExecution.
        wrapper.startTime = LocalDateTime.parse("18.01.2025|09.40.16", formatter);  //пересечение subtask21
        wrapper.task = task1;

        Optional<LocalDateTime> oldValueST = wrapper.task.getStartTime();
        execution = assertThrows(CrossTimeExecution.class, () -> {
                    wrapper.task.setStartTime(wrapper.startTime);
                },
                "Не выброшено исключение при изменении значения поля startTime у задачи на пересекаемое.");
        oldValueST.ifPresent(localDateTime -> wrapper.task.setStartTime(localDateTime));

        //8. Изменяем startTime у подзадачи, пересекаемое, и проверяем подкидывание исключения CrossTimeExecution.
        wrapper.startTime = LocalDateTime.parse("17.01.2025|14.20.16", formatter);  //пересечение task1
        wrapper.task = subtask21;

        oldValueST = wrapper.task.getStartTime();
        execution = assertThrows(CrossTimeExecution.class, () -> {
                    wrapper.task.setStartTime(wrapper.startTime);
                },
                "Не выброшено исключение при изменении значения поля startTime у подзадачи на пересекаемое.");
        oldValueST.ifPresent(localDateTime -> wrapper.task.setStartTime(localDateTime));

        //9. Изменяем duration у задачи, не пересекаемое, и проверяем успех добавления.
        wrapper.durationMinutes = 1080;
        wrapper.task = task1;

        assertDoesNotThrow(() -> {
            long oldValueDuration = wrapper.task.getDuration();
            wrapper.task.setDuration(wrapper.durationMinutes);
            wrapper.task.setDuration(oldValueDuration);
        }, "При изменении значения поля duration у задачи, не пересекаемое, возникла ошибка.");

        //10. Изменяем duration у подзадачи, не пересекаемое, и проверяем успех добавления.
        wrapper.durationMinutes = 720;
        wrapper.task = subtask11;

        assertDoesNotThrow(() -> {
            long oldValueDuration = wrapper.task.getDuration();
            wrapper.task.setDuration(wrapper.durationMinutes);
            wrapper.task.setDuration(oldValueDuration);
        }, "При изменении значения поля duration у подзадачи, не пересекаемое, возникла ошибка.");

        //11. Изменяем duration у задачи, пересекаемое, и проверяем подкидывание исключения CrossTimeExecution.
        wrapper.durationMinutes = 1155; //пересечение subtask21
        wrapper.task = task1;

        long oldValueDuration = wrapper.task.getDuration();
        execution = assertThrows(CrossTimeExecution.class, () -> {
                    wrapper.task.setDuration(wrapper.durationMinutes);
                },
                "Не выброшено исключение при изменении значения поля duration у задачи на пересекаемое.");
        wrapper.task.setDuration(oldValueDuration);

        //12. Изменяем duration у подзадачи, пересекаемое, и проверяем подкидывание исключения CrossTimeExecution.
        wrapper.durationMinutes = 1265;  //пересечение task2
        wrapper.task = subtask11;

        oldValueDuration = wrapper.task.getDuration();
        execution = assertThrows(CrossTimeExecution.class, () -> {
                    wrapper.task.setDuration(wrapper.durationMinutes);
                },
                "Не выброшено исключение при изменении значения поля duration у подзадачи на пересекаемое.");
        wrapper.task.setDuration(oldValueDuration);
    }
}