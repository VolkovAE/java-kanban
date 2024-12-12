package tracker.services;

import tracker.model.tasks.Task;

import java.util.ArrayList;
import java.util.List;

//Поставил модификатор доступа по умолчанию.
//Создавать объекты класса InMemoryTaskManager только в Managers.
class InMemoryHistoryManager implements HistoryManager {
    private final List<Task> history = new ArrayList<>();

    @Override
    public void add(Task task) {
        if (history.size() == MAX_SIZE_HISTORY) {
            history.removeFirst();
        }

        history.add(task);
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);    //не даем работать с оригинальным списком, с объектами списка да
    }

    @Override
    public void remove(int id) {

    }
}