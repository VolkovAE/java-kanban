package tracker.webapi.handlers.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy*****HH:mm:ss");

    @Override
    public void write(final JsonWriter jsonWriter, final LocalDateTime localDateTime) throws IOException {
        // приводим localDateTime к необходимому формату
        if (localDateTime == null) {
            jsonWriter.value("");   //jsonWriter.value((String) null);
        } else {
            jsonWriter.value(localDateTime.format(dtf));
        }
    }

    @Override
    public LocalDateTime read(final JsonReader jsonReader) throws IOException {
        String value = jsonReader.nextString();

        if (value.isEmpty()) return null;

        return LocalDateTime.parse(value, dtf);
    }
}