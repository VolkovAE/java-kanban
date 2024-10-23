package tracker.services;

public class ID {

    private static int numberOfTasks = 0;

    public static int createId() {
        return ++numberOfTasks;
    }

    public static void reset() {
        numberOfTasks = 0;
    }
}