package tracker.services;

import tracker.model.enums.Status;
import tracker.model.tasks.Epic;
import tracker.model.tasks.Subtask;
import tracker.model.tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;

//Поставил модификатор доступа по умолчанию.
//Создавать объекты класса InMemoryTaskManager только в Managers.
class InMemoryTaskManager implements TaskManager {

    private static int id = 0;

    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();

    /* Методы для каждого из типа задач(Задача/Эпик/Подзадача): */

    //region a. Получение списка всех задач.

    /// Список задач.
    @Override
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    /// Список подзадач.
    @Override
    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    /// Список эпиков.
    @Override
    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }
    //endregion

    //region b. Удаление всех задач.

    /// Задачи.
    @Override
    public void delAllTasks() {
        tasks.clear();
    }

    /// Подзадачи
    @Override
    public void delAllSubtasks() {
        subtasks.clear();

        for (Epic epic : epics.values()) epic.setSubtasks();    //очищаем в эпиках список подзадач
    }

    /// Эпики.
    @Override
    public void delAllEpics() {
        subtasks.clear();
        epics.clear();
    }
    //endregion

    //region c. Получение по идентификатору.

    /// Задачи.
    @Override
    public Task getTaskByID(int id) {
        if (!tasks.containsKey(id)) return null;

        Task task = tasks.get(id);

        Managers.getDefaultHistory().add(task); //просмотр задачи

        return task;
    }

    /// Подзадачи.
    @Override
    public Subtask getSubtaskByID(int id) {
        if (!subtasks.containsKey(id)) return null;

        Subtask subtask = subtasks.get(id);

        Managers.getDefaultHistory().add(subtask);  //просмотр подзадачи

        return subtask;
    }

    /// Эпика.
    @Override
    public Epic getEpicByID(int id) {
        if (!epics.containsKey(id)) return null;

        Epic epic = epics.get(id);

        Managers.getDefaultHistory().add(epic); //просмотр эпика

        return epic;
    }
    //endregion

    //region d. Создание. Сам объект должен передаваться в качестве параметра.

    /// Задачи.
    @Override
    public void addTask(Task task) {

        task.setId(++id);

        tasks.put(id, task);
    }

    /// Подзадачи.
    @Override
    public void addSubtask(Subtask subtask) {

        subtask.setId(++id);

        subtasks.put(id, subtask);

        Epic epic = subtask.getEpic();

        if (epic.getStatus() == Status.DONE) {  //если эпик закрыт, то при добавлении подзадачи откроем его повторно
            epic.setStatus(Status.IN_PROGRESS);
        }

        epic.getSubtasks().add(subtask);    //добавление подзадачи в эпик
    }

    /// Эпика.
    @Override
    public void addEpic(Epic epic) {

        epic.setId(++id);

        epics.put(id, epic);
    }
    //endregion

    //region e. Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра.

    /// Задачи.
    @Override
    public void updateTask(Task task) {

        //a. Менеджер сам не выбирает статус для задачи. Информация о нём приходит
        // менеджеру вместе с информацией о самой задаче. По этим данным в одних случаях
        // он будет сохранять статус, в других будет рассчитывать.

        int id = task.getId();

        if (!tasks.containsKey(id)) return; //проверяем, чтобы метод не добавлял новую задачу

        updateStatusTask(task);

        tasks.put(id, task);
    }

    /// Подзадачи.
    @Override
    public void updateSubtask(Subtask subtask) {

        //Для эпиков:
        //если у эпика нет подзадач или все они имеют статус NEW, то статус должен быть NEW.
        //  Ситуации (метод addSubtask):
        //      - создаю новый эпик, подзадач нет, статус NEW.
        //      - добавляю новую задачу в эпик, статус у нее NEW. Смена статусов эпика:
        //          - NEW -> NEW (не меняем)
        //          - IN_PROGRESS -> IN_PROGRESS (не меняем)
        //          - DONE -> NEW (заново открываем эпик)
        //если все подзадачи имеют статус DONE, то и эпик считается завершённым — со статусом DONE.
        //  Ситуации:
        //      - обновляю подзадачу, происходит смена статусов ее и эпика:
        //          - подзадачи NEW -> IN_PROGRESS, эпику устанавливаем IN_PROGRESS
        //          - подзадачи IN_PROGRESS -> DONE, эпику устанавливаем:
        //              - DONE - если все подзадачи закрыты
        //              - IN_PROGRESS - во всех остальных случаях
        //        во всех остальных случаях статус должен быть IN_PROGRESS.
        int id = subtask.getId();

        if (!subtasks.containsKey(id)) return; //проверяем, чтобы метод не добавлял новую подзадачу

        updateStatusSubtask(subtask);

        subtasks.put(id, subtask);
    }

    /// Эпика.
    @Override
    public void updateEpic(Epic epic) {

        //Статусом эпика управляют подзадачи.

        int id = epic.getId();

        if (!epics.containsKey(id)) return; //проверяем, чтобы метод не добавлял новый эпик

        epics.put(id, epic);
    }
    //endregion

    //region f. Удаление по идентификатору.

    /// Задачи.
    @Override
    public void delTaskByID(int id) {
        if (!tasks.containsKey(id)) return;

        tasks.remove(id);
    }

    /// Подзадачи.
    @Override
    public void delSubtaskByID(int id) {
        if (!subtasks.containsKey(id)) return;

        //Удаляем подзадачу из эпика:
        //  получили копию списка подзадач эпика
        //  удалили в нем подзадачу
        //  присвоили эпику ссылку на новый список задач
        Subtask subtask = subtasks.get(id);
        Epic epic = subtask.getEpic();
        ArrayList<Subtask> subtaskArrayList = epic.getSubtasks();
        subtaskArrayList.remove(subtask);
        epic.setSubtasks(subtaskArrayList);

        //Удаляем саму подзадачу.
        subtasks.remove(id);

        //Обновляем статус эпика (по оставшимся подзадачам).
        updateStatusEpic(epic);
    }

    /// Эпика.
    @Override
    public void delEpicByID(int id) {
        if (!epics.containsKey(id)) return;

        //Удаляем все подзадачи эпика.
        Epic epic = getEpicByID(id);
        ArrayList<Subtask> subtasksByEpic = epic.getSubtasks(); //getSubtasksByEpic(epic);

        for (Subtask subtask : subtasksByEpic) {    //оставил, потому что удаляю ссылки на подзадачи из subtasks
            subtasks.remove(subtask.getId());
        }

        epic.setSubtasks(); //удаляю ссылки на подзадачи из списка подзадач удаляемого эпика (сделал в сеттере)

        //После удаления подзадач, удаляем сам эпик.
        epics.remove(id);
    }
    //endregion

    //region Дополнительные методы.

    /// a. Получение списка всех подзадач определённого эпика.
    @Override
    public ArrayList<Subtask> getSubtasksByEpic(Epic epic) {
        /*
        ArrayList<Subtask> subtasksByEpic = new ArrayList<>();

        if (epic == null) return subtasksByEpic;

        for (Subtask subtask : subtasks.values()) {
            if (subtask.getEpic() == epic) subtasksByEpic.add(subtask);
        }

        return subtasksByEpic;
         */

        return epic.getSubtasks();
    }
    //endregion

    /* Служебные методы: */

    //region Методы изменения статуса задач.

    /// Начальное состояние определяется в конструкторах.
    /// Каждое обновление задачи переводит ее в следующее состояние.
    /// Маршрут: STATUS.NEW -> STATUS.IN_PROGRESS -> STATUS.DONE
    private void updateStatusTask(Task task) {
        switch (task.getStatus()) {
            case NEW -> task.setStatus(Status.IN_PROGRESS);
            case IN_PROGRESS -> task.setStatus(Status.DONE);
        }
    }

    private void updateStatusSubtask(Subtask subtask) {

        updateStatusTask(subtask);  //обновление статуса подзадачи

        updateStatusEpic(subtask.getEpic());    //обновляем статус эпика с учетом обновления статуса подзадачи
    }

    private void updateStatusEpic(Epic epic) {

        if (epic == null) return;   //такого эпика нет в системе

        boolean isNew = false;
        boolean isDone = false;

        ArrayList<Subtask> subtasks = getSubtasksByEpic(epic);
        for (Subtask subtask : subtasks) {
            switch (subtask.getStatus()) {
                case IN_PROGRESS -> {
                    epic.setStatus(Status.IN_PROGRESS); //одна в работе, эпик в работе
                    return;
                }
                case NEW -> {
                    isNew = true;   //запоминаем наличие задач в статусе NEW
                }
                case DONE -> {
                    isDone = true;  //запоминаем наличие задач в статусе DONE
                }
            }
        }

        //Нет подзадач в статусе STATUS.IN_PROGRESS.
        if ((isNew) && (isDone)) epic.setStatus(Status.IN_PROGRESS);    //есть и new и done подзадачи
        else if ((!isNew) && (isDone)) epic.setStatus(Status.DONE); //все подзадачи закрыты
        else epic.setStatus(Status.NEW);    //или только открытые подзадачи или нет подзадач
    }
    //endregion
}