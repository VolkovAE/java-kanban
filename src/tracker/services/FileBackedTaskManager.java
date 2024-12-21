package tracker.services;

import tracker.model.enums.Status;
import tracker.model.tasks.Epic;
import tracker.model.tasks.Subtask;
import tracker.model.tasks.Task;
import tracker.services.enums.TypeTask;
import tracker.services.exceptions.ManagerSaveException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static tracker.services.enums.TypeTask.TASK;
import static tracker.services.enums.TypeTask.SUBTASK;
import static tracker.services.enums.TypeTask.EPIC;

import static tracker.model.enums.Status.IN_PROGRESS;
import static tracker.model.enums.Status.DONE;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private Path fileNameSave;

    FileBackedTaskManager(HistoryManager historyManager, Path fileNameSave) {
        super(historyManager);

        this.fileNameSave = fileNameSave;
    }

    FileBackedTaskManager(HistoryManager historyManager, String fileNameSave) {
        super(historyManager);

        this.fileNameSave = Paths.get(fileNameSave);
    }

    private void save() {
        try (FileWriter fileWriter = new FileWriter(fileNameSave.toString(), StandardCharsets.UTF_8);
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
            throw new ManagerSaveException(e.getMessage());
        }
    }

    /// Сохранение задачи в строку.
    private String toString(Task task) {

        TypeTask typeTask;
        if (task instanceof Epic) typeTask = EPIC;
        else if (task instanceof Subtask) typeTask = SUBTASK;
        else typeTask = TASK;

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(task.getId());
        stringBuilder.append(",");
        stringBuilder.append(typeTask.toString());
        stringBuilder.append(",");
        stringBuilder.append(task.getName());
        stringBuilder.append(",");
        stringBuilder.append(task.getStatus().toString());
        stringBuilder.append(",");
        stringBuilder.append(task.getDescr());
        stringBuilder.append(",");
        if (typeTask == SUBTASK) stringBuilder.append(((Subtask) task).getEpic().getId());

        return stringBuilder.toString();
    }

    /// Создание задачи из строки.
    Task fromString(String value) {
        String[] arr = value.split(",");   //0-id, 1-Type, 2-Name, 3-Status, 4-Descr, 5-Epic (если тип Subtask)

        Task task;

        if (arr[1].equals("EPIC")) {
            task = new Epic(arr[2], arr[4]);
        } else if (arr[1].equals("TASK")) {
            task = new Task(arr[2], arr[4]);
        } else if (arr[1].equals("SUBTASK")) {
            task = new Subtask(arr[2], arr[4], this.getEpicByID(Integer.parseInt(arr[5])));
        } else {
            return null;
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
        int id = super.addTask(task);
        save();

        return id;
    }

    @Override
    public int addSubtask(Subtask subtask) {
        int id = super.addSubtask(subtask);
        save();

        return id;
    }

    @Override
    public int addEpic(Epic epic) {
        int id = super.addEpic(epic);
        save();

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