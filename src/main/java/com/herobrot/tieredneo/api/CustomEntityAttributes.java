package com.herobrot.tieredneo.api;

import com.herobrot.tieredneo.TieredNeo;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CustomEntityAttributes {
    private static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE,
            TieredNeo.MODID);
    public static final DeferredHolder<Attribute, Attribute> DIG_SPEED = ATTRIBUTES.register("generic.dig_speed",
            () -> new RangedAttribute("attribute.name.generic.dig_speed", 0.0, 0.0, 2048.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> CRIT_CHANCE = ATTRIBUTES.register("generic.crit_chance",
            () -> new RangedAttribute("attribute.name.generic.crit_chance", 0.0, 0.0, 1.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> DURABLE = ATTRIBUTES.register("generic.durable",
            () -> new RangedAttribute("attribute.name.generic.durable", 0.0, 0.0, 1.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> RANGE_ATTACK_DAMAGE = ATTRIBUTES.register("generic" +
            ".range_attack_damage", () -> new RangedAttribute("attribute.name.generic.range_attack_damage", 0.0, 0.0,
            2048.0).setSyncable(true));

    private CustomEntityAttributes() {}

    public static void register(IEventBus modEventBus) {
        ATTRIBUTES.register(modEventBus);
    }

    public static Holder<Attribute> digSpeed() {return DIG_SPEED;}

    public static Holder<Attribute> critChance() {return CRIT_CHANCE;}

    public static Holder<Attribute> durable() {return DURABLE;}

    public static Holder<Attribute> rangeAttackDamage() {return RANGE_ATTACK_DAMAGE;}
}