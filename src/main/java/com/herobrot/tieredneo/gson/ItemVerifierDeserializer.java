package com.herobrot.tieredneo.gson;

import com.google.gson.*;
import com.herobrot.tieredneo.api.ItemVerifier;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Deserializes the compact TieredZ verifier format into ItemVerifier instances.
 *
 * TieredZ datapack format (each verifier is an object with ONE dynamic key):
 *   { "tag": "minecraft:swords" }
 *   { "item": "minecraft:diamond_sword" }
 *
 * Our ItemVerifier has two fields: type (String) and value (String).
 * The key of the JSON object IS the type; the value is a string.
 * Standard Gson field mapping cannot handle this, hence the custom deserializer.
 *
 * If the object has multiple keys (malformed), only the first is used.
 * If the object is empty, a JsonParseException is thrown.
 */
public class ItemVerifierDeserializer implements JsonDeserializer<ItemVerifier> {

    @Override
    public ItemVerifier deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        if (!json.isJsonObject()) {
            throw new JsonParseException(
                    "[TieredNeo] ItemVerifier must be a JSON object, got: " + json);
        }

        JsonObject obj = json.getAsJsonObject();

        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            String type  = entry.getKey();
            String value = entry.getValue().getAsString();
            return new ItemVerifier(type, value);
        }

        throw new JsonParseException("[TieredNeo] ItemVerifier object is empty.");
    }
}