package com.herobrot.tieredneo;

import com.herobrot.tieredneo.api.BorderTemplate;
import com.herobrot.tieredneo.api.PotentialAttribute;
import com.herobrot.tieredneo.config.TieredConfig;
import com.herobrot.tieredneo.data.TooltipBorderLoader;
import com.herobrot.tieredneo.init.ClientInit;
import com.herobrot.tieredneo.init.RegistrationInit;
import com.herobrot.tieredneo.reforge.ReforgeScreen;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod(value = TieredNeo.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = TieredNeo.MODID, value = Dist.CLIENT)
public class TieredNeoClient {
    public static final Map<ResourceLocation, PotentialAttribute> CACHED_ATTRIBUTES = new HashMap<>();
    public static final List<BorderTemplate> BORDER_TEMPLATES = new ArrayList<>();

    public static void registerConfigScreen(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (modContainer, parentScreen) -> AutoConfig.getConfigScreen(TieredConfig.class, parentScreen).get());
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(ClientInit::setup);
    }

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(RegistrationInit.REFORGE_MENU.get(), ReforgeScreen::new);
    }

    @SubscribeEvent
    public static void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new TooltipBorderLoader());
    }
}