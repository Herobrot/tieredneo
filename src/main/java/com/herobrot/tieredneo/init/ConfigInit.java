package com.herobrot.tieredneo.init;

import com.herobrot.tieredneo.config.TieredConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;

public class ConfigInit {
    public static TieredConfig CONFIG = new TieredConfig();

    public static void init() {
        AutoConfig.register(TieredConfig.class, JanksonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(TieredConfig.class).getConfig();
    }
}