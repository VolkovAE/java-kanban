import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tracker.model.tasks.Epic;
import tracker.model.tasks.Subtask;
import tracker.model.tasks.Task;
import tracker.services.HistoryManager;
import tracker.services.Managers;
import tracker.services.TaskManager;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestModelExt {

    private static final TaskManager taskManager = Managers.getDefault();  //получаем объект одного из менеджеров
    private static final HistoryManager historyManager = Managers.getDefaultHistory(); //получаем объект одного из менеджеров

    @BeforeAll
    static void init() {
        //Подготавливаем данные для тестирования (взял метод из предыдущего задания).
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
    }

    @Test
    public void shouldBeEqualTasksWithEqualID() {
        //проверьте, что экземпляры класса Task равны друг другу, если равен их id;

        Task task1 = taskManager.getTaskByID(1);
        Task task2 = taskManager.getTaskByID(1);

        assertEquals(task1, task2, "Задачи с одинаковыми идентификаторами не равны друг другу.");
    }

    @Test
    public void shouldBeEqualSubtasksWithEqualID() {
        //проверьте, что наследники класса Task равны друг другу, если равен их id;

        Subtask subtask1 = taskManager.getSubtaskByID(4);
        Subtask subtask2 = taskManager.getSubtaskByID(4);

        assertEquals(subtask1, subtask2, "Подзадачи с одинаковыми идентификаторами не равны друг другу.");
    }

    @Test
    public void shouldBeEqualEpicsWithEqualID() {
        //проверьте, что наследники класса Task равны друг другу, если равен их id;

        Epic epic1 = taskManager.getEpicByID(3);
        Epic epic2 = taskManager.getEpicByID(3);

        assertEquals(epic1, epic2, "Эпики с одинаковыми идентификаторами не равны друг другу.");
    }

    //проверьте, что объект Epic нельзя добавить в самого себя в виде подзадачи;
    //Исключено, т.к. сначала создается эпик, потом создается подзадача с указанием эпика в конструкторе с параметрами.
    //Ни как по другому добавить подзадачу в эпик нельзя.
    //Приведение типов, например Epic к Task (как родительскому классу), а потом явно к Subtask вызовет ошибку.

    //проверьте, что объект Subtask нельзя сделать своим же эпиком;
    //Исключено. В конструкторе Subtask указываю предварительно созданный эпик.
    //Ни как по другому эпик у подзадачи не устанавливается.
    //Приведение типов вызовет синтаксис ошибку.
}