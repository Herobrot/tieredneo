package com.herobrot.tieredneo.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.world.entity.EquipmentSlot;

import java.lang.reflect.Type;

public class EquipmentSlotDeserializer implements JsonDeserializer<EquipmentSlot> {
    @Override
    public EquipmentSlot deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String slotName = json.getAsString().toLowerCase();
        return switch (slotName) {
            case "mainhand" -> EquipmentSlot.MAINHAND;
            case "offhand" -> EquipmentSlot.OFFHAND;
            case "feet" -> EquipmentSlot.FEET;
            case "legs" -> EquipmentSlot.LEGS;
            case "chest" -> EquipmentSlot.CHEST;
            case "head" -> EquipmentSlot.HEAD;
            case "body" -> EquipmentSlot.BODY;
            default ->
                    throw new JsonParseException("[TieredNeo]: EquipmentSlot unknown: '" + slotName + "'. Valid "
                            + "values: MAINHAND, OFFHAND, FEET, LEGS, CHEST, HEAD, BODY");
        };
    }
}