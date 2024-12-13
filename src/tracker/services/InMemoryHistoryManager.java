package tracker.services;

import tracker.model.tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//Поставил модификатор доступа по умолчанию.
//Создавать объекты класса InMemoryTaskManager только в Managers.
class InMemoryHistoryManager implements HistoryManager {
    HashMap<Integer, Node<Task>> nodeHashMap = new HashMap<>();    //связь ключ-узел

    //region Реализация двусвязного списка.

    private Node<Task> head;    //указатель на первый элемент списка. Он же first

    private Node<Task> tail;    //указатель на последний элемент списка. Он же last

    /// Добавляет задачу в конец двусвязного списка.
    private void linkLast(Task data) {
        final Node<Task> oldTail = tail;
        final Node<Task> newNode = new Node<>(oldTail, data, null);
        tail = newNode;
        if (oldTail == null)
            head = newNode;
        else
            oldTail.next = newNode;
    }

    /// Выгрузить задачи из двусвязного списка в ArrayList.
    private List<Task> getTasks() {
        ArrayList<Task> tasks = new ArrayList<>();

        Node<Task> iterator = head;
        while (iterator != null) {
            tasks.add(iterator.data);

            iterator = iterator.next;
        }

        return tasks;
    }

    /// Удалять узел из двусвязного списка.
    private void removeNode(Node<Task> node) {
        if (node == null) return;

        if (node.prev != null)
            node.prev.next = node.next;
        else
            head = node.next;

        if (node.next != null)
            node.next.prev = node.prev;
        else
            tail = node.prev;

        node.prev = null;
        node.next = null;
    }
    //endregion

    @Override
    public void add(Task task) {
        int idTask = task.getId();

        remove(idTask); //удаляем задачу из истории

        linkLast(task); //добавляем задачу в конец двусвязного списка

        nodeHashMap.put(idTask, tail);  //добавляем/обновляем связь ключ-задача
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    @Override
    public void remove(int id) {
        if (!nodeHashMap.containsKey(id)) return;   //если задачи в истории нет, то выходим

        Node<Task> taskNode = nodeHashMap.get(id); //получили узел задачи по ее id

        removeNode(taskNode);   //удаляем узел из двусвязного списка

        nodeHashMap.remove(id); //удаляем запись в таблице связи ключ-узел
    }
}