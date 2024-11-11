package tracker.services;

import tracker.model.tasks.Epic;
import tracker.model.tasks.Subtask;
import tracker.model.tasks.Task;

import java.util.ArrayList;

public interface TaskManager {
    /* Методы для каждого из типа задач(Задача/Эпик/Подзадача): */

    //region a. Получение списка всех задач.
    ArrayList<Task> getTasks();

    ArrayList<Subtask> getSubtasks();

    ArrayList<Epic> getEpics();
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
    void addTask(Task task);

    void addSubtask(Subtask subtask);

    void addEpic(Epic epic);
    //endregion

    //region e. Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра.
    void updateTask(Task task);

    void updateSubtask(Subtask subtask);

    void updateEpic(Epic epic);
    //endregion

    //region f. Удаление по идентификатору.
    void delTaskByID(int id);

    void delSubtaskByID(int id);

    void delEpicByID(int id);
    //endregion

    //region Дополнительные методы.

    /// a. Получение списка всех подзадач определённого эпика.
    ArrayList<Subtask> getSubtasksByEpic(Epic epic);
    //endregion
}