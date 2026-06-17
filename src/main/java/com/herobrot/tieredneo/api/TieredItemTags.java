package com.herobrot.tieredneo.api;

import com.herobrot.tieredneo.TieredNeo;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * All item tag keys used by this mod.

 * Tags are data-driven — the actual item lists live in:
 *   src/main/resources/data/tieredneo/tags/item/*.json

 * These TagKey constants are used in code to check membership.
 */
public final class TieredItemTags {

    /** Items that should never receive a tier modifier (e.g. creative-only items). */
    public static final TagKey<Item> MODIFIER_RESTRICTED = tag("modifier_restricted");

    /** Items that can be placed in the reforge "catalyst" slot (slot 2). */
    public static final TagKey<Item> REFORGE_ADDITION = tag("reforge_addition");

    /**
     * Fallback base items accepted in the reforge "base material" slot (slot 0)
     * when no custom reforge_items datapack entry exists for that item type.
     */
    public static final TagKey<Item> REFORGE_BASE_ITEM = tag("reforge_base_item");

    /**
     * Items that can be used in either MAINHAND or OFFHAND
     * (shields, bows, crossbows, and similar ranged/dual-wielded items).
     */
    public static final TagKey<Item> MAIN_OFFHAND_ITEM = tag("main_offhand_item");

    // -------------------------------------------------------------------------

    private TieredItemTags() {}

    private static TagKey<Item> tag(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(TieredNeo.MODID, path));
    }
}