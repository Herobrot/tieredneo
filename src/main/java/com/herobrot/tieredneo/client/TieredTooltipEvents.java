package com.herobrot.tieredneo.client;

import com.herobrot.tieredneo.TieredNeo;
import com.herobrot.tieredneo.TieredNeoClient;
import com.herobrot.tieredneo.api.BorderTemplate;
import com.herobrot.tieredneo.api.ModifierUtils;
import com.herobrot.tieredneo.api.PotentialAttribute;
import com.herobrot.tieredneo.init.ConfigInit;
import com.herobrot.tieredneo.util.TieredTooltip;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

@EventBusSubscriber(modid = TieredNeo.MODID, value = Dist.CLIENT)
public class TieredTooltipEvents {
    @SubscribeEvent
    public static void onRenderTooltipPre(RenderTooltipEvent.Pre event) {
        if (!ConfigInit.CONFIG.tieredTooltip) return;
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;
        ResourceLocation tierId = ModifierUtils.getAttributeId(stack);
        if (tierId == null) return;
        String tierIdStr = tierId.toString();
        for (BorderTemplate template : TieredNeoClient.BORDER_TEMPLATES) {
            if (template.containsDecider(tierIdStr)) {
                event.setCanceled(true);
                TieredTooltip.renderTieredTooltip(event.getGraphics(), event.getFont(), event.getComponents(),
                        event.getX(), event.getY(), template);
                return;
            }
        }
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (!ConfigInit.CONFIG.tieredTooltipAttributes) return;
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;
        ResourceLocation tierId = ModifierUtils.getAttributeId(stack);
        if (tierId == null) return;
        boolean isLegendary =
                ConfigInit.CONFIG.legendaryColorsForAttributes && (tierId.getPath().contains("legendary")
                        || tierId.getPath().contains("unique"));

        TextColor legendaryColor = null;
        if (isLegendary) {
            PotentialAttribute attr = TieredNeo.ATTRIBUTE_DATA_LOADER.getItemAttributes().get(tierId);
            if (attr != null && attr.getStyle() != null)
                legendaryColor = attr.getStyle().getColor();
        }
        List<Component> tooltip = event.getToolTip();
        for (int i = 0; i < tooltip.size(); i++) {
            Component line = tooltip.get(i);
            int modType = getModifierType(line);
            if (modType == 1 && legendaryColor != null) tooltip.set(i, forceColor(line, legendaryColor));
            else if (modType == 2) tooltip.set(i, forceColor(line, ChatFormatting.GREEN));
            else if (modType == 3) tooltip.set(i, forceColor(line, ChatFormatting.RED));
        }
    }

    private static int getModifierType(Component component) {
        if (component.getContents() instanceof TranslatableContents tc) {
            String key = tc.getKey();
            if (key.startsWith("attribute.modifier.equals.")) return 1;
            if (key.startsWith("attribute.modifier.plus.") || key.startsWith("neoforge.modifier.plus")) return 2;
            if (key.startsWith("attribute.modifier.take.") || key.startsWith("neoforge.modifier.take")) return 3;
            for (Object arg : tc.getArgs())
                if (arg instanceof Component argComp) {
                    int type = getModifierType(argComp);
                    if (type != 0) return type;
                }
        }
        for (Component sibling : component.getSiblings()) {
            int type = getModifierType(sibling);
            if (type != 0) return type;
        }
        return 0;
    }

    private static Component forceColor(Component component, TextColor newColor) {
        MutableComponent mutable = component.copy();
        mutable.setStyle(mutable.getStyle().withColor(newColor));
        mutable.getSiblings().replaceAll(sibling -> forceColor(sibling, newColor));
        return mutable;
    }

    private static Component forceColor(Component component, ChatFormatting format) {
        MutableComponent mutable = component.copy();
        mutable.setStyle(mutable.getStyle().applyFormat(format));
        mutable.getSiblings().replaceAll(sibling -> forceColor(sibling, format));
        return mutable;
    }
}