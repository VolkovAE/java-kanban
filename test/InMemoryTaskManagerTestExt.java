import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tracker.model.enums.Status;
import tracker.model.tasks.Epic;
import tracker.model.tasks.Subtask;
import tracker.model.tasks.Task;
import tracker.services.Managers;
import tracker.services.TaskManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InMemoryTaskManagerTestExt {
    //проверьте, что InMemoryTaskManager действительно добавляет задачи разного типа и может найти их по id;

    private static TaskManager taskManager;

    @BeforeAll
    public static void init() {
        taskManager = Managers.getDefault();
    }

    @Test
    public void shouldAddTaskAndGetByID() {
        //Добавляем задачу (task), получаем ее id и получаем задачу по этому id (taskOther).
        //Если объекты task и taskOther равны, то значит мы нашли добавленную задачу по ее id.

        Task task = new Task("Задача", "Наименование задачи.");
        taskManager.addTask(task);

        int idTask = task.getId();
        Task taskOther = taskManager.getTaskByID(idTask);

        assertEquals(task, taskOther, "Добавленная задача не найдена по id.");
    }

    @Test
    public void shouldAddSubtaskAndGetByID() {
        //Добавляем подзадачу (subtask), получаем ее id и получаем подзадачу по этому id (subtaskOther).
        //Если объекты subtask и subtaskOther равны, то значит мы нашли добавленную подзадачу по ее id.

        Epic epic = new Epic("Эпик", "Наименование эпика");
        taskManager.addEpic(epic);

        Subtask subtask = new Subtask("Задача", "Наименование задачи.", epic);
        taskManager.addSubtask(subtask);

        int idSubtask = subtask.getId();
        Subtask subtaskOther = taskManager.getSubtaskByID(idSubtask);

        assertEquals(subtask, subtaskOther, "Добавленная подзадача не найдена по id.");
    }

    @Test
    public void shouldAddEpicAndGetByID() {
        //Добавляем эпик (epic), получаем его id и получаем эпик по этому id (taskEpic).
        //Если объекты epic и epicOther равны, то значит мы нашли добавленный эпик по его id.

        Epic epic = new Epic("Эпик", "Наименование эпика");
        taskManager.addEpic(epic);

        int idEpic = epic.getId();
        Epic epicOther = taskManager.getEpicByID(idEpic);

        assertEquals(epic, epicOther, "Добавленный эпик не найден по id.");
    }

    public void shouldImmutableTaskWhenAddManager() {
        //создайте тест, в котором проверяется неизменность задачи (по всем полям) при добавлении задачи в менеджер

        String name = "Имя";
        String descr = "Наименование";

        Task task = new Task(name, descr);

        taskManager.addTask(task);

        Task taskOther = taskManager.getTaskByID(task.getId());


        //Проверяем не изменчивость полей задачи (имя и наименование) при добавлении задачи менеджером.
        assertTrue(name.equals(taskOther.getName()) && descr.equals(taskOther.getDescr()), "Задача изменилась после добавления менеджером.");
    }

    public void shouldImmutableTaskWhenGetManager() {
        //убедитесь, что задачи, добавляемые в HistoryManager, сохраняют предыдущую версию задачи и её данных.

        //В историю добавляется по условию задачи при получении задачи, метод getTaskByID.
        //Цель проверки сверить все поля задачи (имя, наименование, статус, id) после ее получения и до.

        //Подготовка.
        String name = "Имя";
        String descr = "Наименование";

        Task task = new Task(name, descr);

        taskManager.addTask(task);

        int id = task.getId();
        Status status = task.getStatus();

        //Исполнение.
        Task taskOther = taskManager.getTaskByID(id);   //задача была добавлена в историю

        //Проверка.
        assertTrue(name.equals(taskOther.getName()) && descr.equals(taskOther.getName()) && (id == taskOther.getId()) && (status == taskOther.getStatus()), "Задача изменилась после добавления в историю.");
    }

    //проверьте, что задачи с заданным id и сгенерированным id не конфликтуют внутри менеджера;
    //Исключено, т.к. id устанавливается задаче при ее добавлении менеджером.
}