package tracker.services;

import tracker.model.enums.Status;
import tracker.model.tasks.Epic;
import tracker.model.tasks.Subtask;
import tracker.model.tasks.Task;
import tracker.services.enums.TypeTask;
import tracker.services.exceptions.CrossTimeExecution;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.Optional;

//Поставил модификатор доступа по умолчанию.
//Создавать объекты класса InMemoryTaskManager только в Managers.
class InMemoryTaskManager implements TaskManager, PropertyChangeListener {

    private int id = 0;

    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();

    //Подготовлю объект компаратора, чтобы сравнивать задачи и подзадачи по полю startTime.
    //Установлю правило, что задача(подзадача) со startTime == null считается больше задачи(подзадачи),
    //у которой startTime != null.
    private final Comparator<Task> taskComparatorByStartTime = (task1, task2) -> {
        Optional<LocalDateTime> startTimeOptional1 = task1.getStartTime();
        Optional<LocalDateTime> startTimeOptional2 = task2.getStartTime();

        if ((startTimeOptional1.isEmpty()) && (startTimeOptional2.isEmpty())) return 0; //подзадачи равны
        else if (startTimeOptional2.isEmpty()) return -1;   //подзадача 2 > подзадачи 1
        else if (startTimeOptional1.isEmpty()) return 1;    //подзадача 1 > подзадачи 2
        else if (startTimeOptional1.get().isBefore(startTimeOptional2.get())) return -1;    //подзадача 1 < подзадачи 2
        else if (startTimeOptional1.get().equals(startTimeOptional2.get())) return 0;   //подзадачи равны
        else return 1;  //подзадача 1 > подзадачи 2
    };

    private TreeSet<Task> tasksSortedByStartTime = new TreeSet<>(taskComparatorByStartTime);

    private final BiPredicate<Task, Task> isTasksCross = (task1, task2) -> {
        //Проверка, что задачи task1 и task2 пересекаются или не пересекаются.
        //Если startTime и/или endTime одной, или обеих задач равны null, то примем, что задачи не пересекаются.
        //Результат:
        //  - true - задачи пересекаются по времени выполнения
        //  - false - задачи не пересекаются по времени выполнения

        Optional<LocalDateTime> a = task1.getStartTime();
        Optional<LocalDateTime> b = task1.getEndTime();
        Optional<LocalDateTime> c = task2.getStartTime();
        Optional<LocalDateTime> d = task2.getEndTime();

        if (a.isEmpty() || b.isEmpty() || c.isEmpty() || d.isEmpty()) return false;

        return (b.get().isAfter(c.get()) || b.get().equals(c.get())) &&
                (d.get().isAfter(a.get()) || d.get().equals(a.get()));
    };

    private final Predicate<Task> isValidatedTaskCross = (Task task) -> {
        //Получаем приоритизированный список задач по startTime, тем самым исключаем из обработки задачи, где не указано начало работ.
        //Для каждого элемента проверяем пересечение с задачей task.
        //Если хоть одна задача пересекается, то валидация не прошла.
        //Результат:
        //  - true - валидация времени выполнения задачи выполнена успешно
        //  - false - валидация времение выполнения задачи не выполнена

        boolean isCross = getPrioritizedTasks().stream()
                .anyMatch((Task taskE) -> isTasksCross.test(taskE, task));

        return !isCross;
    };

    private final HistoryManager historyManager;

    InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /* Методы для каждого из типа задач(Задача/Эпик/Подзадача): */

    //region a. Получение списка всех задач.

