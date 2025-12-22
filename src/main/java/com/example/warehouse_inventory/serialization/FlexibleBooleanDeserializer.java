package com.example.warehouse_inventory.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.io.IOException;
import java.util.Locale;

public class FlexibleBooleanDeserializer extends JsonDeserializer<Boolean> {
    @Override
    public Boolean deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String raw = parser.getValueAsString();
        if (raw == null) {
            return null;
        }

        String value = raw.trim().toLowerCase(Locale.ROOT);
        return switch (value) {
            case "true", "yes", "1" -> true;
            case "false", "no", "0" -> false;
            default -> throw new InvalidFormatException(parser, "Invalid boolean value", raw, Boolean.class);
        };
    }
}
