package tracker.services.exceptions;

public class CrossTimeExecution extends RuntimeException {
    public CrossTimeExecution(String message) {
        super(message);
    }
}