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
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

import java.util.Optional;

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

    public String getAttributeTypeID() { return attributeTypeID; }
    public AttributeModifier getEntityAttributeModifier() { return attributeModifier; }
    public EquipmentSlot[] getRequiredEquipmentSlots() { return requiredEquipmentSlots; }
    public EquipmentSlot[] getOptionalEquipmentSlots() { return optionalEquipmentSlots; }

    // -------------------------------------------------------------------------
    // Application via ItemAttributeModifierEvent (NeoForge 1.21.1)
    // -------------------------------------------------------------------------

    public void applyModifiersToEvent(ItemAttributeModifierEvent event) {
        if (attributeTypeID == null || attributeModifier == null) return;

        // 1. Obtener el Atributo del Registro
        String normalizedId = attributeTypeID.contains(":") ? attributeTypeID : "minecraft:" + attributeTypeID;
        ResourceLocation attributeRL = ResourceLocation.tryParse(normalizedId);
        if (attributeRL == null) {
            TieredNeo.LOGGER.warn("[TieredNeo] ID de atributo ignorado (mal formado): '{}'", attributeTypeID);
            return;
        }

        Optional<Holder.Reference<Attribute>> optional = BuiltInRegistries.ATTRIBUTE.getHolder(attributeRL);
        if (optional.isEmpty()) return;
        Holder<Attribute> attributeHolder = optional.get();

        // 2. Descubrir el "Slot Natural" del objeto actual (Solución al bug de las armaduras)
        ItemStack stack = event.getItemStack();
        Item item = stack.getItem();
        EquipmentSlot naturalSlot = EquipmentSlot.MAINHAND; // Por defecto (Espadas, Herramientas)

        if (item instanceof ArmorItem armor) {
            naturalSlot = armor.getType().getSlot();
        } else if (item instanceof ShieldItem) {
            naturalSlot = EquipmentSlot.OFFHAND;
        }

        // 3. Lógica de Aplicación
        if (requiredEquipmentSlots != null && requiredEquipmentSlots.length > 0) {
            // REQUIRED: Se aplica estrictamente a todos los definidos
            for (EquipmentSlot slot : requiredEquipmentSlots) {
                applyToSlot(event, attributeHolder, slot);
            }
        } else if (optionalEquipmentSlots != null && optionalEquipmentSlots.length > 0) {
            // OPTIONAL: Filtramos y aplicamos SOLO si el slot natural está en la lista
            for (EquipmentSlot slot : optionalEquipmentSlots) {
                if (slot == naturalSlot) {
                    applyToSlot(event, attributeHolder, slot);
                    break; // Solo lo aplicamos una vez a su slot natural
                }
            }
        } else {
            // ANY: Si el JSON no tiene array de slots, se aplica siempre (ej: items genéricos)
            event.addModifier(attributeHolder, attributeModifier, EquipmentSlotGroup.ANY);
        }
    }

    /**
     * Helper interno para encapsular la lógica de nombrado único para los modificadores en 1.21.1
     */
    private void applyToSlot(ItemAttributeModifierEvent event, Holder<Attribute> attributeHolder, EquipmentSlot slot) {
        EquipmentSlotGroup group = EquipmentSlotGroup.bySlot(slot);
        ResourceLocation baseId = attributeModifier.id();

        // En 1.21.1 requerimos IDs únicos por slot
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