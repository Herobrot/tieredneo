package com.herobrot.tieredneo.api;

import com.herobrot.tieredneo.TieredNeo;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record BorderTemplate(int index, String texture, ResourceLocation identifier, int startGradient, int endGradient,
                             int backgroundGradient, List<String> decider) {
    public BorderTemplate(int index, String texture, int startGradient, int endGradient, int backgroundGradient,
                          List<String> decider) {
        this(index, texture, ResourceLocation.fromNamespaceAndPath(TieredNeo.MODID, "textures/gui/" + texture + ".png"
        ), startGradient, endGradient, backgroundGradient, List.copyOf(decider));
    }

    public boolean containsDecider(String tierId) {
        String pathOnly = tierId.contains(":") ? tierId.split(":")[1] : tierId;

        return this.decider.contains(tierId) || this.decider.contains(pathOnly);
    }
}