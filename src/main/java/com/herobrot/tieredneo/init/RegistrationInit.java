package com.herobrot.tieredneo.init;

import com.herobrot.tieredneo.TieredNeo;
import com.herobrot.tieredneo.api.CustomEntityAttributes;
import com.herobrot.tieredneo.api.TierDataComponent;
import com.herobrot.tieredneo.reforge.ReforgeMenu;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class RegistrationInit {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, TieredNeo.MODID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, TieredNeo.MODID);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<TierDataComponent>> TIER = DATA_COMPONENTS.register("tier", () -> DataComponentType.<TierDataComponent>builder().persistent(TierDataComponent.CODEC).networkSynchronized(TierDataComponent.STREAM_CODEC).build());
    public static final DeferredHolder<MenuType<?>, MenuType<ReforgeMenu>> REFORGE_MENU = MENU_TYPES.register("reforge", () -> IMenuTypeExtension.create(ReforgeMenu::new));

    private RegistrationInit() {}

    public static void register(IEventBus modEventBus) {
        DATA_COMPONENTS.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        CustomEntityAttributes.register(modEventBus);
    }

    public static DataComponentType<TierDataComponent> getTierType() {
        return TIER.get();
    }
}