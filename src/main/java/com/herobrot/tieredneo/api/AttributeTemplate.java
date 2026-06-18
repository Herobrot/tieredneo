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
 * "type": "minecraft:generic.attack_damage",
 * "modifier": { "name": "...", "amount": 0.1, "operation": "ADD_MULTIPLIED_TOTAL" },
 * "optional_equipment_slots": ["MAINHAND", "OFFHAND"]
 * }
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
    // Application via ItemAttributeModifierEvent (NeoForge 1.21.1)
    // -------------------------------------------------------------------------

    public void applyModifiersToEvent(ItemAttributeModifierEvent event) {
        if (attributeTypeID == null || attributeModifier == null) return;

        // Normalizar los namespaces vacíos de TieredZ ("generic.attack_damage" -> "minecraft:generic.attack_damage")
        String normalizedId = attributeTypeID.contains(":") ? attributeTypeID : "minecraft:" + attributeTypeID;

        ResourceLocation attributeRL = ResourceLocation.tryParse(normalizedId);
        if (attributeRL == null) {
            TieredNeo.LOGGER.warn("[TieredNeo] ID de atributo ignorado (mal formado): '{}'", attributeTypeID);
            return;
        }

        Optional<Holder.Reference<Attribute>> optional = BuiltInRegistries.ATTRIBUTE.getHolder(attributeRL);
        if (optional.isEmpty()) return;
        Holder<Attribute> attributeHolder = optional.get();

        // Determinar qué arreglo de slots usar
        EquipmentSlot[] slotsToApply = (requiredEquipmentSlots != null && requiredEquipmentSlots.length > 0)
                ? requiredEquipmentSlots
                : (optionalEquipmentSlots != null ? optionalEquipmentSlots : new EquipmentSlot[0]);

        // Si no hay slots definidos, aplicar a TODO (ANY)
        if (slotsToApply.length == 0) {
            event.addModifier(attributeHolder, attributeModifier, EquipmentSlotGroup.ANY);
            return;
        }

        // Aplicar el modificador a cada slot individualmente
        for (EquipmentSlot slot : slotsToApply) {
            EquipmentSlotGroup group = EquipmentSlotGroup.bySlot(slot);

            // En 1.21.1, múltiples modificadores requieren IDs únicos si se aplican al mismo ítem.
            // Sufijamos el ID con el nombre del slot (ej. "tiered:epic_armor_1_chest")
            ResourceLocation baseId = attributeModifier.id();
            ResourceLocation slotSpecificId = ResourceLocation.fromNamespaceAndPath(
                    baseId.getNamespace(),
                    baseId.getPath() + "_" + slot.getName().toLowerCase()
            );

            AttributeModifier slotModifier = new AttributeModifier(
                    slotSpecificId,
                    attributeModifier.amount(),
                    attributeModifier.operation()
            );

            event.addModifier(attributeHolder, slotModifier, group);
        }
    }
}