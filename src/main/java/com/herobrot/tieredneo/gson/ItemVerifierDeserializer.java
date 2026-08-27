package com.herobrot.tieredneo.gson;

import com.google.gson.*;
import com.herobrot.tieredneo.TieredNeo;
import com.herobrot.tieredneo.api.ItemVerifier;

import java.lang.reflect.Type;
import java.util.Map;

public class ItemVerifierDeserializer implements JsonDeserializer<ItemVerifier> {
    @Override
    public ItemVerifier deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        if (obj.has("type") && obj.has("value"))
            return new ItemVerifier(obj.get("type").getAsString(), obj.get("value").getAsString());
        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue().getAsString();
            if (!key.equals("item") && !key.equals("id") && !key.equals("tag") && !key.equals("namespace") && !key.equals("contains") && !key.equals("startswith"))
                TieredNeo.LOGGER.warn("[TieredNeo]: Datapack Warning: Unknown or malformed verifier -> type: " + "'{}', value: '{}'", key, value);
            return new ItemVerifier(key, value);
        }
        throw new JsonParseException("Formato de ItemVerifier vacío o desconocido: " + json);
    }
}