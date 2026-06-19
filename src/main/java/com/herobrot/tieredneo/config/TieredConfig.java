package com.herobrot.tieredneo.config;

import com.herobrot.tieredneo.TieredNeo;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = TieredNeo.MODID)
@Config.Gui.Background("minecraft:textures/block/stone.png")
public class TieredConfig implements ConfigData {
    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.Tooltip
    public boolean lootContainerModifier = true;
    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.Tooltip
    public boolean entityItemModifier = true;
    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.Tooltip
    public boolean craftingModifier = true;
    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.Tooltip
    public boolean merchantModifier = true;
    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.Tooltip
    public float reforgeModifier = 0.9F;
    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.Tooltip
    public float luckReforgeModifier = 0.02F;
    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.Tooltip
    public boolean uniqueReforge = false;
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
    public boolean tieredTooltipAttributes = true;
    @ConfigEntry.Category("client_settings")
    @ConfigEntry.Gui.Tooltip
    public boolean legendaryColorsForAttributes = true;
}