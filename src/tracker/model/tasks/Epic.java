package tracker.model.tasks;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class Epic extends Task implements PropertyChangeListener {
    transient private List<Subtask> subtasks = new ArrayList<>();

    //Группа временных параметров (начала выполнения, продолжительность, конец выполнения).
    //Обозначаем аббревиатурой SDE (startTime, duration, endTime).
    private LocalDateTime endTime;

    public Epic(String name, String descr) {
        super(name, descr);

        initEpic();
    }

    private void initEpic() {
        startTime = null;
        duration = Duration.ZERO;
        endTime = null;
    }

    public List<Subtask> getSubtasks() {
        if (subtasks == null) subtasks = new ArrayList<>();

        return new ArrayList<>(subtasks);   //subtasks;
    }

    public void setSubtasks() {
        if (subtasks == null) subtasks = new ArrayList<>();

        for (Subtask subtask : subtasks) subtask.removePropertyChangeListener(this);

        subtasks = new ArrayList<>();   //в сеттере будем сбрасывать список подзадач

        initEpic(); //нет подзадач, значит все временные показатели сбрасываем
    }

    public void setSubtasks(Subtask subtask) {
        if (subtasks == null) subtasks = new ArrayList<>();

        subtask.addPropertyChangeListener(this);

        subtasks.add(subtask);

        calcSDEWithAddSubtask(subtask);
    }

    public void setSubtasks(List<Subtask> subtasks) {
        if (this.subtasks == null) this.subtasks = new ArrayList<>();

        for (Subtask subtask : this.subtasks) subtask.removePropertyChangeListener(this);
        for (Subtask subtask : subtasks) subtask.addPropertyChangeListener(this);

        this.subtasks = subtasks;

        calcSDEWithChangedListSubtasks();
    }

    @Override
    public Optional<LocalDateTime> getEndTime() {
        return Optional.ofNullable(endTime);
    }

    private void calcSDEWithAddSubtask(Subtask subtask) {
        //При добавлении новой задачи выполняем коррекцию значений SDE:
        //  1. startTime = min(st, subtask.st)
        //  2. duration += subtask.duration
        //  3. endTime = startTime + duration

        if (subtask.startTime == null) return;  //такая задача не влияет на показатели SDE

        if (startTime == null) {
            //До этого момента у эпика либо не было подзадач, либо у них startTime == null и duration = 0.
            startTime = subtask.startTime;
            duration = subtask.duration;
            endTime = startTime.plus(duration);

            return;
        }

        if (subtask.startTime.isBefore(startTime))
            startTime = subtask.startTime;   //у новой подзадачи startTime меньше чем было у эпика

        duration = duration.plus(subtask.duration); //увеличиваем длительность выполнения эпика

        endTime = startTime.plus(duration); //актуализировали конец работ над эпиком
    }

    private void calcSDEWithChangedListSubtasks() {
        //При смене списка задач, выполняем новый расчет показателей SDE (с начала):
        //  1. Находим минимальное значение startTime из списка подзадач (min st).
        //  2. Находим duration как сумму длительности всех подзадач (сумма d).
        //  3. Вычисляем значение endTime (et = st + d).

        //Подготовлю объект компаратора, чтобы сравнивать подзадачи по полю startTime.
        //Установлю правило, что подзадача со startTime == null считается больше подзадачи,
        //у которой startTime != null.
        Comparator<Subtask> subtaskComparator = (Subtask subtask1, Subtask subtask2) -> {
            if ((subtask1.startTime == null) && (subtask2.startTime == null)) return 0; //подзадачи равны
            else if (subtask2.startTime == null) return -1; //подзадача 2 > подзадачи 1
            else if (subtask1.startTime == null) return 1;  //подзадача 1 > подзадачи 2
            else if (subtask1.startTime.isBefore(subtask2.startTime)) return -1;    //подзадача 1 < подзадачи 2
            else if (subtask1.startTime.equals(subtask2.startTime)) return 0;   //подзадачи равны
            else return 1;  //подзадача 1 > подзадачи 2
        };

        Optional<Subtask> optionalSubtask = subtasks.stream().min(subtaskComparator);
        if (optionalSubtask.isEmpty()) {
            //Список подзадач пуст.
            //В этом случае сбрасываем показатели SDE.
            initEpic();

            return;
        } else if (optionalSubtask.get().startTime == null) {
            //У всех подзадач startTime == null и duration == 0.
            //В этом случае сбрасываем показатели SDE.
            initEpic();

            return;
        }

        startTime = optionalSubtask.get().startTime;    //определили начало работ

        //Получили длительность эпика.
        duration = subtasks.stream()
                .map((subtask) -> subtask.duration)
                .reduce(Duration.ZERO, Duration::plus);

        endTime = startTime.plus(duration); //определили конец работ
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String nameField = evt.getPropertyName();

        if (nameField.equals("startTime")) {
            changedStartTimeSubtask(evt.getOldValue(), evt.getNewValue());
        } else if (nameField.equals("duration")) {
            changedDurationSubtask((long) evt.getOldValue(), (long) evt.getNewValue());
        }
    }

    private <T> void changedStartTimeSubtask(T oldValue, T newValue) {
        if (oldValue.equals(newValue)) return;

        //System.out.println("Произошло изменение значения в поле startTime подзадачи с " + oldValue + " на " + newValue);

        //if (newValue.isBefore(startTime)) startTime = newValue; - решил для данного места оставить ситуацию, что две подзадачи
        //могут иметь одинаковое время начала. Потребуется ее найти. Поэтому запускаем алгоритм расчета показателей SDE сначала.
        calcSDEWithChangedListSubtasks();
    }

    private void changedDurationSubtask(long oldValue, long newValue) {
        if (oldValue == newValue) return;

        //System.out.println("Произошло изменение значения в поле duration подзадачи с " + oldValue + " на " + newValue);
        //duration = duration.minus(Duration.ofMinutes(oldValue)).plus(Duration.ofMinutes(newValue)); //скорректировал длительность работ над эпиком
        duration = duration.minusMinutes(oldValue).plusMinutes(newValue);

        endTime = startTime.plus(duration); //актуализировал время завершения работы над эпиком
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