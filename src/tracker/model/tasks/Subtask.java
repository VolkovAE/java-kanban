package tracker.model.tasks;

import tracker.model.enums.STATUS;
import tracker.services.TaskManager;

import java.util.ArrayList;

public class Subtask extends Task {

    private final int epicId;

    public Subtask(String name, String descr, int epicId) {
        super(name, descr);

        this.epicId = epicId;

        openEpic();
    }

    private void openEpic() {
        Epic epic = TaskManager.getEpicByID(epicId);

        if (epic == null) return;   //такого эпика нет в системе

        if (epic.getStatus() == STATUS.DONE) {  //если эпик закрыт, то при добавлении подзадачи откроем его повторно
            epic.setStatus(STATUS.IN_PROGRESS);
        }
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public void runStateMachine() {
        super.runStateMachine();    //обновление статуса подзадачи

        updateStatusEpic(); //обновляем статус эпика с учетом обновления статуса подзадачи
    }

    private void updateStatusEpic() {
        Epic epic = TaskManager.getEpicByID(epicId);

        if (epic == null) return;   //такого эпика нет в системе

        boolean isNew = false;
        boolean isDone = false;

        ArrayList<Subtask> subtasks = TaskManager.getSubtasksByEpic(epic);
        for (Subtask subtask : subtasks) {
            switch (subtask.status) {
                case IN_PROGRESS -> {
                    epic.setStatus(STATUS.IN_PROGRESS); //одна в работе, эпик в работе
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
        if ((isNew) && (isDone)) epic.setStatus(STATUS.IN_PROGRESS);    //есть и new и done подзадачи
        else if ((!isNew) && (isDone)) epic.setStatus(STATUS.DONE); //все подзадачи закрыты
        else epic.setStatus(STATUS.NEW);    //или только открытые подзадачи или нет подзадач
    }
}