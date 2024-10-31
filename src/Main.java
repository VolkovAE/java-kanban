import tracker.test.TestTracker;

public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");

        //Вынес тестирование в отдельный класс, здесь только точка входа в программу.
        TestTracker testTracker = new TestTracker();    //пакет тестирования прилагается в составе пакета tracker
        testTracker.execTest();
    }
}