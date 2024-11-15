package tracker.model.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {

    private List<Subtask> subtasks = new ArrayList<>();

    public Epic(String name, String descr) {
        super(name, descr);
    }

    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks);   //subtasks;
    }

    //public void setSubtasks(ArrayList<Subtask> subtasks) {
    public void setSubtasks() {
        subtasks = new ArrayList<>();  //в сеттере будем сбрасывать список подзадач
    }

    public void setSubtasks(Subtask subtask) {
        subtasks.add(subtask);
    }

    public void setSubtasks(List<Subtask> subtasks) {
        this.subtasks = subtasks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Epic epic = (Epic) o;
        return subtasks.equals(epic.subtasks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subtasks);
    }

    @Override
    public String toString() {

        String info = "Epic{" +
                "name='" + name + '\'' +
                ", descr='" + descr + '\'' +
                ", id=" + getId() +
                ", status=" + getStatus() +
                '}';

        StringBuilder infoSubtask = new StringBuilder();
        for (Subtask subtask : subtasks) {
            infoSubtask.append("                ").append(subtask).append("\n");
        }

        if (infoSubtask.isEmpty()) infoSubtask = new StringBuilder("          Подзадач нет.");
        else infoSubtask.insert(0, "            Подзадачи:\n");

        info += "\n" + infoSubtask;

        return info;
    }
}