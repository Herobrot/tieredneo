package com.herobrot.tieredneo.events;

import com.herobrot.tieredneo.TieredNeo;
import com.herobrot.tieredneo.api.CustomEntityAttributes;
import com.herobrot.tieredneo.api.ModifierUtils;
import com.herobrot.tieredneo.config.ConfigInit;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.TradeWithVillagerEvent;

@EventBusSubscriber(modid = TieredNeo.MODID)
public class TieredGameplayEvents {
    @SubscribeEvent
    @SuppressWarnings("resource")
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (!event.getEntity().level().isClientSide && ConfigInit.CONFIG.craftingModifier) {
            ModifierUtils.setItemStackAttribute(event.getEntity(), event.getCrafting(), false);
        }
    }

    @SubscribeEvent
    public static void onMobSpawn(FinalizeSpawnEvent event) {
        if (!event.getLevel().isClientSide() && ConfigInit.CONFIG.entityItemModifier) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack stack = event.getEntity().getItemBySlot(slot);
                if (!stack.isEmpty()) {
                    ModifierUtils.setItemStackAttribute(null, stack, false);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onVillagerTrade(TradeWithVillagerEvent event) {
        if (ConfigInit.CONFIG.merchantModifier) {

            ModifierUtils.setItemStackAttribute(event.getEntity(), event.getMerchantOffer().getResult(), false);
        }
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        double digSpeedBonus = player.getAttributeValue(CustomEntityAttributes.DIG_SPEED);

        if (digSpeedBonus > 0) {

            event.setNewSpeed((float) (event.getNewSpeed() + digSpeedBonus));
        }
    }

    @SubscribeEvent
    public static void onCriticalHit(CriticalHitEvent event) {
        Player player = event.getEntity();
        double critChance = player.getAttributeValue(CustomEntityAttributes.CRIT_CHANCE);

        if (critChance > 0 && player.getRandom().nextFloat() < critChance) {
            event.setCriticalHit(true);
            if (event.getDamageMultiplier() <= 1.0f) {
                event.setDamageMultiplier(1.5f);
            }
        }
    }

    @SubscribeEvent
    public static void onProjectileJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof AbstractArrow arrow && arrow.getOwner() instanceof Player player) {
            double rangeBonus = player.getAttributeValue(CustomEntityAttributes.RANGE_ATTACK_DAMAGE);

            if (rangeBonus > 0) {
                arrow.setBaseDamage(arrow.getBaseDamage() + rangeBonus);
            }
        }
    }
}