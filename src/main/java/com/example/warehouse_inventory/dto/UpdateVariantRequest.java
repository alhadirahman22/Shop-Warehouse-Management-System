package com.example.warehouse_inventory.dto;

import com.example.warehouse_inventory.serialization.FlexibleBooleanDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record UpdateVariantRequest(
        @NotBlank(message = "Variant name is required")
        @Size(max = 120, message = "Variant name max 120 chars")
        String variantName,

        @Schema(
                description = "JSON string for variant attributes (allowed keys: size, color, material, brand, weight, length, width, height)",
                example = "{\"size\":\"L\",\"color\":\"red\"}")
        String attributes,

        @NotNull(message = "Price is required")
        BigDecimal price,

        @NotNull(message = "Active is required")
        @JsonDeserialize(using = FlexibleBooleanDeserializer.class)
        Boolean active
) {
}
