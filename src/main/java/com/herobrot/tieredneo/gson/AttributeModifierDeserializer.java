package com.herobrot.tieredneo.gson;

import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.lang.reflect.Type;

public class AttributeModifierDeserializer implements JsonDeserializer<AttributeModifier> {
    @Override
    public AttributeModifier deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        JsonObject obj = json.getAsJsonObject();

        String nameStr = getRequired(obj, "name").getAsString();
        double amount = getRequired(obj, "amount").getAsDouble();
        String opStr = getRequired(obj, "operation").getAsString();

        ResourceLocation id = ResourceLocation.tryParse(nameStr);
        if (id == null) {
            throw new JsonParseException("[TieredNeo] AttributeModifier 'name' is not a valid ResourceLocation: '" + nameStr + "'");
        }

        AttributeModifier.Operation operation;
        try {
            operation = AttributeModifier.Operation.valueOf(opStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new JsonParseException("[TieredNeo] Unknown AttributeModifier operation: '" + opStr + "'. Valid " + "values: ADD_VALUE, ADD_MULTIPLIED_BASE, ADD_MULTIPLIED_TOTAL");
        }

        return new AttributeModifier(id, amount, operation);
    }

    private JsonElement getRequired(JsonObject obj, String key) throws JsonParseException {
        if (!obj.has(key)) {
            throw new JsonParseException("[TieredNeo] AttributeModifier JSON missing field: '" + key + "'");
        }
        return obj.get(key);
    }
}