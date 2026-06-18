package com.herobrot.tieredneo.mixin;

import com.herobrot.tieredneo.api.ModifierUtils;
import com.herobrot.tieredneo.config.ConfigInit;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LootTable.class)
public class LootTableMixin {

    /**
     * Inyectamos al final de la generación de ítems para cofres.
     * En 1.21.1, getRandomItems devuelve la lista final de ítems generados.
     * Iteramos sobre ella y aplicamos atributos si la configuración lo permite.
     */
    @Inject(
            method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;",
            at = @At("RETURN")
    )
    private void onGetRandomItems(LootParams params, CallbackInfoReturnable<ObjectArrayList<ItemStack>> cir) {
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