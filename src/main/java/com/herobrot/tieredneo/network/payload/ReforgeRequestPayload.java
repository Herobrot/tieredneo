package com.herobrot.tieredneo.network.payload;

import com.herobrot.tieredneo.TieredNeo;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record ReforgeRequestPayload() implements CustomPacketPayload {
    public static final Type<ReforgeRequestPayload> TYPE = new Type<>(TieredNeo.rl("reforge_request"));
    public static final StreamCodec<ByteBuf, ReforgeRequestPayload> STREAM_CODEC =
            StreamCodec.unit(new ReforgeRequestPayload());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {return TYPE;}
}