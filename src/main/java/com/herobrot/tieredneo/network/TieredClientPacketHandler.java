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

import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public class TieredClientPacketHandler {

    public static void init() {
        // Handlers are registered declaratively in TieredNetwork.onRegisterPayloads().
        // This method exists as a hook for any future imperative setup.
    }

    // -------------------------------------------------------------------------
    // S2C handlers
    // -------------------------------------------------------------------------

    public static void handleHealthSync(HealthSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.setHealth(payload.health());
            }
        });
    }

    public static void handleReforgeReady(ReforgeReadyPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().screen instanceof ReforgeScreen screen
                    && screen.reforgeButton != null) {
                screen.reforgeButton.active = !payload.disableButton();
            }
        });
    }

    public static void handleReforgeItemSync(ReforgeItemSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() ->
                TieredNeo.REFORGE_DATA_LOADER.setRawMap(payload.reforgeMap())
        );
    }

    /**
     * Deserializes each PotentialAttribute from its JSON string and populates
     * both the live loader map (for immediate use) and the client cache
     * (for reconnect recovery).
     *
     * The previous map is saved to CACHED_ATTRIBUTES before replacement so that
     * if this payload arrives while items are being rendered, the old data is
     * still accessible until the full replacement completes on the main thread.
     */
    public static void handleAttributeSync(AttributeSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Snapshot the old data into the client cache before overwriting
            TieredNeoClient.CACHED_ATTRIBUTES.clear();
            TieredNeoClient.CACHED_ATTRIBUTES.putAll(TieredNeo.ATTRIBUTE_DATA_LOADER.getItemAttributes());

            // Deserialize the incoming attributes
            Map<ResourceLocation, PotentialAttribute> incoming = new HashMap<>();
            List<ResourceLocation> ids   = payload.ids();
            List<String>           jsons = payload.jsons();

            for (int i = 0; i < ids.size(); i++) {
                try {
                    PotentialAttribute attr =
                            AttributeDataLoader.GSON.fromJson(jsons.get(i), PotentialAttribute.class);
                    if (attr != null) {
                        incoming.put(ids.get(i), attr);
                    }
                } catch (Exception e) {
                    TieredNeo.LOGGER.error("[TieredNeo] Client failed to deserialize attribute '{}': {}",
                            ids.get(i), e.getMessage());
                }
            }

            // Replace the loader's map — it accepts client updates via this path
            TieredNeo.ATTRIBUTE_DATA_LOADER.applyClientSync(incoming);
            TieredNeo.LOGGER.debug("[TieredNeo] Client received {} attributes from server.", incoming.size());
        });
    }

    /**
     * Reemplaza LibzServerPacket.writeS2CMousePositionPacket.
     * Forces the cursor position using GLFW directly, converting
     * scaled UI coordinates to real window pixel coordinates.
     */
    public static void handleMousePosition(MousePositionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            long windowHandle = Minecraft.getInstance().getWindow().getWindow();
            double scale = Minecraft.getInstance().getWindow().getGuiScale();
            GLFW.glfwSetCursorPos(windowHandle, payload.mouseX() * scale, payload.mouseY() * scale);
        });
    }
}