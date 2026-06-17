package com.herobrot.tieredneo.network.payload;

import com.herobrot.tieredneo.TieredNeo;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record MousePositionPayload(int mouseX, int mouseY) implements CustomPacketPayload {
    public static final Type<MousePositionPayload> TYPE = new Type<>(TieredNeo.rl("mouse_position"));

    public static final StreamCodec<ByteBuf, MousePositionPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, MousePositionPayload::mouseX,
            ByteBufCodecs.INT, MousePositionPayload::mouseY,
            MousePositionPayload::new
    );

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}