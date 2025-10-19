package io.xcodebn.zounadminspring.web;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Utility for converting String form inputs to various Java types.
 * Handles primitives, wrappers, dates, enums, and more.
 */
@Component
public class TypeConverter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Convert a string value to the target type.
     */
    public Object convert(String value, Class<?> targetType) {
        if (value == null || value.isBlank()) {
            return null;
        }

        value = value.trim();

        // Handle primitives and common wrappers
        if (targetType == String.class) {
            return value;
        }

        if (targetType == Integer.class || targetType == int.class) {
            return Integer.parseInt(value);
        }

        if (targetType == Long.class || targetType == long.class) {
            return Long.parseLong(value);
        }

        if (targetType == Double.class || targetType == double.class) {
            return Double.parseDouble(value);
        }

        if (targetType == Float.class || targetType == float.class) {
            return Float.parseFloat(value);
        }

        if (targetType == Boolean.class || targetType == boolean.class) {
            return Boolean.parseBoolean(value) || value.equalsIgnoreCase("on") || value.equals("1");
        }

        if (targetType == BigDecimal.class) {
            return new BigDecimal(value);
        }

        // Date types
        if (targetType == LocalDate.class) {
            return LocalDate.parse(value, DATE_FORMATTER);
        }

        if (targetType == LocalDateTime.class) {
            return LocalDateTime.parse(value, DATETIME_FORMATTER);
        }

        if (targetType == Date.class) {
            // Convert from LocalDate
            LocalDate localDate = LocalDate.parse(value, DATE_FORMATTER);
            return java.sql.Date.valueOf(localDate);
        }

        if (targetType == java.sql.Date.class) {
            LocalDate localDate = LocalDate.parse(value, DATE_FORMATTER);
            return java.sql.Date.valueOf(localDate);
        }

        if (targetType == java.sql.Timestamp.class) {
            LocalDateTime localDateTime = LocalDateTime.parse(value, DATETIME_FORMATTER);
            return java.sql.Timestamp.valueOf(localDateTime);
        }

        // Enums
        if (targetType.isEnum()) {
            return convertToEnum(value, targetType);
        }

        throw new IllegalArgumentException("Cannot convert value '" + value + "' to type " + targetType.getName());
    }

    /**
     * Convert a string to an enum constant.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object convertToEnum(String value, Class<?> enumClass) {
        return Enum.valueOf((Class<Enum>) enumClass, value);
    }

    /**
     * Check if a value can be converted to the target type.
     */
    public boolean canConvert(Class<?> targetType) {
        return targetType == String.class ||
               targetType == Integer.class || targetType == int.class ||
               targetType == Long.class || targetType == long.class ||
               targetType == Double.class || targetType == double.class ||
               targetType == Float.class || targetType == float.class ||
               targetType == Boolean.class || targetType == boolean.class ||
               targetType == BigDecimal.class ||
               targetType == LocalDate.class ||
               targetType == LocalDateTime.class ||
               targetType == Date.class ||
               targetType == java.sql.Date.class ||
               targetType == java.sql.Timestamp.class ||
               targetType.isEnum();
    }
}
