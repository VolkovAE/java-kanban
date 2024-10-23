package tracker.model.tasks;

public class Subtask extends Task {

    private final int epicId;

    public Subtask(String name, String descr, int epicId) {
        super(name, descr);

        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }
}