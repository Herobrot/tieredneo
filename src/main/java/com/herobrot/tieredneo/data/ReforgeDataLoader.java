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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads data/<namespace>/reforge_items/*.json from datapacks.

 * JSON format (TieredZ-compatible):
 * {
 *   "items": ["minecraft:diamond_sword"],
 *   "base":  ["minecraft:diamond"]
 * }

 * Uses SimpleJsonResourceReloadListener — NeoForge handles file scanning,
 * JSON parsing and I/O errors internally, giving us a clean
 * Map<ResourceLocation, JsonElement> to work with in apply().

 * Network sync:
 *   getRawMap()  — called by ReforgeItemSyncPayload.fromLoader() on the server
 *   setRawMap()  — called by TieredClientPacketHandler.handleReforgeItemSync() on the client
 */
public class ReforgeDataLoader extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new Gson();

    private volatile Map<Item, List<Item>> reforgeBaseMap = Map.of();

    public ReforgeDataLoader() {
        super(GSON, "reforge_items");
    }

    @Override
    protected void apply(@NotNull Map<ResourceLocation, JsonElement> objectMap,
                         @NotNull ResourceManager resourceManager,
                         @NotNull ProfilerFiller profiler) {

        Map<Item, List<Item>> newMap = new HashMap<>();

        objectMap.forEach((fileId, element) -> {
            try {
                JsonObject data = element.getAsJsonObject();
                List<Item> baseItems = new ArrayList<>();

                // Parse base materials first — shared across all items in this file
                if (data.has("base")) {
                    data.getAsJsonArray("base").forEach(b -> {
                        ResourceLocation rl = ResourceLocation.tryParse(b.getAsString());
                        // containsKey() avoids the Items.AIR comparison hack
                        if (rl != null && BuiltInRegistries.ITEM.containsKey(rl)) {
                            baseItems.add(BuiltInRegistries.ITEM.get(rl));
                        } else {
                            TieredNeo.LOGGER.warn("[TieredNeo] Base material '{}' not found, skipping in {}.",
                                    b.getAsString(), fileId);
                        }
                    });
                }

                // Map each target item to the base list
                if (data.has("items")) {
                    data.getAsJsonArray("items").forEach(i -> {
                        ResourceLocation rl = ResourceLocation.tryParse(i.getAsString());
                        if (rl != null && BuiltInRegistries.ITEM.containsKey(rl)) {
                            // computeIfAbsent allows multiple files to contribute to the same item
                            newMap.computeIfAbsent(BuiltInRegistries.ITEM.get(rl),
                                    k -> new ArrayList<>()).addAll(baseItems);
                        } else {
                            TieredNeo.LOGGER.warn("[TieredNeo] Target item '{}' not found, skipping in {}.",
                                    i.getAsString(), fileId);
                        }
                    });
                }

            } catch (Exception e) {
                TieredNeo.LOGGER.error("[TieredNeo] Failed to parse reforge_items file {}: {}", fileId, e);
            }
        });

        // Atomic swap — readers always see a complete consistent map
        this.reforgeBaseMap = Map.copyOf(newMap);
        TieredNeo.LOGGER.info("[TieredNeo] Loaded {} reforge mappings.", this.reforgeBaseMap.size());
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    public List<Item> getReforgeBaseItems(Item item) {
        return reforgeBaseMap.getOrDefault(item, List.of());
    }

    /** Used by ReforgeItemSyncPayload.fromLoader() to send data to clients. */
    public Map<Item, List<Item>> getRawMap() {
        return reforgeBaseMap;
    }

    /**
     * Replaces the map with client-received sync data.
     * Called on the client main thread by TieredClientPacketHandler.
     */
    public void setRawMap(Map<Item, List<Item>> incoming) {
        this.reforgeBaseMap = Map.copyOf(incoming);
    }
}