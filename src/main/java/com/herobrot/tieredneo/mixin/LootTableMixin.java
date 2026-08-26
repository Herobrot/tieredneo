package com.herobrot.tieredneo.mixin;

import com.herobrot.tieredneo.api.ModifierUtils;
import com.herobrot.tieredneo.init.ConfigInit;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LootTable.class)
public class LootTableMixin {

    @Inject(method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;", at = @At("RETURN"))
    private void onGetRandomItems(LootContext context, CallbackInfoReturnable<ObjectArrayList<ItemStack>> cir) {
        if (ConfigInit.CONFIG.lootContainerModifier) {
            ObjectArrayList<ItemStack> generatedLoot = cir.getReturnValue();

            if (generatedLoot != null && !generatedLoot.isEmpty()) {
                for (ItemStack stack : generatedLoot) {
                    if (!stack.isEmpty()) {
                        ModifierUtils.setItemStackAttribute(null, stack, false);
                    }
                }
            }
        }
    }
}