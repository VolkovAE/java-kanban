package tracker.services;

import tracker.model.tasks.Epic;
import tracker.model.tasks.Subtask;
import tracker.model.tasks.Task;

import java.nio.file.Path;
import java.nio.file.Paths;

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