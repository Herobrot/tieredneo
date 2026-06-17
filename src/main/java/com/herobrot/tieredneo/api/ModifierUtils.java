package com.herobrot.tieredneo.api;

import com.herobrot.tieredneo.TieredNeo;
import com.herobrot.tieredneo.config.ConfigInit;
import com.herobrot.tieredneo.config.TieredConfig;
import com.herobrot.tieredneo.util.WeightedList;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Core utility for assigning, removing, and updating tier data components
 * on ItemStacks.

 * Ported from Fabric's ModifierUtils with the following changes:
 *  - SortList replaced by WeightedList<ResourceLocation>
 *  - NBT manipulation replaced by DataComponents (stack.set / stack.has / stack.remove)
 *  - TierDataComponent.tier is now a ResourceLocation, not a String
 *  - Dual namespace check ("tiered:generic.durable" + "tieredneo:generic.durable")
 *    for TieredZ datapack backwards compatibility
 *  - getOperationId() removed; AttributeModifier.Operation.ordinal() used directly
 *  - LevelZ integration removed (Fabric-only mod, no NeoForge port exists)
 *  - Luck modifier preserved (vanilla Player.getLuck() works on NeoForge)
 */
public final class ModifierUtils {

    private ModifierUtils() {}

    // -------------------------------------------------------------------------
    // Weighted random selection
    // -------------------------------------------------------------------------

    /**
     * Picks a random PotentialAttribute ID that is valid for the given item,
     * using weighted probability. Returns null if no attributes are valid.

     * When reforge=true:
     *  - All attributes with weight >= 0 are eligible (including weight-0).
     *  - Each eligible weight gets +1 before dampening (prevents 0-weight starvation).
     *  - The heaviest half of entries is dampened by ConfigInit.reforgeModifier
     *    to make the reforge feel "fair" rather than always producing the same tier.

     * Luck modifier:
     *  - Dampens the heaviest third proportionally to player luck.
     *  - Higher luck → heavier entries dampened more → better tiers more likely.
     */

    @Nullable
    public static ResourceLocation getRandomAttributeIDFor(@Nullable Player player,
                                                           Item item,
                                                           boolean reforge) {
        WeightedList<ResourceLocation> weighted = getResourceLocationWeightedList(item, reforge);

        if (weighted.isEmpty()) return null;

        // Apply reforge dampening to the heaviest entries
        if (reforge && weighted.size() > 2) {
            List<WeightedList.Entry<ResourceLocation>> sorted = weighted.sortedAscending();
            List<WeightedList.Entry<ResourceLocation>> dampened = getEntries(sorted);
            return WeightedList.fromEntries(dampened).draw();
        }

        // Apply luck dampening if a player is present
        if (player != null) {
            float luck = player.getLuck();
            if (luck != 0f) {
                List<WeightedList.Entry<ResourceLocation>> sorted = weighted.sortedAscending();
                List<WeightedList.Entry<ResourceLocation>> dampened = getEntries(sorted, luck);
                return WeightedList.fromEntries(dampened).draw();
            }
        }

        return weighted.draw();
    }

    private static @NotNull WeightedList<ResourceLocation> getResourceLocationWeightedList(Item item, boolean reforge) {
        Map<ResourceLocation, PotentialAttribute> allAttributes =
                TieredNeo.ATTRIBUTE_DATA_LOADER.getItemAttributes();

        // Build initial weighted list
        WeightedList<ResourceLocation> weighted = new WeightedList<>();
        allAttributes.forEach((id, attribute) -> {
            if (attribute.isValid(item)) {
                int w = attribute.getWeight();
                if (w > 0 || reforge) {
                    weighted.add(reforge ? w + 1 : w, id);
                }
            }
        });
        return weighted;
    }

    private static @NotNull List<WeightedList.Entry<ResourceLocation>> getEntries(List<WeightedList.Entry<ResourceLocation>> sorted) {
        int maxWeight = sorted.getLast().weight();
        float modifier = ConfigInit.CONFIG.reforgeModifier;
        List<WeightedList.Entry<ResourceLocation>> dampened = new ArrayList<>();
        for (WeightedList.Entry<ResourceLocation> entry : sorted) {
            if (entry.weight() > maxWeight / 2) {
                dampened.add(entry.withWeight((int) (entry.weight() * modifier)));
            } else {
                dampened.add(entry);
            }
        }
        return dampened;
    }

