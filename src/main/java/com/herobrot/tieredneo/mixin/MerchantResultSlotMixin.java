package com.herobrot.tieredneo.mixin;

import com.herobrot.tieredneo.api.ModifierUtils;
import com.herobrot.tieredneo.init.ConfigInit;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantResultSlot;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantResultSlot.class)
public class MerchantResultSlotMixin {

    @Inject(method = "onTake", at = @At("HEAD"))
    @SuppressWarnings("resource")
    private void onTradeTake(Player player, ItemStack stack, CallbackInfo ci) {
        if (!player.level().isClientSide && ConfigInit.CONFIG.merchantModifier) {
            ModifierUtils.setItemStackAttribute(player, stack, false);
        }
    }
}