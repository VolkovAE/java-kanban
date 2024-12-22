import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tracker.model.tasks.Epic;
import tracker.model.tasks.Subtask;
import tracker.model.tasks.Task;
import tracker.services.FileBackedTaskManager;
import tracker.services.Managers;
import tracker.services.TaskManager;
import tracker.services.exceptions.ManagerSaveException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest {
    @Test
    public void shouldBeExceptionWithDoNotExistsFile() {
        //Проверим загрузку из несуществующего файла.
        //Успешно, если будет выброшено исключение ManagerSaveException.

        String fileNameNotExists = System.getProperty("user.dir");

        Path pathNameNotExists = Paths.get(fileNameNotExists, "NoExistsFile.txt");

        assertThrows(ManagerSaveException.class, () -> {
            FileBackedTaskManager.loadFromFile(pathNameNotExists);
        });
    }

    @Test
    public void checkSaveLoadEmptyFile() {
        //Проверим сохранение и загрузку пустого файла.

        //Порядок:
        //  - создаем пустой файл
        //  - делаем из него загрузку loadFromFile
        //  -> успешно, если:
        //      - вернулся объект типа FileBackedTaskManager
        //      - списки задач, подзадач и эпиков пусты
        //  - делаем задачу (произойдет запись в файл, здесь это не проверяем)
        //  -> промежуточная проверка, что размер переданного файла отличен от 0
        //  - удаляем задачу (произойдет запись в файл, здесь это не проверяем)
        //  -> успешно, если размер переданного файла равен 0
        //  - удаляем тестовый файл

        File file;
        try {
            file = File.createTempFile("emptyFile", null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Path path = file.toPath();

        FileBackedTaskManager fileBackedTaskManager;
        try {
            fileBackedTaskManager = FileBackedTaskManager.loadFromFile(path);
        } catch (ManagerSaveException e) {
            throw new RuntimeException(e);
        }

        assertNotNull(fileBackedTaskManager, "Не создан менеджер задач.");

        assertInstanceOf(FileBackedTaskManager.class, fileBackedTaskManager, "Менеджер задач не использует " +
                "файловое хранилище.");

        assertTrue(fileBackedTaskManager.getTasks().isEmpty() &&
                fileBackedTaskManager.getSubtasks().isEmpty() &&
                fileBackedTaskManager.getEpics().isEmpty(), "Из пустого файла были созданы задачи.");

        String name = "Задача 1";
        String descr = "Описание задачи 1";

        Task task = new Task(name, descr);
        int id = fileBackedTaskManager.addTask(task);   //сделали запись в файл, теперь его размер больше 0

        long sizeFile = 0;
        try {
            sizeFile = Files.size(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        assertNotEquals(0, sizeFile, "При добавлении задачи, размер файла " + sizeFile);

        fileBackedTaskManager.delTaskByID(id);

        try {
            sizeFile = Files.size(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        assertEquals(0, sizeFile, "При удалении единственной задачи, размер файла " + sizeFile);

        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void checkSaveToFile() {
        //Проверим сохранение нескольких задач.

        //Порядок:
        //  - создаем новый менеджер использующий файловое хранилище
        //  - создаем задачи:
        //      - Task1
        //      - Task2
        //      - Epic1
        //      - Subtask11(Epic1)
        //      - Subtask12(Epic1)
        //      - Epic2
        //      - Subtask21(Epic2)
        //  - читаем содержимое файла и сравниваем с эталоном, который был подготовлен заранее
        //  -> успешно, если содержимое файлов равно

        TaskManager taskManager = Managers.getDefault(true);

        //Создаем две задачи, а также эпик с двумя подзадачами и эпик с одной подзадачей.
        Task task1 = new Task("Задача 1", "Описание задачи 1.");    //id = 1
        taskManager.addTask(task1);
        Task task2 = new Task("Задача 2", "Описание задачи 2.");    //id = 2
        taskManager.addTask(task2);

        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1.");   //id = 3
        taskManager.addEpic(epic1);

        Subtask subtask11 = new Subtask("Подзадача 11", "Описание подзадачи 11.", epic1);   //id = 4
        taskManager.addSubtask(subtask11);
        Subtask subtask12 = new Subtask("Подзадача 12", "Описание подзадачи 12.", epic1);   //id = 5
        taskManager.addSubtask(subtask12);

        Epic epic2 = new Epic("Эпик 2", "Описание эпика 2.");   //id = 6
        taskManager.addEpic(epic2);

        Subtask subtask21 = new Subtask("Подзадача 21", "Описание подзадачи 21.", epic2);   //id = 7
        taskManager.addSubtask(subtask21);

        String[] etalon = getEtalonContextFile();

        try (FileReader fileReader = new FileReader(((FileBackedTaskManager) taskManager).getFileNameSave().toString());
             LineNumberReader lineNumberReader = new LineNumberReader(fileReader)) {

            while (lineNumberReader.ready()) {

                String strCur = lineNumberReader.readLine();
                int lineNumber = lineNumberReader.getLineNumber();

                assertEquals(strCur, etalon[lineNumber - 1], "Запись в файл имеет ошибку в строке " + lineNumber);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String[] getEtalonContextFile() {
        String[] arr = new String[7];
        arr[0] = "1,TASK,Задача 1,NEW,Описание задачи 1.,";
        arr[1] = "2,TASK,Задача 2,NEW,Описание задачи 2.,";
        arr[2] = "3,EPIC,Эпик 1,NEW,Описание эпика 1.,";
        arr[3] = "6,EPIC,Эпик 2,NEW,Описание эпика 2.,";
        arr[4] = "4,SUBTASK,Подзадача 11,NEW,Описание подзадачи 11.,3";
        arr[5] = "5,SUBTASK,Подзадача 12,NEW,Описание подзадачи 12.,3";
        arr[6] = "7,SUBTASK,Подзадача 21,NEW,Описание подзадачи 21.,6";

        return arr;
    }

    @Test
    public void checkLoadTasksFromFile() {
        //Проверим загрузку нескольких задач.
        //Реализовать доп задание, только через этот метод, а не добавление main в FileBackedTaskManager

        //Порядок:
        //  - создаем новый менеджер использующий файловое хранилище №1
        //  - создаем задачи:
        //      - Task1
        //      - Task2
        //      - Epic1
        //      - Subtask11(Epic1)
        //      - Subtask12(Epic1)
        //      - Epic2
        //      - Subtask21(Epic2)
        //  - создаем новый менеджер использующий файловое хранилище №2, загружает данные из файла менеджера 1
        //  - сравниваем созданные задачи и задачи менеджера №2
        //  -> успешно, если задачи созданные и задачи менеджера №2 равны:
        //      по полям id, name, status, descr для всех типов задач;
        //      для Subtask дополнительно совпадают эпики по полям: id, name, status, descr;
        //      для Epic дополнительно совпадают Subtask по количеству и по полям каждая подзадача id, name, status, descr;

        TaskManager taskManager = Managers.getDefault(true);

        //Создаем две задачи, а также эпик с двумя подзадачами и эпик с одной подзадачей.
        Task task1 = new Task("Задача 1", "Описание задачи 1.");    //id = 1
        taskManager.addTask(task1);
        Task task2 = new Task("Задача 2", "Описание задачи 2.");    //id = 2
        taskManager.addTask(task2);

        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1.");   //id = 3
        taskManager.addEpic(epic1);

        Subtask subtask11 = new Subtask("Подзадача 11", "Описание подзадачи 11.", epic1);   //id = 4
        taskManager.addSubtask(subtask11);
        Subtask subtask12 = new Subtask("Подзадача 12", "Описание подзадачи 12.", epic1);   //id = 5
        taskManager.addSubtask(subtask12);

        Epic epic2 = new Epic("Эпик 2", "Описание эпика 2.");   //id = 6
        taskManager.addEpic(epic2);

        Subtask subtask21 = new Subtask("Подзадача 21", "Описание подзадачи 21.", epic2);   //id = 7
        taskManager.addSubtask(subtask21);

        TaskManager taskManager2 = FileBackedTaskManager.loadFromFile(((FileBackedTaskManager) taskManager).getFileNameSave());

        //Сравниваем Task.
        List<Task> tasks = taskManager.getTasks();
        List<Task> tasks2 = taskManager2.getTasks();

        assertEquals(tasks.size(), tasks2.size(), "Загружено некорректное количество задач.");

        for (int index = 0; index < tasks.size(); index++) {
            assertTrue(isEqualsFields(tasks.get(index), tasks2.get(index)), "Задачи загружены не корректно.");
        }

        //Сравниваем Subtask.
        List<Subtask> subtasks = taskManager.getSubtasks();
        List<Subtask> subtasks2 = taskManager2.getSubtasks();

        assertEquals(subtasks.size(), subtasks2.size(), "Загружено некорректное количество подзадач.");

        for (int index = 0; index < subtasks.size(); index++) {
            assertTrue(isEqualsFields(subtasks.get(index), subtasks2.get(index)), "Подзадачи загружены не корректно.");

            //Сверяем эпики по полям.
            assertTrue(isEqualsFields(subtasks.get(index).getEpic(), subtasks2.get(index).getEpic()));
        }

        //Сравниваем Epic.
        List<Epic> epics = taskManager.getEpics();
        List<Epic> epics2 = taskManager.getEpics();

        assertEquals(epics.size(), epics2.size(), "Загружено некорректное количество эпиков.");

        for (int index = 0; index < epics.size(); index++) {
            assertTrue(isEqualsFields(epics.get(index), epics2.get(index)), "Эпики загружены не корректно.");

            //Сверяем подзадачи эпиков.
            subtasks = epics.get(index).getSubtasks();
            subtasks2 = epics2.get(index).getSubtasks();

            assertEquals(subtasks.size(), subtasks2.size(), "Не корректное количество подзадач в загруженном эпике.");

            for (int index1 = 0; index1 < subtasks.size(); index1++) {
                assertTrue(isEqualsFields(subtasks.get(index1), subtasks2.get(index1)), "Не корректный список подзадач" +
                        "в загруженном эпике.");
            }
        }
    }

    private boolean isEqualsFields(Task task1, Task task2) {
        return (task1.getId() == task2.getId()) &&
                (task1.getName().equals(task2.getName())) &&
                (task1.getDescr().equals(task2.getDescr())) &&
                (task1.getStatus() == task2.getStatus());
    }
}