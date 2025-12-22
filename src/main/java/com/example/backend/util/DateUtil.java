package com.example.backend.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateUtil {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private DateUtil() {
    }

    public static String formatIso(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return ISO.format(dateTime);
    }

    public static String nowIso() {
        return ISO.format(LocalDateTime.now());
    }
}
