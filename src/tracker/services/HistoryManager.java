package tracker.services;

import tracker.model.tasks.Task;

import java.util.List;

public interface HistoryManager {
    void add(Task task);    //помечает задачи как просмотренные

    List<Task> getHistory();    //возвращает список просмотренных задач (история)

    void remove(int id);    //удаляет задачу из истории
}