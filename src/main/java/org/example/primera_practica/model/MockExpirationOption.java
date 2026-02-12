package org.example.primera_practica.model;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public enum MockExpirationOption {
    ONE_YEAR("1 year", Duration.ofDays(365)),
    ONE_MONTH("1 month", Duration.ofDays(30)),
    ONE_WEEK("1 week", Duration.ofDays(7)),
    ONE_DAY("1 day", Duration.ofDays(1)),
    ONE_HOUR("1 hour", Duration.ofHours(1));

    private final String label;
    private final Duration duration;

    MockExpirationOption(String label, Duration duration) {
        this.label = label;
        this.duration = duration;
    }

    public String getLabel() {
        return label;
    }

    public Duration getDuration() {
        return duration;
    }

    public static List<MockExpirationOption> formOptions() {
        return List.of(ONE_MONTH, ONE_WEEK, ONE_DAY, ONE_HOUR);
    }

    public static Optional<MockExpirationOption> fromValue(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        for (MockExpirationOption option : values()) {
            if (option.name().equals(normalized)) {
                return Optional.of(option);
            }
        }
        return Optional.empty();
    }
}
