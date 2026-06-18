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

    // -------------------------------------------------------------------------
    // Data Loaders (singletons shared across server and client)
    // -------------------------------------------------------------------------

    /** Handles loading data/tieredneo/item_attributes/*.json from datapacks */
    public static final AttributeDataLoader ATTRIBUTE_DATA_LOADER = new AttributeDataLoader();

    /** Handles loading data/tieredneo/reforge_items/*.json from datapacks */
    public static final ReforgeDataLoader REFORGE_DATA_LOADER = new ReforgeDataLoader();

    // -------------------------------------------------------------------------
    // Deferred Registers
    // -------------------------------------------------------------------------

    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MODID);

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, MODID);

    // -------------------------------------------------------------------------
    // Registry Entries
    // -------------------------------------------------------------------------

    /**
     * The core data component that stores a tier on an ItemStack.
     * Replaces Fabric's ComponentType<TierComponent> with NeoForge's DeferredHolder.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<TierDataComponent>> TIER =
            DATA_COMPONENTS.register("tier", () ->
                    DataComponentType.<TierDataComponent>builder()
                            .persistent(TierDataComponent.CODEC)
                            .networkSynchronized(TierDataComponent.STREAM_CODEC)
                            .build()
            );

    /**
     * Resolved DataComponentType reference for use with ItemStack has/get/set/remove.

     * DeferredHolder itself does not implement DataComponentType, so we cannot
     * pass TIER directly to ItemStack methods. Calling TIER.get() at registration
     * time would be too early (registries not yet frozen), so instead we expose
     * this accessor method which is safe to call any time after FML common setup.

     * Usage:
     *   stack.has(TieredNeo.TIER_TYPE)       // ✓
     *   stack.get(TieredNeo.TIER_TYPE)       // ✓
     *   stack.set(TieredNeo.TIER_TYPE, val)  // ✓
     *   stack.remove(TieredNeo.TIER_TYPE)    // ✓
     */
    public static DataComponentType<TierDataComponent> TIER_TYPE() {
        return TIER.get();
    }

    /**
     * The screen handler / menu for the reforging table UI.
     * Uses IMenuTypeExtension so the factory receives the extra FriendlyByteBuf data
     * that the server sends when opening the screen (BlockPos of the anvil).
     */
    public static final DeferredHolder<MenuType<?>, MenuType<ReforgeMenu>> REFORGE_MENU =
            MENU_TYPES.register("reforge", () ->
                    IMenuTypeExtension.create(ReforgeMenu::new)
            );

    // -------------------------------------------------------------------------
    // Constructor – mod entry point
    // -------------------------------------------------------------------------

    public TieredNeo(IEventBus modEventBus, ModContainer modContainer) {
        // Register deferred registers onto the mod event bus
        DATA_COMPONENTS.register(modEventBus);
        MENU_TYPES.register(modEventBus);

        // Register custom entity attributes (dig speed, crit chance, durable, range attack damage)
        CustomEntityAttributes.register(modEventBus);

        // Register networking channels and payload types
        TieredNetwork.register(modEventBus);

        // Subscribe to common setup for tasks that must run after registration
        modEventBus.addListener(this::commonSetup);

        // Register Cloth Config
        ConfigInit.init();
        if (FMLEnvironment.dist.isClient()) {
            TieredNeoClient.registerConfigScreen(modContainer);
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // TieredItemTags are defined declaratively; no runtime init needed.
        // CustomEntityAttributes are registered via DeferredRegister; no explicit call needed here.
        // Data loaders are registered in the server/client lifecycle events (see TieredEvents).
        LOGGER.info("[TieredNeo] Common setup complete.");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Convenience factory for ResourceLocations namespaced to this mod. */
    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}