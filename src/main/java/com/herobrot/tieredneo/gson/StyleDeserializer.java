package com.herobrot.tieredneo.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.lang.reflect.Type;
import java.util.Map;

public class StyleDeserializer implements JsonDeserializer<Style> {
    @Override
    public Style deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        Style style = Style.EMPTY;
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();
            switch (key) {
                case "color" -> {
                    TextColor textColor = TextColor.parseColor(value.getAsString()).result().orElse(null);
                    if (textColor != null) style = style.withColor(textColor);
                }
                case "bold" -> style = style.withBold(value.getAsBoolean());
                case "italic" -> style = style.withItalic(value.getAsBoolean());
                case "underlined" -> style = style.withUnderlined(value.getAsBoolean());
                case "strikethrough" -> style = style.withStrikethrough(value.getAsBoolean());
                case "obfuscated" -> style = style.withObfuscated(value.getAsBoolean());
            }
        }
        return style;
    }
}