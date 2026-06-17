package com.herobrot.tieredneo.api;

import com.google.gson.annotations.SerializedName;
import com.herobrot.tieredneo.TieredNeo;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

import java.util.Optional;

/**
 * A single attribute modifier template applied when an ItemStack receives a tier.

 * JSON schema (TieredZ datapack-compatible):
 * {
 *   "type": "minecraft:generic.attack_damage",  // or bare "generic.attack_damage"
 *   "modifier": { "name": "...", "amount": 0.1, "operation": "ADD_MULTIPLIED_TOTAL" },
 *   "optional_equipment_slots": ["MAINHAND", "OFFHAND"]
 * }

 * Two issues found in real TieredZ datapacks and addressed here:

 * 1. Bare attribute types without namespace (e.g. "generic.attack_damage"):
 *    ResourceLocation.tryParse() returns null for these. We normalize them to
 *    "minecraft:<type>" before parsing, matching TieredZ's original behavior.

 * 2. Multiple slots in optional_equipment_slots (e.g. ["MAINHAND", "OFFHAND"]):
 *    resolveSlotGroup() now maps common combinations to their EquipmentSlotGroup
 *    equivalent (HAND for mainhand+offhand, ARMOR for all armor slots, etc.)
 *    instead of always taking the first element.
 */
public class AttributeTemplate {

    @SerializedName("type")
    private final String attributeTypeID;

    @SerializedName("modifier")
    private final AttributeModifier attributeModifier;

    @SerializedName("required_equipment_slots")
    private final EquipmentSlot[] requiredEquipmentSlots;

    @SerializedName("optional_equipment_slots")
    private final EquipmentSlot[] optionalEquipmentSlots;

    public AttributeTemplate(String attributeTypeID,
                             AttributeModifier attributeModifier,
                             EquipmentSlot[] requiredEquipmentSlots,
                             EquipmentSlot[] optionalEquipmentSlots) {
        this.attributeTypeID = attributeTypeID;
        this.attributeModifier = attributeModifier;
        this.requiredEquipmentSlots = requiredEquipmentSlots;
        this.optionalEquipmentSlots = optionalEquipmentSlots;
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public String getAttributeTypeID() { return attributeTypeID; }

    public AttributeModifier getEntityAttributeModifier() { return attributeModifier; }

    public EquipmentSlot[] getRequiredEquipmentSlots() { return requiredEquipmentSlots; }

    public EquipmentSlot[] getOptionalEquipmentSlots() { return optionalEquipmentSlots; }

    // -------------------------------------------------------------------------
    // Slot group resolution
    // -------------------------------------------------------------------------

    /**
     * Derives the best-fit EquipmentSlotGroup for this template's slot array.

     * Strategy:
     *  1. Prefer required_equipment_slots if present.
     *  2. Fall back to optional_equipment_slots.
     *  3. Map common combinations to their group constants.
     *  4. Default to ANY if nothing is specified.

     * Common mappings from TieredZ datapacks:
     *   [MAINHAND]                         → MAINHAND
     *   [OFFHAND]                          → OFFHAND
     *   [MAINHAND, OFFHAND]                → HAND
     *   [HEAD]                             → HEAD
     *   [CHEST]                            → CHEST
     *   [LEGS]                             → LEGS
     *   [FEET]                             → FEET
     *   [HEAD, CHEST, LEGS, FEET]          → ARMOR
     *   [HEAD, CHEST, LEGS, FEET, MAINHAND, OFFHAND] → ANY
     *   anything else with 1 slot         → bySlot(slot)
     *   anything else multi-slot          → ANY (safe fallback)
     */
    public EquipmentSlotGroup resolveSlotGroup() {
        EquipmentSlot[] slots = effectiveSlots();
        if (slots == null || slots.length == 0) return EquipmentSlotGroup.ANY;

        if (slots.length == 1) {
            return EquipmentSlotGroup.bySlot(slots[0]);
        }

        // Build a bitmask of which slots are present for pattern matching
        boolean hasMain   = contains(slots, EquipmentSlot.MAINHAND);
        boolean hasOff    = contains(slots, EquipmentSlot.OFFHAND);
        boolean hasHead   = contains(slots, EquipmentSlot.HEAD);
        boolean hasChest  = contains(slots, EquipmentSlot.CHEST);
        boolean hasLegs   = contains(slots, EquipmentSlot.LEGS);
        boolean hasFeet   = contains(slots, EquipmentSlot.FEET);

        // [MAINHAND, OFFHAND] → HAND
        if (hasMain && hasOff && !hasHead && !hasChest && !hasLegs && !hasFeet) {
            return EquipmentSlotGroup.HAND;
        }

        // [HEAD, CHEST, LEGS, FEET] → ARMOR
        if (hasHead && hasChest && hasLegs && hasFeet && !hasMain && !hasOff) {
            return EquipmentSlotGroup.ARMOR;
        }

        // All six → ANY
        if (hasMain && hasOff && hasHead && hasChest && hasLegs && hasFeet) {
            return EquipmentSlotGroup.ANY;
        }

        // Unknown multi-slot pattern — ANY is the safe fallback
        TieredNeo.LOGGER.debug("[TieredNeo] Unrecognized slot combination in '{}', defaulting to ANY.",
                attributeTypeID);
        return EquipmentSlotGroup.ANY;
    }

    // -------------------------------------------------------------------------
    // Application via ItemAttributeModifierEvent
    // -------------------------------------------------------------------------

    /**
     * Resolves the Attribute and adds the modifier to the event.

     * Namespace normalization:
     *   Bare type strings like "generic.attack_damage" (no colon) are prefixed
     *   with "minecraft:" before parsing, matching TieredZ's original behavior
     *   where the Fabric registry defaulted to the minecraft namespace.
     */
    public void applyModifiersToEvent(ItemAttributeModifierEvent event,
                                      EquipmentSlotGroup slotGroup) {
        if (attributeTypeID == null || attributeModifier == null) return;

        String normalizedId = attributeTypeID.contains(":")
                ? attributeTypeID
                : "minecraft:" + attributeTypeID;

        ResourceLocation attributeRL = ResourceLocation.tryParse(normalizedId);
        if (attributeRL == null) {
            TieredNeo.LOGGER.warn("[TieredNeo] Malformed attribute type ID: '{}'", attributeTypeID);
            return;
        }

        Optional<Holder.Reference<Attribute>> optional =
                BuiltInRegistries.ATTRIBUTE.getHolder(attributeRL);

        if (optional.isEmpty()) {
            TieredNeo.LOGGER.debug("[TieredNeo] Attribute '{}' not found in registry — "
                    + "may belong to another mod that is not loaded.", attributeRL);
            return;
        }

        event.addModifier(optional.get(), attributeModifier, slotGroup);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /** Returns required slots if present, otherwise optional slots. */
    private EquipmentSlot[] effectiveSlots() {
        if (requiredEquipmentSlots != null && requiredEquipmentSlots.length > 0) {
            return requiredEquipmentSlots;
        }
        return optionalEquipmentSlots;
    }

    private static boolean contains(EquipmentSlot[] slots, EquipmentSlot target) {
        for (EquipmentSlot s : slots) {
            if (s == target) return true;
        }
        return false;
    }
}