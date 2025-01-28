package tracker.services.exceptions;

import tracker.services.enums.TypeTask;

public class SetPropertyTaskException extends RuntimeException {
    private final TypeTask typeTask;
    private final int taskId;

    public SetPropertyTaskException(TypeTask typeTask, int taskId, String message) {
        super(message);

        this.typeTask = typeTask;
        this.taskId = taskId;
    }

    public TypeTask getTypeTask() {
        return typeTask;
    }

    public int getIdTask() {
        return taskId;
    }

    @Override
    public String getMessage() {
        return String.format("При выполнении операций с объектом %s ID %d возникла ошибка %s",
                typeTask.toString(), taskId, super.getMessage());
    }
}