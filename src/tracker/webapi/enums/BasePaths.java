package tracker.webapi.enums;

/**
 * Перечисление значений базовых путей WEB API.
 */
public enum BasePaths {
    TASKS,
    SUBTASKS,
    EPICS,
    HISTORY,
    PRIORITIZED;

    @Override
    public String toString() {
        switch (this) {
            case TASKS -> {
                return "/tasks";
            }
            case SUBTASKS -> {
                return "/subtasks";
            }
            case EPICS -> {
                return "/epics";
            }
            case HISTORY -> {
                return "/history";
            }
            case PRIORITIZED -> {
                return "/prioritized";
            }
            default -> {
                return null;
            }
        }
    }
}