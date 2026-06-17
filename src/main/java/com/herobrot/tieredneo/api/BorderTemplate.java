package com.herobrot.tieredneo.api;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Representa la configuración visual del borde de un tooltip cargado desde los resource packs.
 * * ANÁLISIS DE CAMBIOS (NeoForge 1.21.1):
 * - Convertido a 'record' para garantizar inmutabilidad.
 * - Identifier cambiado a ResourceLocation.
 * - [CRÍTICO] Se eliminó la lista 'List<ItemStack> stacks' original de Fabric.
 * Almacenar referencias a ItemStacks en plantillas visuales estáticas provoca
 * fugas de memoria en la 1.21.1 debido a la constante instanciación de
 * DataComponents. La asignación del borde se resolverá dinámicamente en el
 * RenderTooltipEvent comprobando el TierDataComponent contra la lista 'decider'.
 */
public record BorderTemplate(
        int index,
        String texture,
        ResourceLocation identifier,
        int startGradient,
        int endGradient,
        int backgroundGradient,
        List<String> decider
) {
    /**
     * Constructor compacto para la generación del ResourceLocation y la
     * protección de la lista de deciders.
     * Este constructor es el que llama nuestro TooltipBorderLoader.
     */
    public BorderTemplate(int index, String texture, int startGradient, int endGradient, int backgroundGradient, List<String> decider) {
        this(
                index,
                texture,
                // Genera la ruta de la textura automáticamente usando el namespace del mod original
                // para que los resource packs de TieredZ sigan funcionando.
                ResourceLocation.fromNamespaceAndPath("tiered", "textures/gui/" + texture + ".png"),
                startGradient,
                endGradient,
                backgroundGradient,
                // Guardamos una copia inmutable de la lista para evitar modificaciones accidentales
                List.copyOf(decider)
        );
    }

    /**
     * Comprueba si el ID de un tier (ej. "tiered:epic_armor_1") pertenece a este borde.
     * Esta función se llamará durante el evento RenderTooltipEvent.Color.
     *
     * @param tierId El identificador del tier obtenido del TierDataComponent en forma de String.
     */
    public boolean containsDecider(String tierId) {
        return this.decider.contains(tierId);
    }
}