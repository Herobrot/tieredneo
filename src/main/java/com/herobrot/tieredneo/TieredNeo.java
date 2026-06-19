package com.herobrot.tieredneo;

import com.herobrot.tieredneo.api.CustomEntityAttributes;
import com.herobrot.tieredneo.api.TierDataComponent;
import com.herobrot.tieredneo.config.ConfigInit;
import com.herobrot.tieredneo.data.AttributeDataLoader;
import com.herobrot.tieredneo.data.ReforgeDataLoader;
import com.herobrot.tieredneo.network.TieredNetwork;
import com.herobrot.tieredneo.reforge.ReforgeMenu;
import com.mojang.logging.LogUtils;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import org.slf4j.Logger;

@Mod(TieredNeo.MODID)
public class TieredNeo {
    public static final String MODID = "tieredneo";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final AttributeDataLoader ATTRIBUTE_DATA_LOADER = new AttributeDataLoader();
    public static final ReforgeDataLoader REFORGE_DATA_LOADER = new ReforgeDataLoader();
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MODID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, MODID);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<TierDataComponent>> TIER =
            DATA_COMPONENTS.register("tier",
                    () -> DataComponentType.<TierDataComponent>builder().persistent(TierDataComponent.CODEC).networkSynchronized(TierDataComponent.STREAM_CODEC).build());

    public static DataComponentType<TierDataComponent> TIER_TYPE() {
        return TIER.get();
    }

    public static final DeferredHolder<MenuType<?>, MenuType<ReforgeMenu>> REFORGE_MENU = MENU_TYPES.register(
            "reforge", () -> IMenuTypeExtension.create(ReforgeMenu::new));

    public TieredNeo(IEventBus modEventBus, ModContainer modContainer) {

        DATA_COMPONENTS.register(modEventBus);
        MENU_TYPES.register(modEventBus);

        CustomEntityAttributes.register(modEventBus);

        TieredNetwork.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        ConfigInit.init();
        if (FMLEnvironment.dist.isClient()) {
            TieredNeoClient.registerConfigScreen(modContainer);
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {

        LOGGER.info("[TieredNeo] Common setup complete.");
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}