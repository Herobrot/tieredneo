package com.herobrot.tieredneo;

import com.herobrot.tieredneo.api.CustomEntityAttributes;
import com.herobrot.tieredneo.api.ModifierUtils;
import com.herobrot.tieredneo.config.ConfigInit;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.TradeWithVillagerEvent;

/**
 * Reemplaza a ItemMixin, MobEntityMixin, MerchantScreenHandlerMixin y parte de PlayerEntityMixin.
 */
@EventBusSubscriber(modid = TieredNeo.MODID)
public class TieredGameplayEvents {

    // -------------------------------------------------------------------------
    // Asignación de Tiers
    // -------------------------------------------------------------------------

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
            // Se le asigna el tier al ítem que el jugador está a punto de comprar
            ModifierUtils.setItemStackAttribute(event.getEntity(), event.getMerchantOffer().getResult(), false);
        }
    }

    // -------------------------------------------------------------------------
    // Lectura de Atributos Personalizados
    // -------------------------------------------------------------------------

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        double digSpeedBonus = player.getAttributeValue(CustomEntityAttributes.DIG_SPEED);

        if (digSpeedBonus > 0) {
            // Sumamos el bono de velocidad de minado al cálculo original
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
}