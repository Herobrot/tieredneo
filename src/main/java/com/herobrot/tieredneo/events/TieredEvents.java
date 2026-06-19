package com.herobrot.tieredneo.events;

import com.herobrot.tieredneo.TieredNeo;
import com.herobrot.tieredneo.api.AttributeTemplate;
import com.herobrot.tieredneo.api.ModifierUtils;
import com.herobrot.tieredneo.api.PotentialAttribute;
import com.herobrot.tieredneo.api.TierDataComponent;
import com.herobrot.tieredneo.command.CommandInit;
import com.herobrot.tieredneo.network.TieredNetwork;
import com.herobrot.tieredneo.network.payload.AttributeSyncPayload;
import com.herobrot.tieredneo.network.payload.HealthSyncPayload;
import com.herobrot.tieredneo.network.payload.ReforgeItemSyncPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = TieredNeo.MODID)
public class TieredEvents {
    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(TieredNeo.ATTRIBUTE_DATA_LOADER);
        event.addListener(TieredNeo.REFORGE_DATA_LOADER);
        TieredNeo.LOGGER.debug("[TieredNeo] Registered data reload listeners.");
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sendSyncPackets(player);
        }
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() != null) {
            sendSyncPackets(event.getPlayer());
            ModifierUtils.updateInventoryComponents(event.getPlayer().getInventory());
        } else {
            event.getPlayerList().getPlayers().forEach(player -> {
                sendSyncPackets(player);
                ModifierUtils.updateInventoryComponents(player.getInventory());
            });
            TieredNeo.LOGGER.info("[TieredNeo] Datapack reload — synced {} players.",
                    event.getPlayerList().getPlayers().size());
        }
    }

    @SubscribeEvent
    public static void onItemAttributeModifier(ItemAttributeModifierEvent event) {
        TierDataComponent tierData = event.getItemStack().get(TieredNeo.TIER_TYPE());
        if (tierData == null || tierData.isPresent()) return;

        PotentialAttribute attribute = TieredNeo.ATTRIBUTE_DATA_LOADER.getItemAttributes().get(tierData.tierId());
        if (attribute == null) return;

        for (AttributeTemplate template : attribute.getAttributes()) {
            String typeId = template.getAttributeTypeID();
            if (typeId.equals("tiered:generic.durable") || typeId.equals("tieredneo:generic.durable")) {
                continue;
            }

            template.applyModifiersToEvent(event);
        }
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandInit.register(event.getDispatcher());
    }

    private static void sendSyncPackets(ServerPlayer player) {
        TieredNetwork.sendToPlayer(player, AttributeSyncPayload.fromLoader(TieredNeo.ATTRIBUTE_DATA_LOADER));
        TieredNetwork.sendToPlayer(player, ReforgeItemSyncPayload.fromLoader(TieredNeo.REFORGE_DATA_LOADER));
        TieredNetwork.sendToPlayer(player, new HealthSyncPayload(player.getHealth()));
    }
}