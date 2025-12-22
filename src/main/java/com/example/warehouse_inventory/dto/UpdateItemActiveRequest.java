package com.example.warehouse_inventory.dto;

import com.example.warehouse_inventory.serialization.FlexibleBooleanDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotNull;

public record UpdateItemActiveRequest(
        @NotNull(message = "Active is required")
        @JsonDeserialize(using = FlexibleBooleanDeserializer.class)
        Boolean active
) {
}
