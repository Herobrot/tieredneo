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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Loads assets/namespace/tooltips/*.json from client resource packs.
 */
public class TooltipBorderLoader extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new Gson();

    public TooltipBorderLoader() {
        super(GSON, "tooltips");
    }

    @Override
    protected void apply(@NotNull Map<ResourceLocation, JsonElement> objectMap,
                         @NotNull ResourceManager resourceManager,
                         @NotNull ProfilerFiller profiler) {

        List<BorderTemplate> newTemplates = new ArrayList<>();

        objectMap.forEach((id, element) -> {
            try {
                JsonObject data = element.getAsJsonObject();
                if (data.has("tooltips")) {
                    data.getAsJsonArray("tooltips").forEach(tElement -> {
                        JsonObject t = tElement.getAsJsonObject();
                        List<String> decider = new ArrayList<>();

                        if (t.has("decider")) {
                            t.getAsJsonArray("decider").forEach(d -> decider.add(d.getAsString()));
                        }

                        // Fallback idéntico al original si no hay gradiente de fondo
                        int backgroundGradient = t.has("background_gradient")
                                ? new BigInteger(t.get("background_gradient").getAsString(), 16).intValue()
                                : -267386864;

                        newTemplates.add(new BorderTemplate(
                                t.get("index").getAsInt(),
                                t.get("texture").getAsString(),
                                new BigInteger(t.get("start_border_gradient").getAsString(), 16).intValue(),
                                new BigInteger(t.get("end_border_gradient").getAsString(), 16).intValue(),
                                backgroundGradient,
                                decider
                        ));
                    });
                }
            } catch (Exception e) {
                TieredNeo.LOGGER.error("[TieredNeo] Error parseando tooltip JSON: {}", id, e);
            }
        });

        // Limpiar y poblar la caché del cliente de forma segura en el hilo principal
        TieredNeoClient.BORDER_TEMPLATES.clear();
        TieredNeoClient.BORDER_TEMPLATES.addAll(newTemplates);
        TieredNeo.LOGGER.info("[TieredNeo] Cargadas {} plantillas de bordes visuales.", newTemplates.size());
    }
}