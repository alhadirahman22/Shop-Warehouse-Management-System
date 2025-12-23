package com.example.warehouse_inventory.seed;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.example.warehouse_inventory.dto.ItemResponse;
import com.example.warehouse_inventory.dto.VariantResponse;
import com.example.warehouse_inventory.repository.ItemRepository;
import com.example.warehouse_inventory.repository.VariantRepository;
import com.example.warehouse_inventory.service.InventoryService;
import com.example.warehouse_inventory.service.ItemService;
import com.example.warehouse_inventory.service.VariantService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import lombok.RequiredArgsConstructor;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataSeeder implements CommandLineRunner {
    private final ItemService itemService;
    private final VariantService variantService;
    private final InventoryService inventoryService;
    private final ItemRepository itemRepository;
    private final VariantRepository variantRepository;

    @Override
    public void run(String... args) {
        if (itemRepository.count() > 0 || variantRepository.count() > 0) {
            return;
        }

        DummyDataFactory factory = new DummyDataFactory();
        Random random = new Random(42L);

        List<String> brands = List.of("Belden", "AMP", "TP-Link", "Mikrotik", "Cisco");
        List<String> colors = List.of("black", "blue", "white", "red");
        List<String> sizes = List.of("S", "M", "L", "XL");
        List<String> lengths = List.of("1m", "2m", "3m", "5m", "10m");

        int itemCount = 50;
        int targetVariants = 220;
        int variantsPerItem = targetVariants / itemCount;
        int extraVariants = targetVariants % itemCount;

        List<ItemResponse> items = new ArrayList<>();
        for (int i = 1; i <= itemCount; i++) {
            String name = String.format("Item %03d", i);
            String description = "Dummy item " + i;
            items.add(itemService.create(factory.item(name, description, true)));
        }

        List<VariantResponse> variants = new ArrayList<>();
        int variantCounter = 0;
        for (int i = 0; i < items.size(); i++) {
            ItemResponse item = items.get(i);
            int count = variantsPerItem + (i < extraVariants ? 1 : 0);
            for (int j = 0; j < count; j++) {
                variantCounter++;
                Map<String, String> attributes = new LinkedHashMap<>();
                attributes.put("brand", brands.get(random.nextInt(brands.size())));
                attributes.put("color", colors.get(random.nextInt(colors.size())));
                attributes.put("size", sizes.get(random.nextInt(sizes.size())));
                attributes.put("length", lengths.get(random.nextInt(lengths.size())));

                String variantName = String.format("%s Variant %03d", item.name(), variantCounter);
                BigDecimal price = new BigDecimal(10000 + (variantCounter % 50) * 1000);
                VariantResponse variant = variantService.create(factory.variant(
                        item.id(),
                        variantName,
                        attributes,
                        price,
                        true));
                variants.add(variant);
            }
        }

        int refIndex = 1000;
        for (VariantResponse variant : variants) {
            int baseQty = 5 + random.nextInt(46);
            inventoryService.stockIn(factory.stockIn(
                    variant.id(),
                    baseQty,
                    "PO-" + (++refIndex)));
            int currentQty = baseQty;

            if (currentQty > 0 && random.nextBoolean()) {
                int outQty = 1 + random.nextInt(Math.max(1, Math.min(5, currentQty)));
                inventoryService.stockOut(factory.stockOut(
                        variant.id(),
                        outQty,
                        "ORDER-" + (++refIndex)));
                currentQty -= outQty;
            }

            if (random.nextBoolean()) {
                int adjust = random.nextInt(5) - 2;
                if (adjust != 0 && currentQty + adjust >= 0) {
                    inventoryService.stockAdjust(factory.stockAdjust(
                            variant.id(),
                            adjust,
                            "ADJ-" + (++refIndex)));
                }
            }
        }
    }
}
