package com.herobrot.tieredneo.client;

import com.herobrot.tieredneo.TieredNeo;
import com.herobrot.tieredneo.TieredNeoClient;
import com.herobrot.tieredneo.api.BorderTemplate;
import com.herobrot.tieredneo.api.ModifierUtils;
import com.herobrot.tieredneo.api.PotentialAttribute;
import com.herobrot.tieredneo.config.ConfigInit;
import com.herobrot.tieredneo.util.TieredTooltip;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
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

        BorderTemplate matchedTemplate = null;
        for (BorderTemplate template : TieredNeoClient.BORDER_TEMPLATES) {
            if (template.containsDecider(tierId.toString())) {
                matchedTemplate = template;
                break;
            }
        }

        if (matchedTemplate != null) {
            event.setCanceled(true);
            TieredTooltip.renderTieredTooltip(
                    event.getGraphics(),
                    event.getFont(),
                    event.getComponents(),
                    event.getX(),
                    event.getY(),
                    matchedTemplate
            );
        }
    }

    // --- SISTEMA DE COLOREADO NATIVO ---

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (!ConfigInit.CONFIG.tieredTooltipAttributes) return;

        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        ResourceLocation tierId = ModifierUtils.getAttributeId(stack);
        if (tierId == null) return;

        boolean isLegendary = ConfigInit.CONFIG.legendaryColorsForAttributes &&
                (tierId.getPath().toLowerCase().contains("legendary") || tierId.getPath().toLowerCase().contains("unique"));

        TextColor legendaryColor = null;
        if (isLegendary) {
            PotentialAttribute attr = TieredNeo.ATTRIBUTE_DATA_LOADER.getItemAttributes().get(tierId);
            if (attr != null && attr.getStyle() != null) {
                legendaryColor = attr.getStyle().getColor();
            }
        }

        List<Component> tooltip = event.getToolTip();

        for (int i = 0; i < tooltip.size(); i++) {
            Component line = tooltip.get(i);
            int modType = getModifierType(line);

            if (modType == 1 && legendaryColor != null) {
                // Estadísticas Base -> Color Legendario (Si aplica)
                tooltip.set(i, forceColor(line, legendaryColor));
            } else if (modType == 2) {
                // Buffs Extra (+X) -> SIEMPRE Verdes
                tooltip.set(i, forceColor(line, ChatFormatting.GREEN));
            } else if (modType == 3) {
                // Debuffs Extra (-X) -> SIEMPRE Rojos
                tooltip.set(i, forceColor(line, ChatFormatting.RED));
            }
        }
    }

    /**
     * Escanea el componente detectando llaves de Vanilla y de NeoForge.
     * Retorna: 1 (Base), 2 (Buff Extra), 3 (Debuff Extra), 0 (Ninguno)
     */
    private static int getModifierType(Component component) {
        if (component.getContents() instanceof TranslatableContents tc) {
            String key = tc.getKey();

            // Estadísticas Base (Daño, Velocidad, etc)
            if (key.startsWith("attribute.modifier.equals.")) return 1;

            // Buffs (Detecta tanto el Vanilla antiguo como el formato NeoForge)
            if (key.startsWith("attribute.modifier.plus.") || key.startsWith("neoforge.modifier.plus")) return 2;

            // Debuffs (Detecta tanto el Vanilla antiguo como el formato NeoForge)
            if (key.startsWith("attribute.modifier.take.") || key.startsWith("neoforge.modifier.take")) return 3;

            // Escanear los argumentos internos de la traducción
            for (Object arg : tc.getArgs()) {
                if (arg instanceof Component argComp) {
                    int type = getModifierType(argComp);
                    if (type != 0) return type;
                }
            }
        }

        // Escanear los hermanos del componente actual
        for (Component sibling : component.getSiblings()) {
            int type = getModifierType(sibling);
            if (type != 0) return type;
        }

        return 0;
    }

    /**
     * Extrae el texto traducido final y le inyecta el color TextColor (Legendarios).
     */
    private static Component forceColor(Component component, TextColor newColor) {
        return Component.literal(component.getString()).withStyle(Style.EMPTY.withColor(newColor));
    }

    /**
     * Extrae el texto traducido final y le inyecta el ChatFormatting (Verde/Rojo).
     */
    private static Component forceColor(Component component, ChatFormatting format) {
        return Component.literal(component.getString()).withStyle(Style.EMPTY.applyFormat(format));
    }
}