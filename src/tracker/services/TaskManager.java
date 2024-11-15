package tracker.services;

import tracker.model.tasks.Epic;
import tracker.model.tasks.Subtask;
import tracker.model.tasks.Task;

import java.util.List;

public interface TaskManager {
    /* Методы для каждого из типа задач(Задача/Эпик/Подзадача): */

    //region a. Получение списка всех задач.
    List<Task> getTasks();

    List<Subtask> getSubtasks();

    List<Epic> getEpics();
    //endregion

    //region b. Удаление всех задач.
    void delAllTasks();

    void delAllSubtasks();

    void delAllEpics();
    //endregion

    //region c. Получение по идентификатору.
    Task getTaskByID(int id);

    Subtask getSubtaskByID(int id);

    Epic getEpicByID(int id);
    //endregion

    //region d. Создание. Сам объект должен передаваться в качестве параметра.
    int addTask(Task task);

    int addSubtask(Subtask subtask);

    int addEpic(Epic epic);
    //endregion

    //region e. Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра.
    boolean updateTask(Task task);

    boolean updateSubtask(Subtask subtask);

    boolean updateEpic(Epic epic);
    //endregion

    //region f. Удаление по идентификатору.
    Task delTaskByID(int id);

    Subtask delSubtaskByID(int id);

    Epic delEpicByID(int id);
    //endregion

    //region Дополнительные методы.

    /// a. Получение списка всех подзадач определённого эпика.
    List<Subtask> getSubtasksByEpic(Epic epic);

    HistoryManager getHistoryManager();
    //endregion
}