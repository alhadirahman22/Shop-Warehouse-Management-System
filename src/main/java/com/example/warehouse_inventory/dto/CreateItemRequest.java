package com.example.warehouse_inventory.dto;

import com.example.warehouse_inventory.serialization.FlexibleBooleanDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateItemRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 150, message = "Name max 150 chars")
        String name,

        @Size(max = 500, message = "Description max 500 chars")
        String description,

        @JsonDeserialize(using = FlexibleBooleanDeserializer.class)
        Boolean active
) {
}
