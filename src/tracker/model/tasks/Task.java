package tracker.model.tasks;

import tracker.model.enums.STATUS;
import tracker.services.ID;

public class Task {
    protected String name;
    protected String descr;
    protected int id;
    protected STATUS status;

    public Task(String name, String descr) {
        this.name = name;
        this.descr = descr;
        this.id = ID.createId();
        this.status = STATUS.NEW;
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

    public String getName() {
        return name;
    }

    public String getDescr() {
        return descr;
    }

    public int getId() {
        return id;
    }

    public STATUS getStatus() {
        return status;
    }

    protected void setStatus(STATUS status) {
        this.status = status;
    }

    /// Начальное состояние определяется в конструкторах.
    /// Маршрут: STATUS.NEW -> STATUS.IN_PROGRESS -> STATUS.DONE
    public void runStateMachine() {
        switch (status) {
            case NEW -> setStatus(STATUS.IN_PROGRESS);
            case IN_PROGRESS -> setStatus(STATUS.DONE);
        }
    }
}