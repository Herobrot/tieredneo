package com.herobrot.tieredneo.gson;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.minecraft.world.entity.EquipmentSlot;

import java.lang.reflect.Type;

public class EquipmentSlotSerializer implements JsonSerializer<EquipmentSlot> {
    @Override
    public JsonElement serialize(EquipmentSlot src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.getName());
    }
}