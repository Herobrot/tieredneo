package com.herobrot.tieredneo.api;

import com.herobrot.tieredneo.TieredNeo;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Custom entity attributes introduced by this mod.

 * All four mirror the originals from TieredZ, with the same IDs and ranges.
 * Using DeferredRegister instead of Registry.registerReference (Fabric) —
 * no mixin or AT required.
 */
public final class CustomEntityAttributes {

    private static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(Registries.ATTRIBUTE, TieredNeo.MODID);

    // -------------------------------------------------------------------------
    // Attribute definitions
    // -------------------------------------------------------------------------

    /**
     * Mining/digging speed bonus. Range [0, 2048].
     * Used in item_attributes datapacks as "tieredneo:generic.dig_speed".
     */
    public static final DeferredHolder<Attribute, Attribute> DIG_SPEED =
            ATTRIBUTES.register("generic.dig_speed", () ->
                    new RangedAttribute("attribute.name.generic.dig_speed", 0.0, 0.0, 2048.0)
                            .setSyncable(true)
            );

    /**
     * Critical hit chance (0.0 – 1.0, i.e. 0%–100%).
     * Used in item_attributes datapacks as "tieredneo:generic.crit_chance".
     */
    public static final DeferredHolder<Attribute, Attribute> CRIT_CHANCE =
            ATTRIBUTES.register("generic.crit_chance", () ->
                    new RangedAttribute("attribute.name.generic.crit_chance", 0.0, 0.0, 1.0)
                            .setSyncable(true)
            );

    /**
     * Durability factor — controls how the tier affects item durability.
     * Range [0, 1]. Used internally as "tieredneo:generic.durable".
     */
    public static final DeferredHolder<Attribute, Attribute> DURABLE =
            ATTRIBUTES.register("generic.durable", () ->
                    new RangedAttribute("attribute.name.generic.durable", 0.0, 0.0, 1.0)
                            .setSyncable(true)
            );

    /**
     * Ranged/projectile attack damage bonus. Range [0, 2048].
     * Used in item_attributes datapacks as "tieredneo:generic.range_attack_damage".
     */
    public static final DeferredHolder<Attribute, Attribute> RANGE_ATTACK_DAMAGE =
            ATTRIBUTES.register("generic.range_attack_damage", () ->
                    new RangedAttribute("attribute.name.generic.range_attack_damage", 0.0, 0.0, 2048.0)
                            .setSyncable(true)
            );

    // -------------------------------------------------------------------------

    private CustomEntityAttributes() {}

    /**
     * Call this from the mod constructor to attach the DeferredRegister to the mod event bus.
     */
    public static void register(IEventBus modEventBus) {
        ATTRIBUTES.register(modEventBus);
    }

    // -------------------------------------------------------------------------
    // Convenience accessor — returns the Holder for use in AttributeTemplate
    // -------------------------------------------------------------------------

    public static Holder<Attribute> digSpeed()          { return DIG_SPEED; }
    public static Holder<Attribute> critChance()        { return CRIT_CHANCE; }
    public static Holder<Attribute> durable()           { return DURABLE; }
    public static Holder<Attribute> rangeAttackDamage() { return RANGE_ATTACK_DAMAGE; }
}