package com.herobrot.tieredneo.client;

import com.herobrot.tieredneo.TieredNeo;
import com.herobrot.tieredneo.TieredNeoClient;
import com.herobrot.tieredneo.api.BorderTemplate;
import com.herobrot.tieredneo.api.ModifierUtils;
import com.herobrot.tieredneo.config.ConfigInit;
import com.herobrot.tieredneo.util.TieredTooltip;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;

/**
 * Intercepta el tooltip justo antes de que el juego lo dibuje.
 */
@EventBusSubscriber(modid = TieredNeo.MODID, value = Dist.CLIENT)
public class TieredTooltipEvents {

    @SubscribeEvent
    public static void onRenderTooltipPre(RenderTooltipEvent.Pre event) {
        if (!ConfigInit.CONFIG.tieredTooltip) return;

        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        ResourceLocation tierId = ModifierUtils.getAttributeId(stack);
        if (tierId == null) return;

        // Buscamos si hay un borde registrado para este tier
        BorderTemplate matchedTemplate = null;
        for (BorderTemplate template : TieredNeoClient.BORDER_TEMPLATES) {
            if (template.containsDecider(tierId.toString())) {
                matchedTemplate = template;
                break;
            }
        }

        if (matchedTemplate != null) {
            // Cancelamos el dibujado vainilla de Minecraft
            event.setCanceled(true);

            // Llamamos a nuestro motor de renderizado con los datos que NeoForge ya preparó
            TieredTooltip.renderTieredTooltip(
                    event.getGraphics(),
                    event.getFont(),
                    event.getComponents(), // Contiene texto e imágenes (como Bundles)
                    event.getX(),
                    event.getY(),
                    matchedTemplate
            );
        }
    }
}