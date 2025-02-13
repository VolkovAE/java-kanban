package tracker.webapi.handlers.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Duration;

public class DurationAdapter extends TypeAdapter<Duration> {
    @Override
    public void write(final JsonWriter jsonWriter, final Duration duration) throws IOException {
        // работаем с минутами
        jsonWriter.value(duration.toMinutes());
    }

    @Override
    public Duration read(final JsonReader jsonReader) throws IOException, NumberFormatException {
        int durationMinutes = Integer.parseInt(jsonReader.nextString());

        return Duration.ofMinutes(durationMinutes);
    }
}