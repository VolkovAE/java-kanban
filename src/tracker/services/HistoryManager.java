package tracker.services;

import tracker.model.tasks.Task;

import java.util.List;

public interface HistoryManager {
    int MAX_SIZE_HISTORY = 10;

    void add(Task task);    //помечает задачи как просмотренные

    List<Task> getHistory();    //возвращет список просмотренных задач (история)

    <T> void delTask(T task);   //удаляем задачу из истории
}