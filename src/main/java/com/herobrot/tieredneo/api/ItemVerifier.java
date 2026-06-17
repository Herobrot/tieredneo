package com.herobrot.tieredneo.api;

import com.google.gson.annotations.SerializedName;
import com.herobrot.tieredneo.TieredNeo;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ItemVerifier {

    @SerializedName("type")
    private String type;

    @SerializedName("value")
    private String value;

    public ItemVerifier() {}

    public ItemVerifier(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public boolean isValid(Item item) {
        if (type == null || value == null || item == null) return false;

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);

        return switch (type.toLowerCase()) {
            case "item" -> itemId.toString().equals(value);

            case "tag" -> {
                ResourceLocation tagRL = ResourceLocation.tryParse(value);
                if (tagRL == null) {
                    TieredNeo.LOGGER.warn(
                            "[TieredNeo] ItemVerifier: malformed tag ResourceLocation '{}'", value);
                    yield false;
                }
                TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tagRL);
                yield BuiltInRegistries.ITEM.wrapAsHolder(item).is(tagKey);
            }

            case "namespace" -> itemId.getNamespace().equals(value);

            case "contains" -> itemId.getPath().contains(value);

            case "startswith" -> itemId.toString().startsWith(value);

            default -> {
                TieredNeo.LOGGER.warn(
                        "[TieredNeo] ItemVerifier: unknown type '{}' (value: '{}')", type, value);
                yield false;
            }
        };
    }

    public String getType()  { return type; }
    public String getValue() { return value; }
}