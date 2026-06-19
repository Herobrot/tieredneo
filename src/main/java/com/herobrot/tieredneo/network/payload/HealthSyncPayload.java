package com.herobrot.tieredneo.network.payload;

import com.herobrot.tieredneo.TieredNeo;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record HealthSyncPayload(float health) implements CustomPacketPayload {
    public static final Type<HealthSyncPayload> TYPE = new Type<>(TieredNeo.rl("health_sync"));
    public static final StreamCodec<ByteBuf, HealthSyncPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.FLOAT, HealthSyncPayload::health, HealthSyncPayload::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {return TYPE;}
}