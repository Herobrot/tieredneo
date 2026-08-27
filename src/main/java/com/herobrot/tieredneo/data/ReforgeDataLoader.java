package com.herobrot.tieredneo.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.herobrot.tieredneo.TieredNeo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReforgeDataLoader extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();
    private volatile Map<Item, List<Item>> reforgeBaseMap = Map.of();

    public ReforgeDataLoader() { super(GSON, "reforge_items"); }

    @Override
    protected void apply(@NotNull Map<ResourceLocation, JsonElement> objectMap, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        Map<Item, List<Item>> newMap = new java.util.HashMap<>();
        objectMap.forEach((fileId, element) -> {
            try {
                JsonObject data = element.getAsJsonObject();
                List<Item> baseItems = new java.util.ArrayList<>();
                if (data.has("base"))
                    data.getAsJsonArray("base").forEach(b -> {
                        ResourceLocation rl = ResourceLocation.tryParse(b.getAsString());
                        if (rl != null && BuiltInRegistries.ITEM.containsKey(rl))
                            baseItems.add(BuiltInRegistries.ITEM.get(rl));
                        else TieredNeo.LOGGER.warn("[TieredNeo]: Base material '{}' not found, skipping in {}.",
                                    b.getAsString(), fileId);
                    });

                if (data.has("items"))
                    data.getAsJsonArray("items").forEach(i -> {
                        ResourceLocation rl = ResourceLocation.tryParse(i.getAsString());
                        if (rl != null && BuiltInRegistries.ITEM.containsKey(rl))
                            newMap.computeIfAbsent(BuiltInRegistries.ITEM.get(rl), k -> new java.util.ArrayList<>()).addAll(baseItems);
                        else TieredNeo.LOGGER.warn("[TieredNeo]: Target item '{}' not found, skipping in {}.",
                                    i.getAsString(), fileId);
                    });

            } catch (Exception e) {
                TieredNeo.LOGGER.error("[TieredNeo]: Failed to parse reforge_items file {}", fileId, e);
            }
        });

        this.reforgeBaseMap = newMap.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        e -> List.copyOf(e.getValue())
                ));

        TieredNeo.LOGGER.info("[TieredNeo]: Loaded {} reforge mappings.", this.reforgeBaseMap.size());
    }

    public List<Item> getReforgeBaseItems(Item item) {
        return reforgeBaseMap.getOrDefault(item, List.of());
    }

    public Map<Item, List<Item>> getRawMap() {
        return reforgeBaseMap;
    }

    public void setRawMap(Map<Item, List<Item>> incoming) {
        this.reforgeBaseMap = incoming.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        e -> List.copyOf(e.getValue())
                ));
    }
}