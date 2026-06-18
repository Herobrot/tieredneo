package com.herobrot.tieredneo;

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

/**
 * Game-bus event listeners for TieredNeo.

 * Handles:
 *  - Data reload listener registration
 *  - Player join / datapack sync packet dispatch
 *  - Dynamic attribute application via ItemAttributeModifierEvent
 *  - Command registration
 */
@EventBusSubscriber(modid = TieredNeo.MODID)
public class TieredEvents {

    // -------------------------------------------------------------------------
    // Data loader registration
    // -------------------------------------------------------------------------

    /**
     * Registers server-side data loaders so they run on every datapack (re)load.
     * Equivalent to Fabric's ResourceManagerHelper.get(SERVER_DATA).registerReloadListener(...)
     */
    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(TieredNeo.ATTRIBUTE_DATA_LOADER);
        event.addListener(TieredNeo.REFORGE_DATA_LOADER);
        TieredNeo.LOGGER.debug("[TieredNeo] Registered data reload listeners.");
    }

    // -------------------------------------------------------------------------
    // Player join & datapack sync
    // -------------------------------------------------------------------------

    /**
     * Sends attribute + reforge + health data to a player when they log in.
     * Equivalent to Fabric's ServerPlayConnectionEvents.JOIN.
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sendSyncPackets(player);
        }
    }

    /**
     * After a /reload, resync all online players and refresh their inventories.
     * Uses OnDatapackSyncEvent which distinguishes single-player join sync
     * from broadcast reload sync — more precise than Fabric's END_DATA_PACK_RELOAD.
     */
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

    // -------------------------------------------------------------------------
    // Dynamic attribute application (NeoForge 1.21.1 standard)
    // -------------------------------------------------------------------------

    /**
     * Applies PotentialAttribute modifiers to ItemStacks at query time.

     * This event fires whenever the game needs to know an item's attributes
     * (tooltip rendering, combat calculation, equipment change). The modifiers
     * are never stored persistently on the stack — they are derived dynamically
     * from the TierDataComponent, which only stores the tier ID.

     * This is the correct 1.21.1 pattern. The old approach of injecting modifiers
     * into ATTRIBUTE_MODIFIERS DataComponent at craft time would survive reloads
     * badly and conflict with other mods that also write to that component.

     * Flow:
     *  1. Read TierDataComponent from the stack.
     *  2. Look up the PotentialAttribute in the loader's immutable snapshot.
     *  3. For each AttributeTemplate, resolve the EquipmentSlotGroup and call
     *     applyModifiersToEvent() — which internally calls event.addModifier().
     */
    @SubscribeEvent
    public static void onItemAttributeModifier(ItemAttributeModifierEvent event) {
        TierDataComponent tierData = event.getItemStack().get(TieredNeo.TIER_TYPE());
        if (tierData == null || tierData.isPresent()) return;

        PotentialAttribute attribute =
                TieredNeo.ATTRIBUTE_DATA_LOADER.getItemAttributes().get(tierData.tierId());
        if (attribute == null) return;

        for (AttributeTemplate template : attribute.getAttributes()) {
            String typeId = template.getAttributeTypeID();
            if (typeId.equals("tiered:generic.durable") || typeId.equals("tieredneo:generic.durable")) {
                continue;
            }
            // Ahora la plantilla maneja sus propios slots internamente
            template.applyModifiersToEvent(event);
        }
    }

    // -------------------------------------------------------------------------
    // Commands
    // -------------------------------------------------------------------------

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandInit.register(event.getDispatcher());
    }
    // -------------------------------------------------------------------------
    // Internal
    // -------------------------------------------------------------------------

    private static void sendSyncPackets(ServerPlayer player) {
        TieredNetwork.sendToPlayer(player, AttributeSyncPayload.fromLoader(TieredNeo.ATTRIBUTE_DATA_LOADER));
        TieredNetwork.sendToPlayer(player, ReforgeItemSyncPayload.fromLoader(TieredNeo.REFORGE_DATA_LOADER));
        TieredNetwork.sendToPlayer(player, new HealthSyncPayload(player.getHealth()));
    }
}