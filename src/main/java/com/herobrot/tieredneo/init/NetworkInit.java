package com.herobrot.tieredneo.init;

import com.herobrot.heroslib.network.PayloadRegistryManager;
import com.herobrot.heroslib.util.ModUtils;
import com.herobrot.tieredneo.TieredNeo;
import com.herobrot.tieredneo.network.TieredClientPacketHandler;
import com.herobrot.tieredneo.network.TieredServerPacketHandler;
import com.herobrot.tieredneo.network.payload.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public final class NetworkInit {
    public static final String PROTOCOL_VERSION = ModUtils.getModVersion(TieredNeo.MODID);

    private NetworkInit() {}

    public static void register() {
        PayloadRegistryManager.registerClientbound(TieredNeo.MODID, PROTOCOL_VERSION, AttributeSyncPayload.TYPE,
                AttributeSyncPayload.STREAM_CODEC, TieredClientPacketHandler::handleAttributeSync);
        PayloadRegistryManager.registerClientbound(TieredNeo.MODID, PROTOCOL_VERSION, HealthSyncPayload.TYPE,
                HealthSyncPayload.STREAM_CODEC, TieredClientPacketHandler::handleHealthSync);
        PayloadRegistryManager.registerClientbound(TieredNeo.MODID, PROTOCOL_VERSION, ReforgeItemSyncPayload.TYPE,
                ReforgeItemSyncPayload.STREAM_CODEC, TieredClientPacketHandler::handleReforgeItemSync);
        PayloadRegistryManager.registerClientbound(TieredNeo.MODID, PROTOCOL_VERSION, ReforgeReadyPayload.TYPE,
                ReforgeReadyPayload.STREAM_CODEC, TieredClientPacketHandler::handleReforgeReady);

        PayloadRegistryManager.registerServerbound(TieredNeo.MODID, PROTOCOL_VERSION, ReforgeRequestPayload.TYPE,
                ReforgeRequestPayload.STREAM_CODEC, TieredServerPacketHandler::handleReforgeRequest);
        PayloadRegistryManager.registerServerbound(TieredNeo.MODID, PROTOCOL_VERSION, ReforgeScreenPayload.TYPE,
                ReforgeScreenPayload.STREAM_CODEC, TieredServerPacketHandler::handleReforgeScreen);
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }
}