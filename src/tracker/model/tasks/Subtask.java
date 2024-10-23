package tracker.model.tasks;

public class Subtask extends Task {

    private int epicId;

    public Subtask(String name, String descr, int epicId) {
        super(name, descr);

        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }
}