package com.herobrot.tieredneo.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record TierDataComponent(ResourceLocation tierId, float durable, int operation) {
    public static final TierDataComponent EMPTY = new TierDataComponent(ResourceLocation.fromNamespaceAndPath(
            "tieredneo", "empty"), -1f, 2);
    public static final Codec<TierDataComponent> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(ResourceLocation.CODEC.fieldOf("tier").forGetter(TierDataComponent::tierId), Codec.FLOAT.fieldOf("durable_factor").forGetter(TierDataComponent::durable), Codec.INT.fieldOf("operation").forGetter(TierDataComponent::operation)).apply(instance, TierDataComponent::new));
    public static final StreamCodec<ByteBuf, TierDataComponent> STREAM_CODEC =
            StreamCodec.composite(ResourceLocation.STREAM_CODEC, TierDataComponent::tierId, ByteBufCodecs.FLOAT,
                    TierDataComponent::durable, ByteBufCodecs.INT, TierDataComponent::operation,
                    TierDataComponent::new);

    public boolean isPresent() {
        return tierId.equals(EMPTY.tierId);
    }

    public boolean pathContains(String keyword) {
        return tierId.getPath().contains(keyword);
    }

    public String tierIdString() {
        return tierId.toString();
    }
}