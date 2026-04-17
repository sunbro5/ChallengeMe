package org.jan.game;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Stores a {@code List<String>} as a single newline-delimited TEXT column.
 * Keeps the entity schema simple — no extra join tables needed.
 */
@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {

    private static final String SEP = "\n";

    @Override
    public String convertToDatabaseColumn(List<String> list) {
        if (list == null || list.isEmpty()) return "";
        return String.join(SEP, list);
    }

    @Override
    public List<String> convertToEntityAttribute(String value) {
        if (value == null || value.isBlank()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(value.split(SEP, -1)));
    }
}
