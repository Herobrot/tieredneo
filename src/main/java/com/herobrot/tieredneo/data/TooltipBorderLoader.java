package com.herobrot.tieredneo.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.herobrot.tieredneo.TieredNeo;
import com.herobrot.tieredneo.TieredNeoClient;
import com.herobrot.tieredneo.api.BorderTemplate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TooltipBorderLoader extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();

    public TooltipBorderLoader() {
        super(GSON, "tooltips");
    }

    @Override
    protected void apply(@NotNull Map<ResourceLocation, JsonElement> objectMap, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        List<BorderTemplate> newTemplates = new ArrayList<>();
        objectMap.forEach((id, element) -> {
            try {
                JsonObject data = element.getAsJsonObject();
                if (data.has("tooltips")) {
                    data.getAsJsonArray("tooltips").forEach(tElement -> {
                        JsonObject t = tElement.getAsJsonObject();
                        List<String> decider = new ArrayList<>();
                        if (t.has("decider"))
                            t.getAsJsonArray("decider").forEach(d -> decider.add(d.getAsString()));

                        int backgroundGradient = t.has("background_gradient") ?
                                Integer.parseUnsignedInt(t.get("background_gradient").getAsString(), 16) : -267386864;

                        newTemplates.add(new BorderTemplate(
                                t.get("index").getAsInt(),
                                t.get("texture").getAsString(),
                                Integer.parseUnsignedInt(t.get("start_border_gradient").getAsString(), 16),
                                Integer.parseUnsignedInt(t.get("end_border_gradient").getAsString(), 16),
                                backgroundGradient,
                                decider
                        ));
                    });
                }
            } catch (Exception e) {
                TieredNeo.LOGGER.error("[TieredNeo]: Error parsing tooltip JSON: {}", id, e);
            }
        });
        TieredNeoClient.BORDER_TEMPLATES.clear();
        TieredNeoClient.BORDER_TEMPLATES.addAll(newTemplates);
        TieredNeo.LOGGER.info("[TieredNeo]: Loaded {} visual border templates.", newTemplates.size());
    }
}