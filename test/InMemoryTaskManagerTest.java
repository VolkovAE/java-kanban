import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tracker.model.tasks.Epic;
import tracker.model.tasks.Subtask;
import tracker.model.tasks.Task;
import tracker.services.HistoryManager;
import tracker.services.Managers;
import tracker.services.TaskManager;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static tracker.model.enums.Status.*;

public class InMemoryTaskManagerTest {
    private static final TaskManager taskManager = Managers.getDefault();
    private static final HistoryManager historyManager = Managers.getDefaultHistory();

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

        isDelSubtasks = true;
        for (Task task : history) {
            if (task instanceof Subtask) {
                if (((Subtask) task).getEpic().equals(epicDel)) {
                    isDelSubtasks = false;

                    break;
                }
            }
        }
        assertTrue(isDelSubtasks, "Подзадачи эпика не удалены из истории.");
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
}