    private static @NotNull List<WeightedList.Entry<ResourceLocation>> getEntries(List<WeightedList.Entry<ResourceLocation>> sorted, float luck) {
        int maxWeight = sorted.getLast().weight();
        float luckMod = ConfigInit.CONFIG.luckReforgeModifier;
        List<WeightedList.Entry<ResourceLocation>> dampened = new ArrayList<>();
        for (WeightedList.Entry<ResourceLocation> entry : sorted) {
            if (entry.weight() > maxWeight / 3) {
                dampened.add(entry.withWeight(
                        (int) (entry.weight() * (1.0f - luckMod * luck))
                ));
            } else {
                dampened.add(entry);
            }
        }
        return dampened;
    }

    // -------------------------------------------------------------------------
    // Component management
    // -------------------------------------------------------------------------

    /**
     * Assigns a random tier to an ItemStack if it has none and is not restricted.

     * Also caches the durable factor and operation ordinal directly into the
     * component so they can be read without a map lookup during tick events.
     */
    public static void setItemStackAttribute(@Nullable Player player,
                                             ItemStack stack,
                                             boolean reforge) {
        if (stack.has(TieredNeo.TIER_TYPE()) || stack.is(TieredItemTags.MODIFIER_RESTRICTED)) return;

        ResourceLocation id = getRandomAttributeIDFor(player, stack.getItem(), reforge);
        if (id == null) return;

        PotentialAttribute attribute = TieredNeo.ATTRIBUTE_DATA_LOADER.getItemAttributes().get(id);
        if (attribute != null) {
            applyTierToStack(stack, id, attribute);
        }
    }

    /** Removes the tier component from an ItemStack, if present. */
    public static void removeItemStackAttribute(ItemStack stack) {
        stack.remove(TieredNeo.TIER_TYPE());
    }

    /**
     * Returns the tier ResourceLocation of the given stack, or null.
     * Replaces the scattered stack.get(Tiered.TIER).tier() calls.
     */
    @Nullable
    public static ResourceLocation getAttributeId(ItemStack stack) {
        TierDataComponent component = stack.get(TieredNeo.TIER_TYPE());
        if (component == null || !component.isPresent()) return null;
        return component.tierId();
    }

    // -------------------------------------------------------------------------
    // Inventory sync after datapack reload
    // -------------------------------------------------------------------------

    /**
     * Called after a datapack reload to verify that each tiered item in the
     * player's inventory still has a valid attribute. If the attribute no longer
     * exists in the reloaded data, the component is replaced with a freshly
     * rolled one from the new dataset.

     * Uses Map.copyOf() snapshot of the attribute map to avoid reading a
     * partially-updated map if the reload races with a tick.
     */
    public static void updateInventoryComponents(Inventory inventory) {
        Map<ResourceLocation, PotentialAttribute> snapshot =
                Map.copyOf(TieredNeo.ATTRIBUTE_DATA_LOADER.getItemAttributes());

        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty()) continue;

            TierDataComponent component = stack.get(TieredNeo.TIER_TYPE());
            if (component == null || !component.isPresent()) continue;

            ResourceLocation tierId = component.tierId();
            Item stackItem = stack.getItem();
            boolean stillValid = snapshot.entrySet().stream()
                    .anyMatch(e -> e.getValue().isValid(stackItem)
                            && e.getKey().equals(tierId));

            if (!stillValid) {
                stack.remove(TieredNeo.TIER_TYPE());
                ResourceLocation newId = getRandomAttributeIDFor(null, stackItem, false);
                if (newId != null) {
                    PotentialAttribute attr = snapshot.get(newId);
                    if (attr != null) {
                        applyTierToStack(stack, newId, attr);
                    }
                }
                inventory.setItem(slot, stack);
            } else {
                PotentialAttribute attr = snapshot.get(tierId);
                if (attr != null) {
                    applyTierToStack(stack, tierId, attr);
                    inventory.setItem(slot, stack);
                }
            }
        }
    }

    /**
     * Helper centralizado para buscar el modificador de durabilidad y aplicar el TierDataComponent.
     */
    private static void applyTierToStack(ItemStack stack, ResourceLocation tierId, PotentialAttribute attr) {
        float durableFactor = -1f;
        int operation = 2; // ADD_MULTIPLIED_TOTAL default

        for (AttributeTemplate t : attr.getAttributes()) {
            String typeId = t.getAttributeTypeID();
            if (typeId.equals("tiered:generic.durable") || typeId.equals("tieredneo:generic.durable")) {
                durableFactor = (float) Math.round(t.getEntityAttributeModifier().amount() * 100.0f) / 100.0f;
                operation = t.getEntityAttributeModifier().operation().ordinal();
                break;
            }
        }

        stack.set(TieredNeo.TIER_TYPE(), new TierDataComponent(tierId, durableFactor, operation));
    }
}