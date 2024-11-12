import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tracker.model.enums.Status;
import tracker.model.tasks.Epic;
import tracker.model.tasks.Subtask;
import tracker.model.tasks.Task;
import tracker.services.Managers;
import tracker.services.TaskManager;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InMemoryTaskManager {

    private static TaskManager taskManager;

    @BeforeAll
    public static void init() {
        taskManager = Managers.getDefault();
    }

    @Test
    public void testWorkingTask() {

        ArrayList<Task> tasksOld = taskManager.getTasks();

        Task task1 = new Task("Задача 1", "Описание задачи 1.");    //id = 1
        taskManager.addTask(task1);

        int id1 = task1.getId();

        Task taskOther1 = taskManager.getTaskByID(id1);

        ArrayList<Task> tasks = taskManager.getTasks();


        assertNotNull(taskOther1, "Задача не найдена.");
        assertEquals(task1, taskOther1, "Задачи не совпадают.");


        assertEquals(tasksOld.size() + 1, tasks.size(), "Неверное количество задач.");
        assertEquals(task1, tasks.get(tasks.size() - 1), "Задачи не совпадают.");

        taskManager.updateTask(task1);
        assertTrue(task1.getStatus() == Status.IN_PROGRESS, "Задача не верно изменяет статус при обновлении.");

        taskManager.delTaskByID(id1);
        tasks = taskManager.getTasks();
        assertEquals(tasksOld.size(), tasks.size(), "Задачи не совпадают.");

        taskManager.delAllTasks();
        tasks = taskManager.getTasks();
        assertEquals(0, tasks.size(), "Задачи не совпадают.");

//        Task task2 = new Task("Задача 2", "Описание задачи 2.");    //id = 2
//        taskManager.addTask(task2);
//
//        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1.");   //id = 3
//        taskManager.addEpic(epic1);
//
//        Subtask subtask11 = new Subtask("Подзадача 11", "Описание подзадачи 11.", epic1);   //id = 4
//        taskManager.addSubtask(subtask11);
//        Subtask subtask12 = new Subtask("Подзадача 12", "Описание подзадачи 12.", epic1);   //id = 5
//        taskManager.addSubtask(subtask12);
//
//        Epic epic2 = new Epic("Эпик 2", "Описание эпика 2.");   //id = 6
//        taskManager.addEpic(epic2);
//
//        Subtask subtask21 = new Subtask("Подзадача 21", "Описание подзадачи 21.", epic2);   //id = 7
//        taskManager.addSubtask(subtask21);


    }

}