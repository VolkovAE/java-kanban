package tracker.webapi.enums;

import java.util.Arrays;

public enum TypesRequests {
    GET("GET"),
    POST("POST"),
    DELETE("DELETE");

    private final String value;

    private TypesRequests(String value) {
        this.value = value;
    }

    public static TypesRequests fromString(String s) throws IllegalArgumentException {
        return Arrays.stream(TypesRequests.values())
                .filter(v -> v.value.equals(s))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Не известный тип запроса: %s", s)));
    }

    public boolean isGet() {
        return this == TypesRequests.GET;
    }

    public boolean isPost() {
        return this == TypesRequests.POST;
    }

    public boolean isDelete() {
        return this == TypesRequests.DELETE;
    }

    @Override
    public String toString() {
        switch (this) {
            case GET -> {
                return "GET";
            }
            case POST -> {
                return "POST";
            }
            case DELETE -> {
                return "DELETE";
            }
            default -> {
                return super.toString();
            }
        }
    }
}