import tracker.model.tasks.Epic;
import tracker.model.tasks.Subtask;
import tracker.model.tasks.Task;
import tracker.services.TaskManager;

public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");

        //Создайте две задачи, а также эпик с двумя подзадачами и эпик с одной подзадачей.
        Task task1 = new Task("Задача 1", "Описание задачи 1.");
        TaskManager.addTask(task1);
        Task task2 = new Task("Задача 2", "Описание задачи 2.");
        TaskManager.addTask(task2);

        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1.");
        TaskManager.addEpic(epic1);

        Subtask subtask11 = new Subtask("Подзадача 11", "Описание подзадачи 11.", epic1.getId());
        TaskManager.addSubtask(subtask11);
        Subtask subtask12 = new Subtask("Подзадача 12", "Описание подзадачи 12.", epic1.getId());
        TaskManager.addSubtask(subtask12);

        Epic epic2 = new Epic("Эпик 2", "Описание эпика 2.");
        TaskManager.addEpic(epic2);

        Subtask subtask21 = new Subtask("Подзадача 21", "Описание подзадачи 21.", epic2.getId());
        TaskManager.addSubtask(subtask21);

        //Распечатайте списки эпиков, задач и подзадач через System.out.println(..).
        print("NEW");

        //Измените статусы созданных объектов, распечатайте их.
        // Проверьте, что статус задачи и подзадачи сохранился, а статус эпика рассчитался по статусам подзадач.
        TaskManager.updateTask(task1);
        TaskManager.updateTask(task2);
        TaskManager.updateTask(task1);
        TaskManager.updateSubtask(subtask11);
        print("task1 = done, task2 = in_progress, subtask11/epic1 = in_progress, остальное без изменений");

        TaskManager.updateSubtask(subtask21);
        print("subtask21/epic2 = in_progress, остальное без изменений");

        TaskManager.updateSubtask(subtask21);
        print("subtask21/epic2 = done, остальное без изменений");

        TaskManager.updateSubtask(subtask11);
        print("subtask11 = done, остальное без изменений");

        TaskManager.updateSubtask(subtask11);
        print("без изменений");

        TaskManager.updateSubtask(subtask12);
        print("subtask12 = in_progress, остальное без изменений");

        TaskManager.updateSubtask(subtask12);
        print("subtask12/epic2 = done, остальное без изменений");

        TaskManager.updateTask(task2);
        print("DONE");

        //И, наконец, попробуйте удалить одну из задач и один из эпиков.
        TaskManager.delSubtaskByID(subtask11.getId());
        print("subtask11 - удалена");

        TaskManager.delTaskByID(task2.getId());
        print("task2 - удалена");

        TaskManager.delEpicByID(epic1.getId());
        print("subtask12/epic1 - удалены");

        //Пояснение: переменные с ссылками на объекты сделаны в методе только для удобства тестирования.
        //В работе предполагается получать списки задач/подзадач/эпиков и с ними работать.
    }

    public static void print(String descrTest) {
        System.out.println("Тест - " + descrTest);

        System.out.println("    Список эпиков:");
        for (Epic epic : TaskManager.getEpics()) {
            System.out.println("        " + epic);
        }
        System.out.println('\n');

        System.out.println("    Список задач:");
        for (Task task : TaskManager.getTasks()) {
            System.out.println("        " + task);
        }
        System.out.println('\n');

        System.out.println("    Список подзадач:");
        for (Subtask subtask : TaskManager.getSubtasks()) {
            System.out.println("        " + subtask);
        }
        System.out.println('\n');
        System.out.println("=".repeat(15));
    }
}