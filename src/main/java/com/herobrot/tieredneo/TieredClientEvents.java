package com.herobrot.tieredneo;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

/**
 * Client-side GAME bus event listener.
 *
 * Separated from TieredNeoClient (which is on the MOD bus) because NeoForge
 * does not allow a single class to subscribe to both buses via annotation.
 */
@EventBusSubscriber(modid = TieredNeo.MODID, value = Dist.CLIENT)
public class TieredClientEvents {

    /**
     * Clears all client-side caches when the player disconnects.
     *
     * Without this, data from Server A (attribute definitions, border templates)
     * would persist in memory when joining Server B, causing wrong tooltips or
     * applying attributes that don't exist in the new server's datapacks.
     */
    @SubscribeEvent
    public static void onPlayerLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        TieredNeoClient.CACHED_ATTRIBUTES.clear();
        TieredNeoClient.BORDER_TEMPLATES.clear();
        TieredNeo.LOGGER.debug("[TieredNeo] Client caches cleared on logout.");
    }
}