package com.herobrot.tieredneo.mixin.compat.easyanvils;

import com.herobrot.heroslib.client.event.ITabbedScreen;
import fuzs.easyanvils.client.gui.screens.inventory.ModAnvilScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ModAnvilScreen.class)
public abstract class ModAnvilScreenMixin implements ITabbedScreen {

    @Override
    public Class<? extends Screen> heroslib$getParentScreenClass() {
        return AnvilScreen.class;
    }
}