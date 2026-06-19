package com.herobrot.tieredneo.events;

import com.herobrot.tieredneo.TieredNeo;
import com.herobrot.tieredneo.api.CustomEntityAttributes;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;

@EventBusSubscriber(modid = TieredNeo.MODID)
public class TieredModEvents {
    @SubscribeEvent
    public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, CustomEntityAttributes.DIG_SPEED);
        event.add(EntityType.PLAYER, CustomEntityAttributes.CRIT_CHANCE);
        event.add(EntityType.PLAYER, CustomEntityAttributes.DURABLE);
        event.add(EntityType.PLAYER, CustomEntityAttributes.RANGE_ATTACK_DAMAGE);
    }
}