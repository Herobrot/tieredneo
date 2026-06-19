package com.herobrot.tieredneo.events;

import com.herobrot.tieredneo.TieredNeo;
import com.herobrot.tieredneo.TieredNeoClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@EventBusSubscriber(modid = TieredNeo.MODID, value = Dist.CLIENT)
public class TieredClientEvents {

    @SubscribeEvent
    public static void onPlayerLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        TieredNeoClient.CACHED_ATTRIBUTES.clear();
        TieredNeo.LOGGER.debug("[TieredNeo] Server-synced client attributes cleared on logout.");
    }
}