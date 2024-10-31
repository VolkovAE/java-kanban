package tracker.model.tasks;

import java.util.Objects;

public class Subtask extends Task {

    private final Epic epic;

    public Subtask(String name, String descr, Epic epic) {
        super(name, descr);

        this.epic = epic;   //указываем какому эпику принадлежит подзадача

        epic.setSubtasks(this); //добавляем подзадачу в список подзадач эпика
    }

    public Epic getEpic() {
        return epic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Subtask subtask = (Subtask) o;
        return Objects.equals(epic, subtask.epic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), epic);
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "epic=" + epic.getName() +
                ", name='" + name + '\'' +
                ", descr='" + descr + '\'' +
                ", id=" + getId() +
                ", status=" + getStatus() +
                '}';
    }
}