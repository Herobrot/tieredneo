package com.herobrot.tieredneo.api;

import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;

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
        this.verifiers = verifiers;
        this.weight = weight;
        this.style = style;
        this.attributes = attributes;
    }

    public String getID() {return id;}

    public List<ItemVerifier> getVerifiers() {return verifiers;}

    public int getWeight() {return weight;}

    public Style getStyle() {return style;}

    public List<AttributeTemplate> getAttributes() {return attributes;}

    public boolean isValid(Item item) {
        if (verifiers == null || verifiers.isEmpty()) return false;
        for (ItemVerifier verifier : verifiers) {
            if (verifier.isValid(item)) return true;
        }
        return false;
    }
}