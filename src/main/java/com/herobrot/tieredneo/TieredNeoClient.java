package com.herobrot.tieredneo;

import com.herobrot.tieredneo.api.BorderTemplate;
import com.herobrot.tieredneo.api.PotentialAttribute;
import com.herobrot.tieredneo.config.TieredConfig;
import com.herobrot.tieredneo.data.TooltipBorderLoader;
import com.herobrot.tieredneo.network.TieredClientPacketHandler;
import com.herobrot.tieredneo.reforge.ReforgeScreen;
import com.herobrot.tieredneo.screen.TabNavigationHelper;

import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client-only entry point and event listener.

 * Fixes over the original:
 *  - CACHED_ATTRIBUTES and BORDER_TEMPLATES are cleared on logout
 *    (ClientPlayerNetworkEvent.LoggingOut) to prevent data from a previous
 *    server leaking into a new connection.
 *  - Screen and reload-listener registration moved to dedicated events.
 */
@EventBusSubscriber(modid = TieredNeo.MODID, value = Dist.CLIENT)
public class TieredNeoClient {

    /**
     * Registra la pantalla de configuración en el menú de Mods de NeoForge.
     * Llamado desde el constructor principal exclusivamente cuando Dist.CLIENT es detectado.
     */
    public static void registerConfigScreen(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (modContainer, parentScreen) -> AutoConfig.getConfigScreen(TieredConfig.class, parentScreen).get());
    }

    // -------------------------------------------------------------------------
    // Client-side caches
    // -------------------------------------------------------------------------

    /**
     * Attributes received from the server. Populated by the AttributeSyncPayload
     * handler and cleared on logout to prevent cross-server contamination.
     */
    public static final Map<ResourceLocation, PotentialAttribute> CACHED_ATTRIBUTES =
            new HashMap<>();

    /**
     * Tooltip border templates loaded from resource packs.
     * Cleared on logout alongside CACHED_ATTRIBUTES.
     */
    public static final List<BorderTemplate> BORDER_TEMPLATES = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Mod bus events
    // -------------------------------------------------------------------------

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(TieredNeo.REFORGE_MENU.get(), ReforgeScreen::new);
            TieredClientPacketHandler.init();
            TabNavigationHelper.init();
            TieredNeo.LOGGER.info("[TieredNeo] Client setup complete.");
        });
    }

    @SubscribeEvent
    public static void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new TooltipBorderLoader());
    }

    // -------------------------------------------------------------------------
    // Game bus events (client) — logout cache cleanup
    // Note: logout events fire on the game bus, not the mod bus.
    // The @EventBusSubscriber above covers the mod bus only, so we register
    // the logout handler manually in onClientSetup via NeoForge.EVENT_BUS.
    //
    // Alternative: use a separate @EventBusSubscriber(bus = Bus.GAME) class.
    // We chose the separate class approach to keep responsibilities clear.
    // See TieredClientEvents.java.
    // -------------------------------------------------------------------------
}