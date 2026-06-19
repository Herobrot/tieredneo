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

public class AttributeDataLoader extends SimplePreparableReloadListener<Map<ResourceLocation, PotentialAttribute>> {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping()

            .registerTypeAdapter(ItemVerifier.class, new ItemVerifierDeserializer()).registerTypeAdapter(AttributeModifier.class, new AttributeModifierDeserializer()).registerTypeAdapter(EquipmentSlot.class, new EquipmentSlotDeserializer()).registerTypeAdapter(Style.class, new StyleDeserializer())

            .registerTypeAdapter(AttributeModifier.class, new AttributeModifierSerializer()).registerTypeAdapter(EquipmentSlot.class, new EquipmentSlotSerializer()).registerTypeAdapter(Style.class, new StyleSerializer()).create();
    private static final String DATA_FOLDER = "item_attributes";
    private volatile Map<ResourceLocation, PotentialAttribute> itemAttributes = Map.of();

    @Override
    protected @NotNull Map<ResourceLocation, PotentialAttribute> prepare(ResourceManager manager,
                                                                         @NotNull ProfilerFiller profiler) {
        Map<ResourceLocation, PotentialAttribute> result = new HashMap<>();

        manager.listResources(DATA_FOLDER, id -> id.getPath().endsWith(".json")).forEach((fileId, resource) -> {
            try (InputStreamReader reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {

                PotentialAttribute attribute = GSON.fromJson(reader, PotentialAttribute.class);

                if (attribute == null || attribute.getID() == null) {
                    TieredNeo.LOGGER.warn("[TieredNeo] Skipping {}: parsed to null or missing 'id' field.", fileId);
                    return;
                }

                ResourceLocation attributeId = ResourceLocation.tryParse(attribute.getID());
                if (attributeId == null) {
                    TieredNeo.LOGGER.warn("[TieredNeo] Skipping {}: 'id' field '{}' is not a valid ResourceLocation."
                            , fileId, attribute.getID());
                    return;
                }

                result.put(attributeId, attribute);

            } catch (Exception e) {
                TieredNeo.LOGGER.error("[TieredNeo] Failed to parse item_attributes file {}: {}", fileId,
                        e.getMessage());
            }
        });

        TieredNeo.LOGGER.info("[TieredNeo] Prepared {} item attributes from datapacks.", result.size());
        return result;
    }

    @Override
    protected void apply(@NotNull Map<ResourceLocation, PotentialAttribute> prepared,
                         @NotNull ResourceManager manager, @NotNull ProfilerFiller profiler) {
        this.itemAttributes = Map.copyOf(prepared);
        TieredNeo.LOGGER.info("[TieredNeo] Applied {} item attributes.", this.itemAttributes.size());
    }

    public Map<ResourceLocation, PotentialAttribute> getItemAttributes() {
        return itemAttributes;
    }

    public void applyClientSync(Map<ResourceLocation, PotentialAttribute> incoming) {
        this.itemAttributes = Map.copyOf(incoming);
    }
}