package com.herobrot.tieredneo.network;

import com.herobrot.tieredneo.TieredNeo;
import com.herobrot.tieredneo.network.payload.*;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class TieredNetwork {

    private static final String PROTOCOL_VERSION = "1";

    private TieredNetwork() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(TieredNetwork::onRegisterPayloads);
    }

    private static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        // ---- Server -> Client ----
        registrar.playToClient(AttributeSyncPayload.TYPE, AttributeSyncPayload.STREAM_CODEC, TieredClientPacketHandler::handleAttributeSync);
        registrar.playToClient(HealthSyncPayload.TYPE, HealthSyncPayload.STREAM_CODEC, TieredClientPacketHandler::handleHealthSync);
        registrar.playToClient(ReforgeItemSyncPayload.TYPE, ReforgeItemSyncPayload.STREAM_CODEC, TieredClientPacketHandler::handleReforgeItemSync);
        registrar.playToClient(ReforgeReadyPayload.TYPE, ReforgeReadyPayload.STREAM_CODEC, TieredClientPacketHandler::handleReforgeReady);

        // ---- Client -> Server ----
        registrar.playToServer(ReforgeRequestPayload.TYPE, ReforgeRequestPayload.STREAM_CODEC, TieredServerPacketHandler::handleReforgeRequest);
        registrar.playToServer(ReforgeScreenPayload.TYPE, ReforgeScreenPayload.STREAM_CODEC, TieredServerPacketHandler::handleReforgeScreen);

        TieredNeo.LOGGER.debug("[TieredNeo] Payload handlers registrados correctamente.");
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    public static void sendToAll(CustomPacketPayload payload) {
        PacketDistributor.sendToAllPlayers(payload);
    }
}