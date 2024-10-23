package tracker.services;

import tracker.model.enums.STATUS;
import tracker.model.tasks.Epic;
import tracker.model.tasks.Subtask;
import tracker.model.tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {

    private static final HashMap<Integer, Task> tableTasks = new HashMap<>();
    private static final HashMap<Integer, Subtask> tableSubtasks = new HashMap<>();
    private static final HashMap<Integer, Epic> tableEpics = new HashMap<>();

    /* Методы для каждого из типа задач(Задача/Эпик/Подзадача): */

    //region a. Получение списка всех задач.

    /// Список задач.
    public static ArrayList<Task> getTasks() {
        return new ArrayList<>(tableTasks.values());
    }

    /// Список подзадач.
    public static ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(tableSubtasks.values());
    }

    /// Список эпиков.
    public static ArrayList<Epic> getEpics() {
        return new ArrayList<>(tableEpics.values());
    }
    //endregion

    //region b. Удаление всех задач.

    /// Задачи.
    public static void delAllTasks() {
        tableTasks.clear();
    }

    /// Подзадачи
    public static void delAllSubtasks() {
        tableSubtasks.clear();
    }

    /// Эпики.
    public static void delAllEpics() {
        tableSubtasks.clear();
        tableEpics.clear();
    }
    //endregion

    //region c. Получение по идентификатору.

    /// Задачи.
    public static Task getTaskByID(int id) {
        if (!tableTasks.containsKey(id)) return null;

        return tableTasks.get(id);
    }

    /// Подзадачи.
    public static Subtask getSubtaskByID(int id) {
        if (!tableSubtasks.containsKey(id)) return null;

        return tableSubtasks.get(id);
    }

    /// Эпика.
    public static Epic getEpicByID(int id) {
        if (!tableEpics.containsKey(id)) return null;

        return tableEpics.get(id);
    }
    //endregion

    //region d. Создание. Сам объект должен передаваться в качестве параметра.

    /// Задачи.
    public static void addTask(Task task) {
        tableTasks.put(task.getId(), task);
    }

    /// Подзадачи.
    public static void addSubtask(Subtask subtask) {
        tableSubtasks.put(subtask.getId(), subtask);
    }

    /// Эпика.
    public static void addEpic(Epic epic) {
        tableEpics.put(epic.getId(), epic);
    }
    //endregion

    //region e. Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра.

    /// Задачи.
    public static void updateTask(Task task) {

        //a. Менеджер сам не выбирает статус для задачи. Информация о нём приходит
        // менеджеру вместе с информацией о самой задаче. По этим данным в одних случаях
        // он будет сохранять статус, в других будет рассчитывать.
        STATUS curStatusTask = task.getStatus();
        if (curStatusTask == STATUS.NEW) {
            task.setStatus(STATUS.IN_PROGRESS);
        } else if (curStatusTask == STATUS.IN_PROGRESS) {
            task.setStatus(STATUS.DONE);
        }

        tableTasks.put(task.getId(), task);
    }

    /// Подзадачи.
    public static void updateSubtask(Subtask subtask) {

        //Для эпиков:
        //если у эпика нет подзадач или все они имеют статус NEW, то статус должен быть NEW.
        //  Ситуации:
        //      - создаю новый эпик, подзадач нет, статус NEW.
        //      - todo: добавляю новую задачу в эпик, статус у нее NEW. Смена статусов эпика:
        //          - NEW -> NEW (не меняем)
        //          - IN_PROGRESS -> IN_PROGRESS (не меняем)
        //          - DONE -> NEW (заново открываем эпик)
        //если все подзадачи имеют статус DONE, то и эпик считается завершённым — со статусом DONE.
        //  Ситуации:
        //      - todo: обновляю подзадачу, происходит смена статусов ее и эпика:
        //          - подзадачи NEW -> IN_PROGRESS, эпику устанавливаем IN_PROGRESS
        //          - подзадачи IN_PROGRESS -> DONE, эпику устанавливаем:
        //              - DONE - если все подзадачи закрыты
        //              - IN_PROGRESS - во всех остальных случаях
        //        во всех остальных случаях статус должен быть IN_PROGRESS.

        tableSubtasks.put(subtask.getId(), subtask);
    }

    /// Эпика.
    public static void updateEpic(Epic epic) {

        //Статусом эпика управляют подзадачи.

        tableEpics.put(epic.getId(), epic);
    }
    //endregion

    //region f. Удаление по идентификатору.

    /// Задачи.
    public static void delTaskByID(int id) {
        if (!tableTasks.containsKey(id)) return;

        tableTasks.remove(id);
    }

    /// Подзадачи.
    public static void delSubtaskByID(int id) {
        if (!tableSubtasks.containsKey(id)) return;

        tableSubtasks.remove(id);
    }

    /// Эпика.
    public static void delEpicByID(int id) {
        if (!tableEpics.containsKey(id)) return;

        //Удаляем все подзадачи эпика.
        Epic epic = getEpicByID(id);
        ArrayList<Subtask> subtasksByEpic = getSubtasksByEpic(epic);

        for (Subtask subtask : subtasksByEpic) {
            tableSubtasks.remove(subtask.getId());
        }

        //После удаления подзадач, удаляем сам эпик.
        tableEpics.remove(id);
    }
    //endregion

    //region Дополнительные методы.

    /// a. Получение списка всех подзадач определённого эпика.
    public static ArrayList<Subtask> getSubtasksByEpic(Epic epic) {
        ArrayList<Subtask> subtasksByEpic = new ArrayList<>();

        if (epic == null) return subtasksByEpic;

        int epicId = epic.getId();

        for (Subtask subtask : tableSubtasks.values()) {
            if (subtask.getEpicId() == epicId) subtasksByEpic.add(subtask);
        }

        return subtasksByEpic;
    }
    //endregion
}