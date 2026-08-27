package com.herobrot.tieredneo.mixin;

import com.herobrot.tieredneo.api.ModifierUtils;
import com.herobrot.tieredneo.init.ConfigInit;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({CraftingMenu.class, InventoryMenu.class, MerchantMenu.class})
public class MenuShiftClickMixin {

    @Inject(method = "quickMoveStack", at = @At("HEAD"))
    @SuppressWarnings({"resource", "PatternVariableCanNeverMatch", "ConstantConditions"})
    private void onQuickMoveStack(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
        if (player.level().isClientSide) return;
        Object menu = this;
        switch (menu) {
            case CraftingMenu craftingMenu when index == 0 -> {
                if (ConfigInit.CONFIG.craftingModifier)
                    tieredneo_1_21_1$applyTierToSlot(craftingMenu.slots.getFirst(), player);
            }
            case InventoryMenu inventoryMenu when index == 0 -> {
                if (ConfigInit.CONFIG.craftingModifier)
                    tieredneo_1_21_1$applyTierToSlot(inventoryMenu.slots.getFirst(), player);
            }
            case MerchantMenu merchantMenu when index == 2 -> {
                if (ConfigInit.CONFIG.merchantModifier)
                    tieredneo_1_21_1$applyTierToSlot(merchantMenu.slots.get(2), player);
            }
            default -> {}
        }
    }

    @Unique
    private void tieredneo_1_21_1$applyTierToSlot(Slot slot, Player player) {
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            ModifierUtils.setItemStackAttribute(player, stack, false);
        }
    }
}