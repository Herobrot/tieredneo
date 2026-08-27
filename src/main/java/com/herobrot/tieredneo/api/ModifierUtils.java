package com.herobrot.tieredneo.api;

import com.herobrot.heroslevels.api.HerosLevelsAPI;
import com.herobrot.heroslevels.level.LevelManager;
import com.herobrot.heroslevels.level.Skill;
import com.herobrot.tieredneo.TieredNeo;
import com.herobrot.tieredneo.init.ConfigInit;
import com.herobrot.tieredneo.init.RegistrationInit;
import com.herobrot.tieredneo.util.WeightedList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
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
        List<WeightedList.Entry<ResourceLocation>> currentWeights = weighted.sortedAscending();
        boolean appliedModifier = false;
        if (reforge && currentWeights.size() > 2) {
            currentWeights = getEntries(currentWeights, ConfigInit.CONFIG.reforgeModifier, 2, 0f);
            appliedModifier = true;
        }
        if (player != null) {
            float luck = player.getLuck();
            if (luck != 0f) {
                currentWeights = getEntries(currentWeights, ConfigInit.CONFIG.luckReforgeModifier, 3, luck);
                appliedModifier = true;
            }
            if (TieredNeo.isHerosLevelsLoaded && player instanceof ServerPlayer serverPlayer) {
                int smithingLevel = getSmithingLevel(serverPlayer);
                if (smithingLevel > 0) {
                    currentWeights = getEntries(currentWeights, ConfigInit.CONFIG.herosLevelsReforgeModifier, 3, smithingLevel);
                    appliedModifier = true;
                }
            }
        }
        if (appliedModifier) return WeightedList.fromEntries(currentWeights).draw();
        return weighted.draw();
    }

    private static int getSmithingLevel(ServerPlayer player) {
        for (Skill skill : LevelManager.SKILLS.values())
            if (skill.key().equals("smithing"))
                return HerosLevelsAPI.getSkillLevel(player, skill.id());
        return 0;
    }

    private static @NotNull WeightedList<ResourceLocation> getResourceLocationWeightedList(Item item, boolean reforge) {
        Map<ResourceLocation, PotentialAttribute> allAttributes = TieredNeo.ATTRIBUTE_DATA_LOADER.getItemAttributes();
        WeightedList<ResourceLocation> weighted = new WeightedList<>();
        allAttributes.forEach((id, attribute) -> {
            if (attribute.isValid(item)) {
                int w = attribute.getWeight();
                if (w > 0 || reforge) weighted.add(reforge ? w + 1 : w, id);
            }
        });
        return weighted;
    }

    private static @NotNull List<WeightedList.Entry<ResourceLocation>> getEntries(List<WeightedList.Entry<ResourceLocation>> sorted, float modifier, int divisor, float dynamicLevel) {
        int maxWeight = sorted.getLast().weight();
        List<WeightedList.Entry<ResourceLocation>> dampened = new ArrayList<>();
        for (WeightedList.Entry<ResourceLocation> entry : sorted) {
            if (entry.weight() > maxWeight / divisor) {
                float finalModifier = dynamicLevel > 0 ? (1.0f - modifier * dynamicLevel) : modifier;
                dampened.add(entry.withWeight((int) (entry.weight() * finalModifier)));
            } else dampened.add(entry);
        }
        return dampened;
    }

    public static void setItemStackAttribute(@Nullable Player player, ItemStack stack, boolean reforge) {
        if (stack.has(RegistrationInit.getTierType()) || stack.is(TieredItemTags.MODIFIER_RESTRICTED)) return;
        ResourceLocation id = getRandomAttributeIDFor(player, stack.getItem(), reforge);
        if (id == null) return;
        PotentialAttribute attribute = TieredNeo.ATTRIBUTE_DATA_LOADER.getItemAttributes().get(id);
        if (attribute != null) applyTierToStack(stack, id, attribute);
    }

    public static void removeItemStackAttribute(ItemStack stack) {
        stack.remove(RegistrationInit.getTierType());
    }

    @Nullable
    public static ResourceLocation getAttributeId(ItemStack stack) {
        TierDataComponent component = stack.get(RegistrationInit.getTierType());
        if (component == null || component.isEmpty()) return null;
        return component.tierId();
    }

    public static void updateInventoryComponents(Inventory inventory) {
        Map<ResourceLocation, PotentialAttribute> snapshot = TieredNeo.ATTRIBUTE_DATA_LOADER.getItemAttributes();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty()) continue;
            TierDataComponent component = stack.get(RegistrationInit.getTierType());
            if (component == null || component.isEmpty()) continue;
            ResourceLocation tierId = component.tierId();
            Item stackItem = stack.getItem();
            PotentialAttribute attr = snapshot.get(tierId);
            boolean stillValid = attr != null && attr.isValid(stackItem);
            if (!stillValid) {
                stack.remove(RegistrationInit.getTierType());
                ResourceLocation newId = getRandomAttributeIDFor(null, stackItem, false);
                if (newId != null) {
                    PotentialAttribute newAttr = snapshot.get(newId);
                    if (newAttr != null) applyTierToStack(stack, newId, newAttr);
                }
                inventory.setItem(slot, stack);
            } else {
                applyTierToStack(stack, tierId, attr);
                inventory.setItem(slot, stack);
            }
        }
    }

    public static void applyTierToStack(ItemStack stack, ResourceLocation tierId, PotentialAttribute attr) {
        float durableFactor = -1f;
        AttributeModifier.Operation operation = AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
        for (AttributeTemplate t : attr.getAttributes()) {
            String typeId = t.getAttributeTypeID();
            if (typeId.equals(AttributeTemplate.DURABLE_ID) || typeId.equals(AttributeTemplate.LEGACY_DURABLE_ID)) {
                durableFactor = (float) Math.round(t.getEntityAttributeModifier().amount() * 100.0f) / 100.0f;
                operation = t.getEntityAttributeModifier().operation();
                break;
            }
        }
        stack.set(RegistrationInit.getTierType(), new TierDataComponent(tierId, durableFactor, operation.ordinal()));
        if (durableFactor > 0 && stack.has(DataComponents.MAX_DAMAGE)) {
            int baseMax = stack.getItem().getDefaultInstance().getOrDefault(DataComponents.MAX_DAMAGE, 0);
            if (baseMax > 0) {
                int newMax = operation == AttributeModifier.Operation.ADD_VALUE ?
                        baseMax + (int) durableFactor :
                        baseMax + (int) (baseMax * durableFactor);
                stack.set(DataComponents.MAX_DAMAGE, newMax);
            }
        }
        Component customName = stack.get(DataComponents.CUSTOM_NAME);
        boolean hasCustomName = customName != null;
        Component pureBaseName;
        if (stack.has(RegistrationInit.getTierType()))
            pureBaseName = hasCustomName ? customName : stack.getItem().getName(stack);
        else pureBaseName = stack.getHoverName();
        Component fullName =
                Component.empty()
                        .append(Component.translatable(attr.getID() + ".label").withStyle(attr.getStyle()))
                        .append(Component.literal(" ").withStyle(attr.getStyle()))
                        .append(pureBaseName.copy().withStyle(attr.getStyle()));

        if (hasCustomName) stack.set(DataComponents.CUSTOM_NAME, fullName);
        else stack.set(DataComponents.ITEM_NAME, fullName);
    }
}