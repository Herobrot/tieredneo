package com.herobrot.tieredneo.api;

import com.herobrot.tieredneo.TieredNeo;
import com.herobrot.tieredneo.config.ConfigInit;
import com.herobrot.tieredneo.util.WeightedList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ModifierUtils {
    private ModifierUtils() {}

    @Nullable
    public static ResourceLocation getRandomAttributeIDFor(@Nullable Player player, Item item, boolean reforge) {
        WeightedList<ResourceLocation> weighted = getResourceLocationWeightedList(item, reforge);

        if (weighted.isEmpty()) return null;

        if (reforge && weighted.size() > 2) {
            List<WeightedList.Entry<ResourceLocation>> sorted = weighted.sortedAscending();
            List<WeightedList.Entry<ResourceLocation>> dampened = getEntries(sorted);
            return WeightedList.fromEntries(dampened).draw();
        }

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
        Map<ResourceLocation, PotentialAttribute> allAttributes = TieredNeo.ATTRIBUTE_DATA_LOADER.getItemAttributes();

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
                dampened.add(entry.withWeight((int) (entry.weight() * (1.0f - luckMod * luck))));
            } else {
                dampened.add(entry);
            }
        }
        return dampened;
    }

    public static void setItemStackAttribute(@Nullable Player player, ItemStack stack, boolean reforge) {
        if (stack.has(TieredNeo.TIER_TYPE()) || stack.is(TieredItemTags.MODIFIER_RESTRICTED)) return;

        ResourceLocation id = getRandomAttributeIDFor(player, stack.getItem(), reforge);
        if (id == null) return;

        PotentialAttribute attribute = TieredNeo.ATTRIBUTE_DATA_LOADER.getItemAttributes().get(id);
        if (attribute != null) {
            applyTierToStack(stack, id, attribute);
        }
    }

    public static void removeItemStackAttribute(ItemStack stack) {
        stack.remove(TieredNeo.TIER_TYPE());
    }

    @Nullable
    public static ResourceLocation getAttributeId(ItemStack stack) {
        TierDataComponent component = stack.get(TieredNeo.TIER_TYPE());
        if (component == null || component.isPresent()) return null;
        return component.tierId();
    }

    public static void updateInventoryComponents(Inventory inventory) {
        Map<ResourceLocation, PotentialAttribute> snapshot =
                Map.copyOf(TieredNeo.ATTRIBUTE_DATA_LOADER.getItemAttributes());

        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty()) continue;

            TierDataComponent component = stack.get(TieredNeo.TIER_TYPE());
            if (component == null || component.isPresent()) continue;

            ResourceLocation tierId = component.tierId();
            Item stackItem = stack.getItem();
            boolean stillValid =
                    snapshot.entrySet().stream().anyMatch(e -> e.getValue().isValid(stackItem) && e.getKey().equals(tierId));

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

    public static void applyTierToStack(ItemStack stack, ResourceLocation tierId, PotentialAttribute attr) {
        float durableFactor = -1f;
        int operation = 2;

        for (AttributeTemplate t : attr.getAttributes()) {
            String typeId = t.getAttributeTypeID();
            if (typeId.equals("tiered:generic.durable") || typeId.equals("tieredneo:generic.durable")) {
                durableFactor = (float) Math.round(t.getEntityAttributeModifier().amount() * 100.0f) / 100.0f;
                operation = t.getEntityAttributeModifier().operation().ordinal();
                break;
            }
        }

        stack.set(TieredNeo.TIER_TYPE(), new TierDataComponent(tierId, durableFactor, operation));

        if (durableFactor > 0 && stack.has(DataComponents.MAX_DAMAGE)) {
            int currentMax = stack.getOrDefault(DataComponents.MAX_DAMAGE, 0);
            if (currentMax > 0) {
                int newMax = operation == 0 ? currentMax + (int) durableFactor :
                        currentMax + (int) (currentMax * durableFactor);
                stack.set(DataComponents.MAX_DAMAGE, newMax);
            }
        }

        Component pureBaseName = stack.getItem().getName(stack.getItem().getDefaultInstance());

        Component fullName =
                Component.empty().append(Component.translatable(attr.getID() + ".label").withStyle(attr.getStyle())).append(Component.literal(" ").withStyle(attr.getStyle())).append(pureBaseName.copy().withStyle(attr.getStyle()));

        stack.set(DataComponents.ITEM_NAME, fullName);
    }
}