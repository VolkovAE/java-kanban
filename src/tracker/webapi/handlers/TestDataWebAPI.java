package tracker.webapi.handlers;

import tracker.model.tasks.Epic;
import tracker.model.tasks.Subtask;
import tracker.model.tasks.Task;
import tracker.services.TaskManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TestDataWebAPI {
    public static void createTask(TaskManager taskManager) {
        //Создаем три задачи, а также эпик с двумя подзадачами и эпик с одной подзадачей.
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
        Task task3 = new Task("Задача 3", "Описание задачи 3.");
        taskManager.addTask(task3);

        //id = 4
        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1.");
        taskManager.addEpic(epic1);

        //id = 5
        Subtask subtask11 = new Subtask("Подзадача 11", "Описание подзадачи 11.", epic1,
                LocalDateTime.parse("14.01.2025|05.17.46", formatter), 15);
        taskManager.addSubtask(subtask11);

        //id = 6
        Subtask subtask12 = new Subtask("Подзадача 12", "Описание подзадачи 12.", epic1);
        taskManager.addSubtask(subtask12);

        //id = 7
        Epic epic2 = new Epic("Эпик 2", "Описание эпика 2.");
        taskManager.addEpic(epic2);

        //id = 8
        Subtask subtask21 = new Subtask("Подзадача 21", "Описание подзадачи 21.", epic2,
                LocalDateTime.parse("18.01.2025|09.25.16", formatter), 26);
        taskManager.addSubtask(subtask21);
    }
}