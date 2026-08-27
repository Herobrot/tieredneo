package com.herobrot.tieredneo.init;

import com.herobrot.heroslib.client.tab.TabDefinition;
import com.herobrot.heroslib.client.tab.TabRegistry;
import com.herobrot.heroslib.config.ConfigSyncHelper;
import com.herobrot.heroslib.config.HerosConfigManager;
import com.herobrot.tieredneo.TieredNeo;
import com.herobrot.tieredneo.config.TieredConfig;
import com.herobrot.tieredneo.network.payload.ReforgeScreenPayload;
import com.herobrot.tieredneo.reforge.ReforgeScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ClientInit {
    public static final ResourceLocation ANVIL_ICON = TieredNeo.rl("textures/gui/anvil_tab_icon.png");
    public static final ResourceLocation REFORGE_ICON = TieredNeo.rl("textures/gui/reforge_tab_icon.png");

    private ClientInit() {}

    public static void setup() {
        TabRegistry.registerTab(AnvilScreen.class, TabDefinition.builder(
                                TieredNeo.rl("anvil_tab"),
                                Component.translatable("container.repair"),
                                AnvilScreen.class,
                                1
                        )
                        .icon(ANVIL_ICON)
                        .onClick(() -> {
                            if (Minecraft.getInstance().screen instanceof ReforgeScreen) {
                                PacketDistributor.sendToServer(new ReforgeScreenPayload(false));
                            }
                        })
                        .needsMouseFix(true)
                        .showCondition(() -> ConfigInit.CONFIG.showReforgingTab)
                        .build()
        );

        TabRegistry.registerTab(AnvilScreen.class, TabDefinition.builder(
                                TieredNeo.rl("reforge_tab"),
                                Component.translatable("screen.tieredneo.container.reforge"),
                                ReforgeScreen.class,
                                2
                        )
                        .icon(REFORGE_ICON)
                        .onClick(() -> {
                            if (Minecraft.getInstance().screen instanceof AnvilScreen) {
                                PacketDistributor.sendToServer(new ReforgeScreenPayload(true));
                            }
                        })
                        .needsMouseFix(true)
                        .showCondition(() -> ConfigInit.CONFIG.showReforgingTab)
                        .build()
        );

        HerosConfigManager.registerClientSync(TieredNeo.MODID, (jsonReceived) -> {
            TieredConfig serverConfig = ConfigSyncHelper.SYNC_GSON.fromJson(jsonReceived, TieredConfig.class);
            if (serverConfig != null) {
                ConfigSyncHelper.mergeServerConfig(ConfigInit.CONFIG, serverConfig);
                TieredNeo.LOGGER.info("[TieredNeo]: Server configuration applied successfully.");
            }
        });
    }
}