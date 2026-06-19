package com.herobrot.tieredneo.gson;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.lang.reflect.Type;

public class AttributeModifierSerializer implements JsonSerializer<AttributeModifier> {
    @Override
    public JsonElement serialize(AttributeModifier src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        obj.addProperty("amount", src.amount());
        obj.addProperty("operation", src.operation().name());
        obj.addProperty("name", src.id().toString());
        return obj;
    }
}