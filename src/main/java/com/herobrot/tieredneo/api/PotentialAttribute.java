package com.herobrot.tieredneo.api;

import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PotentialAttribute {
    private final String id;
    private final List<ItemVerifier> verifiers;
    private final int weight;
    private final Style style;
    private final List<AttributeTemplate> attributes;

    public PotentialAttribute(String id, List<ItemVerifier> verifiers, int weight, Style style,
                              List<AttributeTemplate> attributes) {
        this.id = id;
        this.verifiers = verifiers != null ? List.copyOf(verifiers) : List.of();
        this.weight = weight;
        this.style = style;
        this.attributes = attributes != null ? List.copyOf(attributes) : List.of();
    }

    public String getID() {return id;}

    public List<ItemVerifier> getVerifiers() {return verifiers;}

    public int getWeight() {return weight;}

    public Style getStyle() {return style;}

    public List<AttributeTemplate> getAttributes() {return attributes;}

    public boolean isValid(@NotNull Item item) {
        if (verifiers.isEmpty()) return false;
        return verifiers.stream().anyMatch(verifier -> verifier.isValid(item));
    }
}