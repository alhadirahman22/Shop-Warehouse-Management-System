package com.example.warehouse_inventory.mapper;

import com.example.warehouse_inventory.dto.StockResponse;
import com.example.warehouse_inventory.entity.Stock;
import com.example.warehouse_inventory.entity.Variant;
import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class StockMapper {
    private StockMapper() {
    }

    public static StockResponse toResponse(Stock stock) {
        Variant variant = stock.getVariant();
        String sku = variant != null ? variant.getSku() : null;
        String variantName = variant != null ? variant.getVariantName() : null;
        JsonNode attributes = variant != null ? VariantMapper.parseAttributes(variant.getAttributes()) : null;
        String price = variant != null ? formatPrice(variant.getPrice()) : null;
        return new StockResponse(
                stock.getVariantId(),
                sku,
                stock.getQuantity(),
                stock.getUpdatedAt(),
                price,
                attributes, variantName);
    }

    private static String formatPrice(BigDecimal price) {
        if (price == null) {
            return null;
        }
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("en", "SG"));
        DecimalFormat formatter = new DecimalFormat("#,##0.00", symbols);
        return formatter.format(price);
    }
}
