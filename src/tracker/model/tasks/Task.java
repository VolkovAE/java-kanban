package tracker.model.tasks;

import tracker.model.enums.Status;
import tracker.services.enums.TypeTask;
import tracker.services.exceptions.SetPropertyTaskException;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

public class Task {
    private Status status = Status.NEW;
    private int id;

    protected String name;
    protected String descr;
    protected LocalDateTime startTime = null;   //дата и время, когда предполагается приступить к задаче
    protected Duration duration = Duration.ZERO;    //плановая продолжительность выполнения задачи в минутах

    protected final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public final void addPropertyChangeListener(PropertyChangeListener listener) {
        if (pcs != null) pcs.addPropertyChangeListener(listener);
    }

    public final void removePropertyChangeListener(PropertyChangeListener listener) {
        if (pcs != null) pcs.removePropertyChangeListener(listener);
    }

    public Task(String name, String descr) {
        this.name = name;
        this.descr = descr;
    }

    public Task(String name, String descr, LocalDateTime startTime, long durationMinutes) throws SetPropertyTaskException {
        this(name, descr);

        if ((startTime == null) && (durationMinutes != 0)) throw new SetPropertyTaskException(TypeTask.TASK, 0,
                "Не допускается создание задачи с продолжительностью выполнения не равной нулю и startTime равным null.");

        this.startTime = startTime;
        this.setDuration(durationMinutes);
    }

    public String getName() {
        return name;
    }

    public String getDescr() {
        return descr;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Optional<LocalDateTime> getStartTime() {
        return Optional.ofNullable(startTime);
    }

    public void setStartTime(LocalDateTime newValue) {
        Optional<LocalDateTime> oldValue = getStartTime();
        this.startTime = newValue;
        if (pcs != null) pcs.firePropertyChange("startTime", oldValue, getStartTime());
    }

    public long getDuration() {
        return convertDurationToMinutes();
    }

    public void setDuration(long durationMinutes) throws SetPropertyTaskException {
        if (durationMinutes < 0) throw new SetPropertyTaskException(TypeTask.TASK, id,
                "Не допускается установка отрицательного значения в продолжительность выполнения задачи.");

        this.duration = convertMinutesToDuration(durationMinutes);
    }

    private Duration convertMinutesToDuration(long durationMinutes) {
        return Duration.ofMinutes(durationMinutes);
    }

    private long convertDurationToMinutes() {
        return duration.toMinutes();
    }

    public Optional<LocalDateTime> getEndTime() {
        if (startTime == null) return Optional.empty();

        if (duration == Duration.ZERO) return Optional.of(startTime);

        return Optional.of(startTime.plus(duration));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "Task{" +
                "name='" + name + '\'' +
                ", descr='" + descr + '\'' +
                ", id=" + id +
                ", status=" + status +
                '}';
    }
}