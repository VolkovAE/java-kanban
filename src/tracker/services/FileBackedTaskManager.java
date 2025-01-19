package tracker.services;

import tracker.model.enums.Status;
import tracker.model.tasks.Epic;
import tracker.model.tasks.Subtask;
import tracker.model.tasks.Task;
import tracker.services.enums.TypeTask;
import tracker.services.exceptions.ManagerSaveException;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static tracker.services.enums.TypeTask.TASK;
import static tracker.services.enums.TypeTask.SUBTASK;
import static tracker.services.enums.TypeTask.EPIC;

import static tracker.model.enums.Status.IN_PROGRESS;
import static tracker.model.enums.Status.DONE;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final Path fileNameSave;
    private static final Charset encoding = StandardCharsets.UTF_8;

    FileBackedTaskManager(HistoryManager historyManager, Path fileNameSave) {
        super(historyManager);

        this.fileNameSave = fileNameSave;
    }

    FileBackedTaskManager(HistoryManager historyManager, String fileNameSave) {
        super(historyManager);

        this.fileNameSave = Paths.get(fileNameSave);
    }

    FileBackedTaskManager(HistoryManager historyManager, File file) {
        super(historyManager);

        this.fileNameSave = file.toPath();
    }

    public Path getFileNameSave() {
        return fileNameSave;
    }

    //region Функционал записи/чтения задач в/из файл(а).

    /// Восстановление данных менеджера из файла при запуске программы.
    public static FileBackedTaskManager loadFromFile(Path file) throws ManagerSaveException {
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(Managers.getDefaultHistory(), file);

        try (FileReader fileReader = new FileReader(fileBackedTaskManager.getFileNameSave().toString(), encoding);
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {

            int maxId = 0;

            while (bufferedReader.ready()) {
                String str = bufferedReader.readLine();

                Task task = fileBackedTaskManager.fromString(str);

                if (task instanceof Epic) {
                    fileBackedTaskManager.addEpic((Epic) task);
                } else if (task instanceof Subtask) {
                    fileBackedTaskManager.addSubtask((Subtask) task);
                } else {
                    fileBackedTaskManager.addTask(task);
                }

                if (task.getId() > maxId) maxId = task.getId();
            }

            fileBackedTaskManager.setId(maxId);
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка чтения файла задач:\n" + e.getMessage());
        }

        return fileBackedTaskManager;
    }

    public static FileBackedTaskManager loadFromFile(String file) {
        return loadFromFile(Paths.get(file));
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        return loadFromFile(file.toPath());
    }

    /// Сохраняет все задачи, подзадачи и эпики в файл.
    private void save() {
        try (FileWriter fileWriter = new FileWriter(fileNameSave.toString(), encoding);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {

            List<Task> tasks = getTasks();
            tasks.addAll(getEpics());
            tasks.addAll(getSubtasks());

            for (Task task : tasks) {
                String str = toString(task);

                bufferedWriter.write(str);
                bufferedWriter.newLine();
            }
            bufferedWriter.flush();
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка записи задач в файл:\n" + e.getMessage());
        }
    }

    /// Сохранение задачи в строку.
    private String toString(Task task) {

        TypeTask typeTask;
        if (task instanceof Epic) typeTask = EPIC;
        else if (task instanceof Subtask) typeTask = SUBTASK;
        else typeTask = TASK;

        StringBuilder stringBuilder = new StringBuilder();
        appendValueToBuilder(stringBuilder, task.getId(), true);
        appendValueToBuilder(stringBuilder, typeTask.toString(), true);
        appendValueToBuilder(stringBuilder, task.getName(), true);
        appendValueToBuilder(stringBuilder, task.getStatus().toString(), true);
        appendValueToBuilder(stringBuilder, task.getDescr(), true);

        if (typeTask == SUBTASK) {
            appendValueToBuilder(stringBuilder, ((Subtask) task).getEpic().getId(), true);
        } else {
            appendValueToBuilder(stringBuilder, getEmptyValue(), true);
        }

        //startTime
        DateTimeFormatter formatter = getPatternFormatLDT();
        Optional<LocalDateTime> startTime = task.getStartTime();
        if (startTime.isPresent()) {
            appendValueToBuilder(stringBuilder, startTime.get().format(formatter), true);
        } else {
            appendValueToBuilder(stringBuilder, getEmptyValue(), true);
        }

        //duration
        appendValueToBuilder(stringBuilder, task.getDuration(), true);

        //endTime
        Optional<LocalDateTime> endTime = task.getEndTime();
        if (endTime.isPresent()) {
            appendValueToBuilder(stringBuilder, endTime.get().format(formatter), false);
        } else {
            appendValueToBuilder(stringBuilder, getEmptyValue(), false);
        }

        return stringBuilder.toString();
    }

    /// Создание задачи из строки.
    Task fromString(String value) {
        String[] arr = value.split(getSeparator()); //0-id, 1-Type, 2-Name, 3-Status, 4-Descr, 5-Epic (если тип Subtask),
        //6-startTime, 7-duration, 8-endTime (зарезервировано)

        Task task;

        LocalDateTime startTime = getStartEndTime(arr[6]);
        int duration = Integer.parseInt(arr[7]);
        //LocalDateTime endTime = getStartEndTime(arr[8]);    //зарезервировано

        switch (arr[1]) {
            case "EPIC" -> task = new Epic(arr[2], arr[4]);
            case "TASK" -> task = new Task(arr[2], arr[4], startTime, duration);
            case "SUBTASK" ->
                    task = new Subtask(arr[2], arr[4], this.getEpicByID(Integer.parseInt(arr[5])), startTime, duration);
            default -> {
                return null;
            }
        }

        Status status;
        if (arr[3].equals("IN_PROGRESS")) {
            task.setStatus(IN_PROGRESS);
        } else if (arr[3].equals("DONE")) {
            task.setStatus(DONE);
        }

        task.setId(Integer.parseInt(arr[0]));

        return task;
    }

    private <T> void appendValueToBuilder(StringBuilder stringBuilder, T value, boolean isSeparator) {
        stringBuilder.append(value);
        if (isSeparator) stringBuilder.append(getSeparator());
    }

    private String getSeparator() {
        return ",";
    }

    private String getEmptyValue() {
        return "-";
    }

    private DateTimeFormatter getPatternFormatLDT() {
        return DateTimeFormatter.ofPattern("dd.MM.yyyy|HH.mm.ss");
    }

    private LocalDateTime getStartEndTime(String value) {
        if (value.equals(getEmptyValue())) return null;

        return LocalDateTime.parse(value, getPatternFormatLDT());
    }
    //endregion Функционал записи/чтения задач в/из файл(а).

    //region Переопределение методов InMemoryTaskManager
    @Override
    public void delAllTasks() {
        super.delAllTasks();
        save();
    }

    @Override
    public void delAllSubtasks() {
        super.delAllSubtasks();
        save();
    }

    @Override
    public void delAllEpics() {
        super.delAllEpics();
        save();
    }

    @Override
    public int addTask(Task task) {
        int id = task.getId();

        if (id == 0) {  //добавляем новую задачу
            id = super.addTask(task);
            save();
        } else {    //задача десериализована
            tasks.put(id, task);
        }

        return id;
    }

    @Override
    public int addSubtask(Subtask subtask) {
        int id = subtask.getId();

        if (id == 0) {  //добавляем новую подзадачу
            id = super.addSubtask(subtask);
            save();
        } else {    //подзадача десериализована
            subtasks.put(id, subtask);
        }

        return id;
    }

    @Override
    public int addEpic(Epic epic) {
        int id = epic.getId();

        if (id == 0) {  //добавляем новый эпик
            id = super.addEpic(epic);
            save();
        } else {    //эпик десериализован
            epics.put(id, epic);
        }

        return id;
    }

    @Override
    public boolean updateTask(Task task) {
        boolean resultUpd = super.updateTask(task);
        save();

        return resultUpd;
    }

    @Override
    public boolean updateSubtask(Subtask subtask) {
        boolean resultUpd = super.updateSubtask(subtask);
        save();

        return resultUpd;
    }

    @Override
    public boolean updateEpic(Epic epic) {
        boolean resultUpd = super.updateEpic(epic);
        save();

        return resultUpd;
    }

    @Override
    public Task delTaskByID(int id) {
        Task delTask = super.delTaskByID(id);
        save();

        return delTask;
    }

    @Override
    public Subtask delSubtaskByID(int id) {
        Subtask delSubtask = super.delSubtaskByID(id);
        save();

        return delSubtask;
    }

    @Override
    public Epic delEpicByID(int id) {
        Epic delEpic = super.delEpicByID(id);
        save();

        return delEpic;
    }
    //endregion Переопределение методов InMemoryTaskManager
}