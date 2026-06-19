package com.herobrot.tieredneo.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.lang.reflect.Type;

public class StyleDeserializer implements JsonDeserializer<Style> {
    @Override
    public Style deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        Style style = Style.EMPTY;

        if (jsonObject.has("color")) {
            String colorStr = jsonObject.get("color").getAsString();
            TextColor textColor = TextColor.parseColor(colorStr).result().orElse(null);
            if (textColor != null) {
                style = style.withColor(textColor);
            }
        }

        if (jsonObject.has("bold")) {
            style = style.withBold(jsonObject.get("bold").getAsBoolean());
        }
        if (jsonObject.has("italic")) {
            style = style.withItalic(jsonObject.get("italic").getAsBoolean());
        }
        if (jsonObject.has("underlined")) {
            style = style.withUnderlined(jsonObject.get("underlined").getAsBoolean());
        }
        if (jsonObject.has("strikethrough")) {
            style = style.withStrikethrough(jsonObject.get("strikethrough").getAsBoolean());
        }
        if (jsonObject.has("obfuscated")) {
            style = style.withObfuscated(jsonObject.get("obfuscated").getAsBoolean());
        }

        return style;
    }
}