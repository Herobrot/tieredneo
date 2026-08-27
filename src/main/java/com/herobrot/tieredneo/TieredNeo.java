package com.herobrot.tieredneo;

import com.herobrot.tieredneo.init.ConfigInit;
import com.herobrot.tieredneo.data.AttributeDataLoader;
import com.herobrot.tieredneo.data.ReforgeDataLoader;
import com.herobrot.tieredneo.init.NetworkInit;
import com.herobrot.tieredneo.init.RegistrationInit;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod(TieredNeo.MODID)
public class TieredNeo {
    public static final String MODID = "tieredneo";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static boolean isHerosLevelsLoaded = false;

    public static final AttributeDataLoader ATTRIBUTE_DATA_LOADER = new AttributeDataLoader();
    public static final ReforgeDataLoader REFORGE_DATA_LOADER = new ReforgeDataLoader();

    public TieredNeo(IEventBus modEventBus, ModContainer modContainer) {
        RegistrationInit.register(modEventBus);
        NetworkInit.register();
        ConfigInit.init();
        verifyingModsInstalled();
        if (FMLEnvironment.dist.isClient())
            TieredNeoClient.registerConfigScreen(modContainer);
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    private void verifyingModsInstalled(){
        isHerosLevelsLoaded = isModLoaded("heroslevels");
    }

    public static boolean isModLoaded(String modTarget) { return ModList.get().isLoaded(modTarget); }

}