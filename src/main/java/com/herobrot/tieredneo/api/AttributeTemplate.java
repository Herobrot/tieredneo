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
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AttributeTemplate {
    public static final String DURABLE_ID = "tieredneo:generic.durable";
    public static final String LEGACY_DURABLE_ID = "tiered:generic.durable";

    @SerializedName("type")
    private final String attributeTypeID;
    @SerializedName("modifier")
    private final AttributeModifier attributeModifier;
    @SerializedName("required_equipment_slots")
    private final EquipmentSlot[] requiredEquipmentSlots;
    @SerializedName("optional_equipment_slots")
    private final EquipmentSlot[] optionalEquipmentSlots;

    private Map<EquipmentSlot, AttributeModifier> cachedSlotModifiers;
    private Holder<Attribute> cachedAttributeHolder;
    private boolean initialized = false;

    public AttributeTemplate(String attributeTypeID, AttributeModifier attributeModifier,
                             @Nullable EquipmentSlot[] requiredEquipmentSlots, @Nullable EquipmentSlot[] optionalEquipmentSlots) {
        this.attributeTypeID = attributeTypeID;
        this.attributeModifier = attributeModifier;
        this.requiredEquipmentSlots = requiredEquipmentSlots;
        this.optionalEquipmentSlots = optionalEquipmentSlots;
    }

    public String getAttributeTypeID() { return attributeTypeID; }

    public AttributeModifier getEntityAttributeModifier() { return attributeModifier; }

    public EquipmentSlot[] getRequiredEquipmentSlots() { return requiredEquipmentSlots; }

    public EquipmentSlot[] getOptionalEquipmentSlots() { return optionalEquipmentSlots; }

    private void init() {
        if (initialized) return;
        this.cachedSlotModifiers = new HashMap<>();
        if (attributeModifier != null) {
            if (requiredEquipmentSlots != null)
                for (EquipmentSlot slot : requiredEquipmentSlots)
                    cachedSlotModifiers.put(slot, createSlotModifier(slot));
            if (optionalEquipmentSlots != null)
                for (EquipmentSlot slot : optionalEquipmentSlots)
                    if (!cachedSlotModifiers.containsKey(slot))
                        cachedSlotModifiers.put(slot, createSlotModifier(slot));
        }

        if (attributeTypeID != null) {
            ResourceLocation attributeRL = ResourceLocation.tryParse(attributeTypeID);
            if (attributeRL == null)
                TieredNeo.LOGGER.warn("[TieredNeo]: Ignored attribute ID (malformed): '{}'", attributeTypeID);
            else {
                Optional<Holder.Reference<Attribute>> optional = BuiltInRegistries.ATTRIBUTE.getHolder(attributeRL);
                if (optional.isEmpty())
                    TieredNeo.LOGGER.warn("[TieredNeo]: Attribute not found in the record: '{}'", attributeTypeID);
                else this.cachedAttributeHolder = optional.get();
            }
        }
        initialized = true;
    }

    public void applyModifiersToEvent(ItemAttributeModifierEvent event) {
        if (attributeTypeID == null || attributeModifier == null) return;
        if (!initialized) init();
        if (cachedAttributeHolder == null || cachedSlotModifiers == null) return;
        if (requiredEquipmentSlots != null && requiredEquipmentSlots.length > 0)
            for (EquipmentSlot slot : requiredEquipmentSlots) {
                AttributeModifier mod = cachedSlotModifiers.get(slot);
                if (mod != null)
                    event.addModifier(cachedAttributeHolder, mod, EquipmentSlotGroup.bySlot(slot));

            }
        else if (optionalEquipmentSlots != null && optionalEquipmentSlots.length > 0) {
            for (EquipmentSlot slot : optionalEquipmentSlots) {
                AttributeModifier mod = cachedSlotModifiers.get(slot);
                if (mod != null)
                    event.addModifier(cachedAttributeHolder, mod, EquipmentSlotGroup.bySlot(slot));
            }
        } else event.addModifier(cachedAttributeHolder, attributeModifier, EquipmentSlotGroup.ANY);
    }

    private AttributeModifier createSlotModifier(EquipmentSlot slot) {
        ResourceLocation baseId = attributeModifier.id();
        ResourceLocation slotSpecificId = ResourceLocation.fromNamespaceAndPath(baseId.getNamespace(),
                baseId.getPath() + "_" + slot.getName());

        return new AttributeModifier(slotSpecificId, attributeModifier.amount(), attributeModifier.operation());
    }
}