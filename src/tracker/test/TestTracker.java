package tracker.test;

import tracker.model.tasks.Epic;
import tracker.model.tasks.Subtask;
import tracker.model.tasks.Task;
import tracker.services.HistoryManager;
import tracker.services.Managers;
import tracker.services.TaskManager;

import java.util.Scanner;

public class TestTracker {
    private final TaskManager taskManager = Managers.getDefault();  //получаем объект одного из менеджеров
    private final HistoryManager historyManager = taskManager.getHistoryManager();

    private final Scanner scanner = new Scanner(System.in);

    public void execTest() {

        //Распечатайте списки эпиков, задач и подзадач через System.out.println(..).
        execTest001();  //создаем задачи для выполнения тестов

        printAllTasks();    //добавил функцию печати из ТЗ для отладки

        //Измените статусы созданных объектов, распечатайте их.
        // Проверьте, что статус задачи и подзадачи сохранился, а статус эпика рассчитался по статусам подзадач.
        execTest002();

        execTest003();

        //И, наконец, попробуйте удалить одну из задач и один из эпиков.
        execTest004();

        printAllTasks();
    }

    private void execTest001() {
        //Создайте две задачи, а также эпик с двумя подзадачами и эпик с одной подзадачей.
        Task task1 = new Task("Задача 1", "Описание задачи 1.");    //id = 1
        taskManager.addTask(task1);
        Task task2 = new Task("Задача 2", "Описание задачи 2.");    //id = 2
        taskManager.addTask(task2);

        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1.");   //id = 3
        taskManager.addEpic(epic1);

        Subtask subtask11 = new Subtask("Подзадача 11", "Описание подзадачи 11.", epic1);   //id = 4
        taskManager.addSubtask(subtask11);
        Subtask subtask12 = new Subtask("Подзадача 12", "Описание подзадачи 12.", epic1);   //id = 5
        taskManager.addSubtask(subtask12);

        Epic epic2 = new Epic("Эпик 2", "Описание эпика 2.");   //id = 6
        taskManager.addEpic(epic2);

        Subtask subtask21 = new Subtask("Подзадача 21", "Описание подзадачи 21.", epic2);   //id = 7
        taskManager.addSubtask(subtask21);

        print("001. Созданы задачи.");
    }

    private void execTest002() {
        taskManager.updateTask(taskManager.getTaskByID(1));
        taskManager.updateTask(taskManager.getTaskByID(2));
        taskManager.updateTask(taskManager.getTaskByID(1));
        taskManager.updateSubtask(taskManager.getSubtaskByID(4));

        print("002. Изменения: task1 = done, task2 = in_progress, subtask11|epic1 = in_progress, остальное без изменений.");
    }

    private void execTest003() {
        taskManager.updateSubtask(taskManager.getSubtaskByID(7));
        print("003.1 subtask21|epic2 = in_progress, остальное без изменений");

        taskManager.updateSubtask(taskManager.getSubtaskByID(7));
        print("003.2 subtask21|epic2 = done, остальное без изменений");

        taskManager.updateSubtask(taskManager.getSubtaskByID(4));
        print("003.3 subtask11 = done, остальное без изменений");

        taskManager.updateSubtask(taskManager.getSubtaskByID(4));
        print("003.4 без изменений");

        taskManager.updateSubtask(taskManager.getSubtaskByID(5));
        print("003.5 subtask12 = in_progress, остальное без изменений");

        taskManager.updateSubtask(taskManager.getSubtaskByID(5));
        print("003.6 subtask12|epic2 = done, остальное без изменений");

        taskManager.updateTask(taskManager.getTaskByID(2));
        print("003.7 Все задачи/подзадачи/эпики закрыты.");
    }

    private void execTest004() {
        taskManager.delSubtaskByID(4);
        print("004.1 subtask11 - удалена, остальное без изменений");

        taskManager.delTaskByID(2);
        print("004.2 task2 - удалена, остальное без изменений");

        taskManager.delEpicByID(3);
        print("004.3 subtask12|epic1 - удалены, остальное без изменений");
    }

    private void print(String descrTest) {
        System.out.println("Тест - " + descrTest);

        System.out.println("    Список эпиков:");
        for (Epic epic : taskManager.getEpics()) {
            System.out.println("        " + epic);
        }
        System.out.println('\n');

        System.out.println("    Список задач:");
        for (Task task : taskManager.getTasks()) {
            System.out.println("        " + task);
        }
        System.out.println('\n');

        System.out.println("    Список подзадач:");
        for (Subtask subtask : taskManager.getSubtasks()) {
            System.out.println("        " + subtask);
        }
        System.out.println('\n');
        System.out.println("=".repeat(15));

        System.out.print("Next...");
        scanner.next();
    }

    private void printAllTasks() {
        System.out.println("Задачи:");
        for (Task task : taskManager.getTasks()) {
            System.out.println(task);
        }

        System.out.println("Эпики:");
        for (Epic epic : taskManager.getEpics()) {
            System.out.println(epic);

            for (Subtask task : taskManager.getSubtasksByEpic(epic)) {
                System.out.println("--> " + task);
            }
        }
        System.out.println("Подзадачи:");
        for (Subtask subtask : taskManager.getSubtasks()) {
            System.out.println(subtask);
        }

        System.out.println("История:");
        for (Task task : historyManager.getHistory()) {
            System.out.println(task);
        }

        System.out.print("Next...");
        scanner.next();
    }
}