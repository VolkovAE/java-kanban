package tracker.services.exceptions;

import tracker.services.enums.TypeTask;

public class SetPropertyTaskException extends RuntimeException {
    private final TypeTask typeTask;
    private final int id_task;

    public SetPropertyTaskException(TypeTask typeTask, int id_task, String message) {
        super(message);

        this.typeTask = typeTask;
        this.id_task = id_task;
    }

    public TypeTask getTypeTask() {
        return typeTask;
    }

    public int getIdTask() {
        return id_task;
    }

    @Override
    public String getMessage() {
        return String.format("При выполнении операций с объектом %s ID %d возникла ошибка %s",
                typeTask.toString(), id_task, super.getMessage());
    }
}