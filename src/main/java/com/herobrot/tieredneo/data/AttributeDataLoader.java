package com.herobrot.tieredneo.data;

import com.google.gson.*;
import com.herobrot.tieredneo.TieredNeo;
import com.herobrot.tieredneo.api.ItemVerifier;
import com.herobrot.tieredneo.api.PotentialAttribute;
import com.herobrot.tieredneo.gson.AttributeModifierDeserializer;
import com.herobrot.tieredneo.gson.AttributeModifierSerializer;
import com.herobrot.tieredneo.gson.EquipmentSlotDeserializer;
import com.herobrot.tieredneo.gson.EquipmentSlotSerializer;
import com.herobrot.tieredneo.gson.ItemVerifierDeserializer;
import com.herobrot.tieredneo.gson.StyleDeserializer;
import com.herobrot.tieredneo.gson.StyleSerializer;

import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.NotNull;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads and parses all item_attributes datapack files.

 * Source path: data/<namespace>/item_attributes/<name>.json

 * Uses SimplePreparableReloadListener so prepare() runs on a worker thread
 * and apply() runs on the main server thread — the same pattern as NeoForge's
 * own JsonDataLoader but without the intermediate Map<ResourceLocation, JsonElement>
 * step, giving us direct control over error handling per-file.

 * GSON instance is package-scoped so AttributeSyncPayload can reuse it
 * for the binary fallback serialization if needed, but the primary sync
 * path uses the StreamCodec defined in TierDataComponent.

 * Thread safety:
 *   After apply() completes, itemAttributes is replaced with Map.copyOf()
 *   producing an immutable snapshot. Concurrent readers (ItemAttributeModifierEvent,
 *   ModifierUtils) always see a consistent complete map, never a partially-written one.
 */
public class AttributeDataLoader extends SimplePreparableReloadListener<Map<ResourceLocation, PotentialAttribute>> {

    static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            // Deserializers — read datapacks from disk
            .registerTypeAdapter(ItemVerifier.class,      new ItemVerifierDeserializer())
            .registerTypeAdapter(AttributeModifier.class, new AttributeModifierDeserializer())
            .registerTypeAdapter(EquipmentSlot.class,     new EquipmentSlotDeserializer())
            .registerTypeAdapter(Style.class,             new StyleDeserializer())
            // Serializers — write attributes back to JSON for AttributeSyncPayload S2C
            .registerTypeAdapter(AttributeModifier.class, new AttributeModifierSerializer())
            .registerTypeAdapter(EquipmentSlot.class,     new EquipmentSlotSerializer())
            .registerTypeAdapter(Style.class,             new StyleSerializer())
            .create();

    private static final String DATA_FOLDER = "item_attributes";

    /** Live attribute map. Replaced atomically on each reload. */
    private volatile Map<ResourceLocation, PotentialAttribute> itemAttributes = Map.of();

    // -------------------------------------------------------------------------
    // Reload lifecycle
    // -------------------------------------------------------------------------

    /**
     * Runs on a worker thread. Scans all datapacks for item_attributes/*.json,
     * parses each one, and returns a mutable map. Errors per-file are logged
     * and skipped so one bad datapack file doesn't abort the entire load.
     */
    @Override
    protected @NotNull Map<ResourceLocation, PotentialAttribute> prepare(ResourceManager manager,
                                                                         @NotNull ProfilerFiller profiler) {
        Map<ResourceLocation, PotentialAttribute> result = new HashMap<>();

        manager.listResources(DATA_FOLDER, id -> id.getPath().endsWith(".json"))
                .forEach((fileId, resource) -> {
                    try (InputStreamReader reader = new InputStreamReader(
                            resource.open(), StandardCharsets.UTF_8)) {

                        PotentialAttribute attribute = GSON.fromJson(reader, PotentialAttribute.class);

                        if (attribute == null || attribute.getID() == null) {
                            TieredNeo.LOGGER.warn("[TieredNeo] Skipping {}: parsed to null or missing 'id' field.", fileId);
                            return;
                        }

                        ResourceLocation attributeId = ResourceLocation.tryParse(attribute.getID());
                        if (attributeId == null) {
                            TieredNeo.LOGGER.warn("[TieredNeo] Skipping {}: 'id' field '{}' is not a valid ResourceLocation.",
                                    fileId, attribute.getID());
                            return;
                        }

                        result.put(attributeId, attribute);

                    } catch (Exception e) {
                        TieredNeo.LOGGER.error("[TieredNeo] Failed to parse item_attributes file {}: {}",
                                fileId, e.getMessage());
                    }
                });

        TieredNeo.LOGGER.info("[TieredNeo] Prepared {} item attributes from datapacks.", result.size());
        return result;
    }

    /**
     * Runs on the main server thread. Replaces the live map with an immutable
     * snapshot so all subsequent reads are safe without synchronization.
     */
    @Override
    protected void apply(@NotNull Map<ResourceLocation, PotentialAttribute> prepared,
                         @NotNull ResourceManager manager,
                         @NotNull ProfilerFiller profiler) {
        this.itemAttributes = Map.copyOf(prepared);
        TieredNeo.LOGGER.info("[TieredNeo] Applied {} item attributes.", this.itemAttributes.size());
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Returns the current immutable attribute map.
     * Safe to call from any thread after the first datapack load completes.
     */
    public Map<ResourceLocation, PotentialAttribute> getItemAttributes() {
        return itemAttributes;
    }

    /**
     * Replaces the attribute map with data received from the server.
     * Called on the client main thread by TieredClientPacketHandler.handleAttributeSync().

     * Only used on the logical client. The server always populates this map
     * through prepare() + apply() via the datapack reload pipeline.
     */
    public void applyClientSync(Map<ResourceLocation, PotentialAttribute> incoming) {
        this.itemAttributes = Map.copyOf(incoming);
    }
}