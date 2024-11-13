package tracker.services;

import tracker.model.tasks.Task;

import java.util.ArrayList;

public interface HistoryManager {
    int MAX_SIZE_HISTORY = 10;

    void add(Task task);    //помечает задачи как просмотренные

    ArrayList<Task> getHistory();   //возвращет список просмотренных задач (история)

    <T> void delTask(T task);   //удаляем задачу из истории
}