    /// Список задач.
    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    /// Список подзадач.
    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    /// Список эпиков.
    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }
    //endregion

    //region b. Удаление всех задач.

    /// Задачи.
    @Override
    public void delAllTasks() {
        for (Task task : tasks.values()) {
            historyManager.remove(task.getId());
        }

        delAllTasksIntasksSortedByStartTime(Task.class);

        tasks.clear();
    }

    /// Подзадачи
    @Override
    public void delAllSubtasks() {
//        for (Subtask subtask : subtasks.values()) {
//            historyManager.remove(subtask.getId());
//        }
        //Заменил на Stream API.
        subtasks.values().stream().forEach(subtask -> historyManager.remove(subtask.getId()));

        delAllTasksIntasksSortedByStartTime(Subtask.class);

        subtasks.clear();

        for (Epic epic : epics.values()) epic.setSubtasks();    //очищаем в эпиках список подзадач
    }

    /// Эпики.
    @Override
    public void delAllEpics() {
//        for (Subtask subtask : subtasks.values()) {
//            historyManager.remove(subtask.getId());
//        }
        //Заменил на Stream API.
        subtasks.values().stream().forEach(subtask -> historyManager.remove(subtask.getId()));

//        for (Epic epic : epics.values()) {
//            historyManager.remove(epic.getId());
//        }
        //Заменил на Stream API.
        epics.values().stream().forEach(epic -> historyManager.remove(epic.getId()));

        subtasks.clear();
        epics.clear();
    }
    //endregion

    //region c. Получение по идентификатору.

    /// Задачи.
    @Override
    public Optional<Task> getTaskByID(int id) {
        if (!tasks.containsKey(id)) return Optional.empty();

        Task task = tasks.get(id);

        historyManager.add(task);   //просмотр задачи

        return Optional.of(task);
    }

    /// Подзадачи.
    @Override
    public Optional<Subtask> getSubtaskByID(int id) {
        if (!subtasks.containsKey(id)) return Optional.empty();

        Subtask subtask = subtasks.get(id);

        historyManager.add(subtask);    //просмотр подзадачи

        return Optional.of(subtask);
    }

    /// Эпика.
    @Override
    public Optional<Epic> getEpicByID(int id) {
        if (!epics.containsKey(id)) return Optional.empty();

        Epic epic = epics.get(id);

        historyManager.add(epic);   //просмотр эпика

        return Optional.of(epic);
    }
    //endregion

    //region d. Создание. Сам объект должен передаваться в качестве параметра.

    /// Задачи.
    @Override
    public int addTask(Task task) throws CrossTimeExecution {
        if (!isValidatedTaskCross.test(task))
            throw new CrossTimeExecution("Добавляемая задача пересекается по времени выполнения.");

        task.setId(++id);

        tasks.put(id, task);

        addTaskIntasksSortedByStartTime(task);

        return id;  //0 - зарезервировано для случая ошибки
    }

    /// Подзадачи.
    @Override
    public int addSubtask(Subtask subtask) {
        if (!isValidatedTaskCross.test(subtask))
            throw new CrossTimeExecution("Добавляемая подзадача пересекается по времени выполнения.");

        subtask.setId(++id);

        subtasks.put(id, subtask);

        Epic epic = subtask.getEpic();

        if (epic.getStatus() == Status.DONE) {  //если эпик закрыт, то при добавлении подзадачи откроем его повторно
            epic.setStatus(Status.IN_PROGRESS);
        }

        addTaskIntasksSortedByStartTime(subtask);

        return id;  //0 - зарезервировано для случая ошибки
    }

    /// Эпика.
    @Override
    public int addEpic(Epic epic) {

        epic.setId(++id);

        epics.put(id, epic);

        return id;  //0 - зарезервировано для случая ошибки
    }
    //endregion

    //region e. Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра.

    /// Задачи.
    @Override
    public boolean updateTask(Task task) {

        //a. Менеджер сам не выбирает статус для задачи. Информация о нём приходит
        // менеджеру вместе с информацией о самой задаче. По этим данным в одних случаях
        // он будет сохранять статус, в других будет рассчитывать.

        int id = task.getId();

        if (!tasks.containsKey(id)) return false;   //проверяем, чтобы метод не добавлял новую задачу

        updateStatusTask(task);

        tasks.put(id, task);

        return true;
    }

    /// Подзадачи.
    @Override
    public boolean updateSubtask(Subtask subtask) {

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

        if (!subtasks.containsKey(id)) return false;    //проверяем, чтобы метод не добавлял новую подзадачу

        updateStatusSubtask(subtask);

        subtasks.put(id, subtask);

        return true;
    }

    /// Эпика.
    @Override
    public boolean updateEpic(Epic epic) {

        //Статусом эпика управляют подзадачи.

        int id = epic.getId();

        if (!epics.containsKey(id)) return false;   //проверяем, чтобы метод не добавлял новый эпик

        epics.put(id, epic);

        return true;
    }
    //endregion

    //region f. Удаление по идентификатору.

    /// Задачи.
    @Override
    public Task delTaskByID(int id) {
        if (!tasks.containsKey(id)) return null;

        Task task = tasks.get(id);

        delTaskIntasksSortedByStartTime(task);

        historyManager.remove(id);  //удаляем задачу из истории просмотров

        tasks.remove(id);

        return task;
    }

    /// Подзадачи.
    @Override
    public Subtask delSubtaskByID(int id) {
        if (!subtasks.containsKey(id)) return null;

        //Удаляем подзадачу из эпика:
        //  получили копию списка подзадач эпика
        //  удалили в нем подзадачу
        //  присвоили эпику ссылку на новый список задач
        Subtask subtask = subtasks.get(id);
        Epic epic = subtask.getEpic();
        List<Subtask> subtaskArrayList = epic.getSubtasks();
        subtaskArrayList.remove(subtask);
        epic.setSubtasks(subtaskArrayList);

        delTaskIntasksSortedByStartTime(subtask);

        historyManager.remove(id);  //удаляем подзадачу из истории просмотров

        //Удаляем саму подзадачу.
        subtasks.remove(id);

        //Обновляем статус эпика (по оставшимся подзадачам).
        updateStatusEpic(epic);

        return subtask;
    }

    /// Эпика.
    @Override
    public Optional<Epic> delEpicByID(int id) {
        if (!epics.containsKey(id)) return null;

        //Удаляем все подзадачи эпика.
        //Обертка для использования в лямбде.
        interface Wrapper {
        }

        var wrapper = new Wrapper() {
            List<Subtask> subtasksByEpic;
        };

        Optional<Epic> epic = getEpicByID(id);
        epic.ifPresent(epicGetId -> {
            wrapper.subtasksByEpic = epicGetId.getSubtasks();
        });
        //List<Subtask> subtasksByEpic = epic.getSubtasks(); //getSubtasksByEpic(epic);

//        for (Subtask subtask : subtasksByEpic) {    //оставил, потому что удаляю ссылки на подзадачи из subtasks
//            subtasks.remove(subtask.getId());
//            historyManager.remove(subtask.getId()); //удаляем подзадачу из истории просмотров
//        }
        //Заменил на использование Stream API.
        wrapper.subtasksByEpic.stream()
                .forEach(subtask -> {
                    subtasks.remove(subtask.getId());
                    historyManager.remove(subtask.getId()); //удаляем подзадачу из истории просмотров
                });

        epic.ifPresent(Epic::setSubtasks);
        //epic.setSubtasks(); //удаляю ссылки на подзадачи из списка подзадач удаляемого эпика (сделал в сеттере)

        //После удаления подзадач, удаляем сам эпик.
        epics.remove(id);

        epic.ifPresent(epicGetId -> historyManager.remove(epicGetId.getId()));
        //historyManager.remove(epic.getId());    //удаляем эпик из истории просмотров

        return epic;
    }
    //endregion

    //region Дополнительные методы.

    /// a. Получение списка всех подзадач определённого эпика.
    @Override
    public List<Subtask> getSubtasksByEpic(Epic epic) {
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

        List<Subtask> subtasks = getSubtasksByEpic(epic);
        for (Subtask subtask : subtasks) {
            switch (subtask.getStatus()) {
                case IN_PROGRESS -> {
                    epic.setStatus(Status.IN_PROGRESS); //одна в работе, эпик в работе
                    return;
                }
                case NEW -> isNew = true;   //запоминаем наличие задач в статусе NEW
                case DONE -> isDone = true;  //запоминаем наличие задач в статусе DONE
            }
        }

        //Нет подзадач в статусе STATUS.IN_PROGRESS.
        if ((isNew) && (isDone)) epic.setStatus(Status.IN_PROGRESS);    //есть и new и done подзадачи
        else if ((!isNew) && (isDone)) epic.setStatus(Status.DONE); //все подзадачи закрыты
        else epic.setStatus(Status.NEW);    //или только открытые подзадачи или нет подзадач
    }
    //endregion

    //region Методы истории данного менеджера задач.
    @Override
    public HistoryManager getHistoryManager() {
        return historyManager;
    }
    //endregion

    //region Методы изменения и предоставления отсортированного списка задач (подзадач) по startTime.
    @Override
    public List<Task> getPrioritizedTasks() {
        return tasksSortedByStartTime.stream()
                .toList();
    }

    private void addTaskIntasksSortedByStartTime(Task task) {
        //1. Проверяем тип параметра task.
        //2. Проверяем, что task != null.
        //3. Проверяем, что значение startTime != null.
        //4. Проверяем, что значения нет в дереве.
        //5. Добавляем в дерево множества.
        //6. Подключаем слушателя, т.к. нужно отслеживать изменение значения в поле startTime.

        if (!(task instanceof Task) && !(task instanceof Subtask)) return;
        if (task == null) return;
        if (task.getStartTime().isEmpty()) return;
        if (tasksSortedByStartTime.contains(task)) return;

        tasksSortedByStartTime.add(task);

        task.addPropertyChangeListener(this);
    }

    private void delTaskIntasksSortedByStartTime(Task task) {
        //1. Проверяем тип параметра task.
        //2. Проверяем, что task != null.
        //3. Проверяем, что значение есть в дереве.
        //4. Отключаем прослушивание изменений значения в поле startTime.
        //5. Удаляем из дерева множества.

        if (!(task instanceof Task) && !(task instanceof Subtask)) return;
        if (task == null) return;
        if (!tasksSortedByStartTime.contains(task)) return;

        task.removePropertyChangeListener(this);

        tasksSortedByStartTime.remove(task);
    }

    private void delAllTasksIntasksSortedByStartTime(Class<? super Subtask> type) {
        //1. Проверяем значение переданного типа.
        //2. Проверяем, что в дереве множества есть элементы.
        //2. Отключаем прослушивание изменений значения в поле startTime в удаленных задачах (подзадачах).
        //3. Удаляем задачи (подзадачи), см параметр typeTask, из tasksSortedByStartTime

        if (!(type.equals(Task.class)) && !(type.equals(Subtask.class))) return;

        if (tasksSortedByStartTime.isEmpty()) return;

        tasksSortedByStartTime.stream()
                .filter(task -> (task.getClass().equals(type)))
                .forEach(task -> task.removePropertyChangeListener(this));

        tasksSortedByStartTime = tasksSortedByStartTime.stream()
                .filter(task -> !(task.getClass().equals(type)))
                .collect(Collectors.toCollection(() -> new TreeSet<Task>(taskComparatorByStartTime)));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String nameField = evt.getPropertyName();

        if (nameField.equals("startTime")) {
            changedStartTimeTaskSubtask((Task) evt.getSource(),
                    (Optional<LocalDateTime>) evt.getOldValue(),
                    (Optional<LocalDateTime>) evt.getNewValue());
        } else if (nameField.equals("duration")) {
            changedDurationTaskSubtask((Task) evt.getSource(),
                    (long) evt.getOldValue(),
                    (long) evt.getNewValue());
        }
    }

    private <T extends Task, V extends Optional<LocalDateTime>> void changedStartTimeTaskSubtask(T task, V oldValue, V newValue)
            throws CrossTimeExecution {
        if (oldValue.equals(newValue)) return;

        //System.out.println("Произошло изменение значения в поле startTime задачи/подзадачи с " + oldValue + " на " + newValue);

        //Учет изменения значения в поле startTime объектов классов Task (Subtask):
        //нет - 1. Отключить прослушивание изменений у объекта (startTime).
        //2. Удалить объект из дерева множества.
        //3. Проверить, что новое значение не равно null.
        //4. Добавить объект в дерево множество (сортировка).
        //нет - 5. Включить прослушивание изменений у объекта (startTime).

        //task.removePropertyChangeListener(this);  //задачи нет в дереве, но изменения из null нужно тоже отследить

        tasksSortedByStartTime = tasksSortedByStartTime.stream()
                .filter(taskCur -> !(taskCur.getId() == task.getId()))
                .collect(Collectors.toCollection(() -> new TreeSet<Task>(taskComparatorByStartTime)));

        if (task.getStartTime().isEmpty()) return;

        if (!isValidatedTaskCross.test(task)) {
            TypeTask typeTask = TypeTask.TASK;

            if (task instanceof Subtask) typeTask = TypeTask.SUBTASK;

            throw new CrossTimeExecution(String.format("При установке начала времени выполнения %s " +
                    "возникает пересечение по времени выполнения.", typeTask));
        }

        tasksSortedByStartTime.add(task);

        //task.addPropertyChangeListener(this);
    }

    private <T extends Task> void changedDurationTaskSubtask(T task, long oldValue, long newValue)
            throws CrossTimeExecution {
        if (oldValue == newValue) return;

        //System.out.println("Произошло изменение значения в поле duration задачи/подзадачи с " + oldValue + " на " + newValue);

        //Учет изменения значения в поле duration объектов классов Task (Subtask):
        //нет - 1. Отключить прослушивание изменений у объекта (duration).
        //2. Удалить объект из дерева множества.
        //3. Проверить, что новое значение не равно null.
        //4. Добавить объект в дерево множество (сортировка).
        //нет - 5. Включить прослушивание изменений у объекта (duration).

        //task.removePropertyChangeListener(this);  //задачи нет в дереве, но изменения из null нужно тоже отследить

        tasksSortedByStartTime = tasksSortedByStartTime.stream()
                .filter(taskCur -> !(taskCur.getId() == task.getId()))
                .collect(Collectors.toCollection(() -> new TreeSet<Task>(taskComparatorByStartTime)));

        if (!isValidatedTaskCross.test(task)) {
            TypeTask typeTask = TypeTask.TASK;

            if (task instanceof Subtask) typeTask = TypeTask.SUBTASK;

            throw new CrossTimeExecution(String.format("При установке длительности выполнения %s " +
                    "возникает пересечение по времени выполнения.", typeTask));
        }

        tasksSortedByStartTime.add(task);

        //task.addPropertyChangeListener(this);
    }
    //endregion
}