package com.herobrot.tieredneo.api;

import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;

import java.util.List;

/**
 * Represents a named attribute configuration loaded from a datapack JSON.

 * Each PotentialAttribute corresponds to one JSON file under:
 *   data/<namespace>/item_attributes/<name>.json

 * It stores:
 *  - id       : the fully-qualified ID (e.g. "tieredneo:sword_legendary")
 *  - verifiers: list of ItemVerifier rules — if ANY verifier matches an item,
 *               this attribute is eligible for that item
 *  - weight   : probability weight used in the weighted draw (higher = more common)
 *               0 means the attribute can only be assigned via reforge or command
 *  - style    : the text Style (color, bold, italic…) shown in tooltips
 *  - attributes: the list of AttributeTemplate entries that are actually applied

 * Gson deserializes this directly from JSON via AttributeDataLoader.
 * Fields use @SerializedName annotations in the original; here we match the
 * JSON key names exactly as the datapack schema expects them so existing
 * TieredZ datapacks remain compatible without modification.
 */
public class PotentialAttribute {

    private final String id;
    private final List<ItemVerifier> verifiers;
    private final int weight;
    private final Style style;
    private final List<AttributeTemplate> attributes;

    // Gson needs this path (direct field injection), no @SerializedName needed
    // as long as JSON keys match the field names exactly.
    public PotentialAttribute(String id,
                              List<ItemVerifier> verifiers,
                              int weight,
                              Style style,
                              List<AttributeTemplate> attributes) {
        this.id = id;
        this.verifiers = verifiers;
        this.weight = weight;
        this.style = style;
        this.attributes = attributes;
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public String getID() { return id; }

    public List<ItemVerifier> getVerifiers() { return verifiers; }

    public int getWeight() { return weight; }

    public Style getStyle() { return style; }

    public List<AttributeTemplate> getAttributes() { return attributes; }

    /**
     * Returns true if ANY of this attribute's verifiers accepts the given item.

     * Accepts an Item instance (not a ResourceLocation) because the "tag"
     * verifier type requires Holder.is(TagKey), which is only accessible
     * through the Item's built-in registry holder.
     *
     * @param item the Item to test (never null)
     */
    public boolean isValid(Item item) {
        if (verifiers == null || verifiers.isEmpty()) return false;
        for (ItemVerifier verifier : verifiers) {
            if (verifier.isValid(item)) return true;
        }
        return false;
    }
}