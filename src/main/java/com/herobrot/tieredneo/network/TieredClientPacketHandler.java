package com.herobrot.tieredneo.network;

import com.herobrot.tieredneo.TieredNeo;
import com.herobrot.tieredneo.TieredNeoClient;
import com.herobrot.tieredneo.api.PotentialAttribute;
import com.herobrot.tieredneo.data.AttributeDataLoader;
import com.herobrot.tieredneo.network.payload.*;
import com.herobrot.tieredneo.reforge.ReforgeScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TieredClientPacketHandler {

    public static void handleHealthSync(HealthSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.setHealth(payload.health());
            }
        });
    }

    public static void handleReforgeReady(ReforgeReadyPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().screen instanceof ReforgeScreen screen && screen.reforgeButton != null)
                screen.reforgeButton.active = !payload.disableButton();
        });
    }

    public static void handleReforgeItemSync(ReforgeItemSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> TieredNeo.REFORGE_DATA_LOADER.setRawMap(payload.reforgeMap()));
    }

    public static void handleAttributeSync(AttributeSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Map<ResourceLocation, PotentialAttribute> incoming = new HashMap<>();
            List<ResourceLocation> ids = payload.ids();
            List<String> jsons = payload.jsons();
            for (int i = 0; i < ids.size(); i++) {
                try {
                    PotentialAttribute attr = AttributeDataLoader.GSON.fromJson(jsons.get(i), PotentialAttribute.class);
                    if (attr != null) incoming.put(ids.get(i), attr);
                } catch (Exception e) {
                    TieredNeo.LOGGER.error("[TieredNeo]: Client failed to deserialize attribute '{}': {}", ids.get(i), e);
                }
            }
            TieredNeo.ATTRIBUTE_DATA_LOADER.applyClientSync(incoming);
            TieredNeoClient.CACHED_ATTRIBUTES.clear();
            TieredNeoClient.CACHED_ATTRIBUTES.putAll(incoming);
            TieredNeo.LOGGER.debug("[TieredNeo]: Client received {} attributes from server.", incoming.size());
        });
    }
}