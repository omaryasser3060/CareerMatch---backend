package com.example.backend.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public final class DateUtils {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    private DateUtils() {
        // Utility class - prevent instantiation
    }

    // ============================================================
    // Format / Parse
    // ============================================================

    public static String format(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(ISO_FORMATTER);
    }

    public static String formatForDisplay(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(DISPLAY_FORMATTER);
    }

    public static LocalDateTime parse(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeStr, ISO_FORMATTER);
    }

    // ============================================================
    // Now
    // ============================================================

    public static LocalDateTime now() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    // ============================================================
    // Relative Dates
    // ============================================================

    public static LocalDateTime daysAgo(int days) {
        return now().minusDays(days);
    }

    public static LocalDateTime hoursAgo(int hours) {
        return now().minusHours(hours);
    }

    public static LocalDateTime minutesAgo(int minutes) {
        return now().minusMinutes(minutes);
    }

    public static LocalDateTime daysFromNow(int days) {
        return now().plusDays(days);
    }

    public static LocalDateTime hoursFromNow(int hours) {
        return now().plusHours(hours);
    }

    // ============================================================
    // Comparison
    // ============================================================

    public static boolean isAfter(LocalDateTime dateTime, LocalDateTime reference) {
        return dateTime != null && reference != null && dateTime.isAfter(reference);
    }

    public static boolean isBefore(LocalDateTime dateTime, LocalDateTime reference) {
        return dateTime != null && reference != null && dateTime.isBefore(reference);
    }

    public static boolean isBetween(LocalDateTime dateTime, LocalDateTime start, LocalDateTime end) {
        return dateTime != null && start != null && end != null
                && !dateTime.isBefore(start) && !dateTime.isAfter(end);
    }

    // ============================================================
    // Duration
    // ============================================================

    public static long daysBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(start, end);
    }

    public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.HOURS.between(start, end);
    }

    // ============================================================
    // Instant Conversion
    // ============================================================

    public static Instant toInstant(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.toInstant(ZoneOffset.UTC);
    }

    public static LocalDateTime fromInstant(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}