package com.herobrot.tieredneo.mixin;

import com.herobrot.heroslib.client.event.ITabbedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin implements ITabbedScreen {

    @Shadow protected int leftPos;
    @Shadow protected int topPos;

    @Override
    public int heroslib$getGuiLeft() {
        return this.leftPos;
    }

    @Override
    public int heroslib$getGuiTop() {
        return this.topPos;
    }

    @Override
    public Class<? extends Screen> heroslib$getParentScreenClass() {
        Class<?> clazz = this.getClass();
        return clazz.asSubclass(Screen.class);
    }
}