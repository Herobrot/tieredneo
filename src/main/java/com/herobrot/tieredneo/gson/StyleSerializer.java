package com.herobrot.tieredneo.gson;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.minecraft.network.chat.Style;

import java.lang.reflect.Type;

public class StyleSerializer implements JsonSerializer<Style> {
    @Override
    public JsonElement serialize(Style style, Type type, JsonSerializationContext context) {
        if (style.isEmpty()) return JsonNull.INSTANCE;

        JsonObject obj = new JsonObject();

        if (style.isBold()) obj.addProperty("bold", true);
        if (style.isItalic()) obj.addProperty("italic", true);
        if (style.isUnderlined()) obj.addProperty("underlined", true);
        if (style.isStrikethrough()) obj.addProperty("strikethrough", true);
        if (style.isObfuscated()) obj.addProperty("obfuscated", true);

        if (style.getColor() != null) {

            obj.addProperty("color", style.getColor().serialize());
        }
        style.getFont();
        obj.addProperty("font", style.getFont().toString());

        return obj;
    }
}