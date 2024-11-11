package tracker.services;

import tracker.model.tasks.Task;

import java.util.ArrayList;

//Поставил модификатор доступа по умолчанию.
//Создавать объекты класса InMemoryTaskManager только в Managers.
class InMemoryHistoryManager implements HistoryManager {
    private final ArrayList<Task> history = new ArrayList<>();
    private static final int MAX_SIZE_HISTORY = 10;

    @Override
    public void add(Task task) {
        if (history.size() == MAX_SIZE_HISTORY) {
            history.removeFirst();
        }

        history.add(task);
    }

    @Override
    public ArrayList<Task> getHistory() {
        return new ArrayList<>(history);    //не даем работать с оригинальным списком, с объектами списка да
    }
}