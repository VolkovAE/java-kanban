package tracker.services;

import tracker.model.tasks.Task;

import java.util.ArrayList;

public interface HistoryManager {
    void add(Task task);    //помечает задачи как просмотренные

    ArrayList<Task> getHistory();   //возвращет список просмотренных задач (история)
}