import org.junit.jupiter.api.Test;
import tracker.model.tasks.Epic;
import tracker.model.tasks.Subtask;
import tracker.model.tasks.Task;
import tracker.services.HistoryManager;
import tracker.services.Managers;
import tracker.services.TaskManager;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InMemoryHistoryManagerTest {
    private static final TaskManager taskManager = Managers.getDefault();
    private static final HistoryManager historyManager = taskManager.getHistoryManager();

    @Test
    public void getHistory() {
        //Подготовка.
        Epic epic1 = new Epic("1", "11");
        Epic epic2 = new Epic("2", "22");
        Epic epic3 = new Epic("3", "33");

        Subtask subtask1 = new Subtask("1", "11", epic1);
        Subtask subtask2 = new Subtask("2", "22", epic2);
        Subtask subtask3 = new Subtask("3", "33", epic3);

        Task task1 = new Task("1", "11");
        Task task2 = new Task("2", "22");
        Task task3 = new Task("3", "33");

        int id_epic1 = taskManager.addEpic(epic1);
        int id_epic2 = taskManager.addEpic(epic2);
        int id_epic3 = taskManager.addEpic(epic3);

        int id_subtask1 = taskManager.addSubtask(subtask1);
        int id_subtask2 = taskManager.addSubtask(subtask2);
        int id_subtask3 = taskManager.addSubtask(subtask3);

        int id_task1 = taskManager.addTask(task1);
        int id_task2 = taskManager.addTask(task2);
        int id_task3 = taskManager.addTask(task3);

        List<Task> test = new ArrayList<>();
        test.add(task3);
        test.add(epic1);
        test.add(epic2);
        test.add(epic3);
        test.add(subtask1);
        test.add(subtask2);
        test.add(subtask3);
        test.add(task1);
        test.add(task2);
        test.add(task3);

        List<Task> testDel = new ArrayList<>();
        testDel.add(task3);
        testDel.add(epic1);
        testDel.add(epic3);
        testDel.add(subtask3);
        testDel.add(task2);
        testDel.add(task3);

        //Исполнение.
        taskManager.getEpicByID(id_epic1);
        taskManager.getEpicByID(id_epic2);
        taskManager.getEpicByID(id_epic3);

        taskManager.getSubtaskByID(id_subtask1);
        taskManager.getSubtaskByID(id_subtask2);
        taskManager.getSubtaskByID(id_subtask3);

        taskManager.getTaskByID(id_task1);
        taskManager.getTaskByID(id_task2);
        taskManager.getTaskByID(id_task3);

        taskManager.getEpicByID(id_epic1);
        taskManager.getEpicByID(id_epic2);
        taskManager.getEpicByID(id_epic3);

        taskManager.getSubtaskByID(id_subtask1);
        taskManager.getSubtaskByID(id_subtask2);
        taskManager.getSubtaskByID(id_subtask3);

        taskManager.getTaskByID(id_task1);
        taskManager.getTaskByID(id_task2);
        taskManager.getTaskByID(id_task3);

        //Проверка.
        List<Task> tasks = historyManager.getHistory();

        assertEquals(HistoryManager.MAX_SIZE_HISTORY, tasks.size(),
                "Количество элементов в истории больше " + HistoryManager.MAX_SIZE_HISTORY);

        assertEquals(test, tasks, "История прочтения задач не верна.");

        taskManager.delTaskByID(task1.getId());
        taskManager.delSubtaskByID(subtask1.getId());
        taskManager.delEpicByID(epic2.getId());

        assertEquals(testDel, historyManager.getHistory(), "Задачи не удалены из истории.");
    }
}