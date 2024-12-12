package tracker.services;

import tracker.model.tasks.Task;

import java.util.ArrayList;
import java.util.List;

//Поставил модификатор доступа по умолчанию.
//Создавать объекты класса InMemoryTaskManager только в Managers.
class InMemoryHistoryManager<T> implements HistoryManager {
    private final List<Task> history = new ArrayList<>();

    private Node<T> head;   //указатель на первый элемент списка. Он же first

    private Node<T> tail;//указатель на последний элемент списка. Он же last

    /// Добавляет задачу в конец двусвязного списка.
    private void linkLast(T data) {
        final Node<T> oldTail = tail;
        final Node<T> newNode = new Node<>(oldTail, data, null);
        tail = newNode;
        if (oldTail == null)
            head = newNode;
        else
            oldTail.next = newNode;
    }

    /// Выгрузить задачи из двусвязного списка в ArrayList.
    private ArrayList<T> getTasks() {
        ArrayList<T> tasks = new ArrayList<>();

        Node<T> iterator = head;
        while (iterator != null) {
            tasks.add(iterator.data);

            iterator = iterator.next;
        }

        return tasks;
    }

    /// Удалять узел из двусвязного списка.
    private void removeNode(Node<T> node) {
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