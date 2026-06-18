package com.herobrot.tieredneo.network.payload;

import com.herobrot.tieredneo.TieredNeo;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record ReforgeScreenPayload(boolean reforgingScreen) implements CustomPacketPayload {
    public static final Type<ReforgeScreenPayload> TYPE = new Type<>(TieredNeo.rl("reforge_screen"));

    public static final StreamCodec<ByteBuf, ReforgeScreenPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ReforgeScreenPayload::reforgingScreen,
            ReforgeScreenPayload::new
    );

    @Override public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }
}