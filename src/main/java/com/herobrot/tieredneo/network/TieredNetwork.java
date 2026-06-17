package com.herobrot.tieredneo.network;

import com.herobrot.tieredneo.TieredNeo;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Central networking registration for TieredNeo.
 *
 * NeoForge 1.21.1 networking model:
 *  - Each packet is a record implementing CustomPacketPayload.
 *  - Payloads are registered on the mod event bus via RegisterPayloadHandlersEvent.
 *  - Sending uses PacketDistributor (replaces ServerPlayNetworking.send / ClientPlayNetworking.send).
 *
 * This class is the skeleton; payload types and handlers are filled in during step 5.
 */
public final class TieredNetwork {

    /** Network protocol version. Increment on breaking payload changes. */
    private static final String PROTOCOL_VERSION = "1";

    private TieredNetwork() {}

    /**
     * Attach the payload registration listener to the mod event bus.
     * Called from {@link TieredNeo#TieredNeo(IEventBus, net.neoforged.fml.ModContainer)}.
     */
    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(TieredNetwork::onRegisterPayloads);
    }

    /**
     * Fired by NeoForge after all mods have registered their payload types.
     * Step 5 will fill in the individual payload registrations here.
     */
    private static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        // ---- Payload registrations will be added in Step 5 ----
        // Example shape (do not remove this comment):
        //
        // registrar.playToClient(
        //         AttributeSyncPayload.TYPE,
        //         AttributeSyncPayload.STREAM_CODEC,
        //         TieredClientPacketHandler::handleAttributeSync
        // );
        //
        // registrar.playToServer(
        //         ReforgeRequestPayload.TYPE,
        //         ReforgeRequestPayload.STREAM_CODEC,
        //         TieredServerPacketHandler::handleReforgeRequest
        // );

        TieredNeo.LOGGER.debug("[TieredNeo] Payload handlers registered.");
    }

    // -------------------------------------------------------------------------
    // Send helpers (thin wrappers around PacketDistributor)
    // -------------------------------------------------------------------------

    /** Send a payload S2C to a specific player. */
    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    /** Send a payload S2C to all players on the server. */
    public static void sendToAll(CustomPacketPayload payload) {
        PacketDistributor.sendToAllPlayers(payload);
    }
}