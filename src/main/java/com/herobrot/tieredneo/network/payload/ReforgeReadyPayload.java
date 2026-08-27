package com.herobrot.tieredneo.network.payload;

import com.herobrot.tieredneo.TieredNeo;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record ReforgeReadyPayload(boolean disableButton) implements CustomPacketPayload {
    public static final Type<ReforgeReadyPayload> TYPE = new Type<>(TieredNeo.rl("reforge_ready"));
    public static final StreamCodec<ByteBuf, ReforgeReadyPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.BOOL, ReforgeReadyPayload::disableButton, ReforgeReadyPayload::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {return TYPE;}
}