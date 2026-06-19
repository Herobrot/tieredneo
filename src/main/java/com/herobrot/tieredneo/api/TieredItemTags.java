package com.herobrot.tieredneo.api;

import com.herobrot.tieredneo.TieredNeo;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class TieredItemTags {
    public static final TagKey<Item> MODIFIER_RESTRICTED = tag("modifier_restricted");
    public static final TagKey<Item> REFORGE_ADDITION = tag("reforge_addition");
    public static final TagKey<Item> REFORGE_BASE_ITEM = tag("reforge_base_item");
    public static final TagKey<Item> MAIN_OFFHAND_ITEM = tag("main_offhand_item");

    private TieredItemTags() {}

    private static TagKey<Item> tag(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(TieredNeo.MODID, path));
    }
}