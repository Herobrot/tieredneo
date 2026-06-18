package com.herobrot.tieredneo.config;

import com.herobrot.tieredneo.TieredNeo;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = TieredNeo.MODID)
@Config.Gui.Background("minecraft:textures/block/stone.png")
public class TieredConfig implements ConfigData {

    @ConfigEntry.Category("general")
    @Comment("Items in for example mineshaft chests get modifiers")
    public boolean lootContainerModifier = true;

    @ConfigEntry.Category("general")
    @Comment("Equipped items on entities get modifiers")
    public boolean entityItemModifier = true;

    @ConfigEntry.Category("general")
    @Comment("Crafted items get modifiers")
    public boolean craftingModifier = true;

    @ConfigEntry.Category("general")
    @Comment("Merchant items get modifiers")
    public boolean merchantModifier = true;

    @ConfigEntry.Category("general")
    @Comment("Decreases the biggest weights by this modifier")
    public float reforgeModifier = 0.9F;

    @ConfigEntry.Category("general")
    @Comment("Modify the biggest weights by this modifier per luck")
    public float luckReforgeModifier = 0.02F;

    @ConfigEntry.Category("general")
    public boolean uniqueReforge = false;

    // -------------------------------------------------------------------------

    @ConfigEntry.Category("client_settings")
    public boolean showReforgingTab = true;

    @ConfigEntry.Category("client_settings")
    public int xIconPosition = 0;

    @ConfigEntry.Category("client_settings")
    public int yIconPosition = 0;

    @ConfigEntry.Category("client_settings")
    public boolean tieredTooltip = true;

    @ConfigEntry.Category("client_settings")
    public boolean centerName = true;

    @ConfigEntry.Category("client_settings")
    @ConfigEntry.Gui.Tooltip
    // Activa o desactiva el coloreado personalizado de los atributos de los Tiers
    public boolean tieredTooltipAttributes = true;

    @ConfigEntry.Category("client_settings")
    @ConfigEntry.Gui.Tooltip
    // Si está activo, los Tiers "Legendarios" o "Únicos" usarán su color especial
    // en lugar del Azul tradicional para las estadísticas positivas (buffs).
    public boolean legendaryColorsForAttributes = true;
}