package invfly.config;

import com.google.common.reflect.TypeToken;
import invfly.Invfly;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

public class DurationSerializer implements TypeSerializer<Duration> {


    @Override
    public @Nullable Duration deserialize(@NonNull TypeToken<?> type, @NonNull ConfigurationNode value) {
        //copy from sponge
        String string = value.getString();
        if (string != null){
            String s = string.toUpperCase();
            if (!s.contains("T")) {
                if (s.contains("D")) {
                    if (s.contains("H") || s.contains("M") || s.contains("S")) {
                        s = s.replace("D", "DT");
                    }
                } else {
                    if (s.startsWith("P")) {
                        s = "PT" + s.substring(1);
                    } else {
                        s = "T" + s;
                    }
                }
            }
            if (!s.startsWith("P")) {
                s = "P" + s;
            }
            try {
                return Duration.parse(s);
            } catch (DateTimeParseException ex) {
                Invfly.instance.getLogger().error("Invalid duration! set default value: 1d", ex);
            }
        }
        return Duration.of(1, ChronoUnit.DAYS);
    }

    @Override
    public void serialize(@NonNull TypeToken<?> type, @Nullable Duration obj, @NonNull ConfigurationNode value) {
        if (obj != null){
            long day = obj.toDays();
            long hours = obj.minus(Duration.ofDays(day)).toHours();
            long minute = obj.minus(Duration.ofDays(day)).minus(Duration.ofHours(hours)).toMinutes();
            long seconds = obj.minus(Duration.ofDays(day)).minus(Duration.ofHours(hours)).minus(Duration.ofMinutes(minute)).getSeconds();
            StringBuilder stringBuilder = new StringBuilder();
            if (day > 0) stringBuilder.append(day).append("D");
            if (hours > 0) stringBuilder.append(hours).append("H");
            if (minute > 0) stringBuilder.append(minute).append("M");
            if (minute > 0) stringBuilder.append(seconds).append("S");
            value.setValue(stringBuilder.toString());
        }
    }
}
