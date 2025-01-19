package tracker.model.tasks;

import tracker.services.enums.TypeTask;
import tracker.services.exceptions.SetPropertyTaskException;

import java.time.LocalDateTime;
import java.util.Objects;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.util.Optional;

public class Subtask extends Task {
    private Epic epic;

//    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
//
//    public void addPropertyChangeListener(PropertyChangeListener listener) {
//        if (pcs != null) pcs.addPropertyChangeListener(listener);
//    }
//
//    public void removePropertyChangeListener(PropertyChangeListener listener) {
//        if (pcs != null) pcs.removePropertyChangeListener(listener);
//    }

    public Subtask(String name, String descr, Epic epic) throws SetPropertyTaskException {
        super(name, descr);

        initSubtask(epic);
    }

    public Subtask(String name, String descr, Epic epic, LocalDateTime startTime, long durationMinutes) {
        super(name, descr, startTime, durationMinutes);

        initSubtask(epic);
    }

    private void initSubtask(Epic epic) {
        this.epic = epic;   //указываем какому эпику принадлежит подзадача

        epic.setSubtasks(this); //добавляем подзадачу в список подзадач эпика
    }

    public Epic getEpic() {
        return epic;
    }

    @Override
    public void setStartTime(LocalDateTime newValue) {
        super.setStartTime(newValue);
    }

    @Override
    public void setDuration(long newValue) throws SetPropertyTaskException {
        long oldValue = getDuration();
        try {
            super.setDuration(newValue);
        } catch (SetPropertyTaskException e) {
            throw new SetPropertyTaskException(TypeTask.SUBTASK, e.getIdTask(), e.getMessage());
        }

        if (pcs != null) pcs.firePropertyChange("duration", oldValue, getDuration());
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