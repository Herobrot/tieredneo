package com.herobrot.tieredneo.network.payload;

import com.herobrot.tieredneo.TieredNeo;
import com.herobrot.tieredneo.api.PotentialAttribute;
import com.herobrot.tieredneo.data.AttributeDataLoader;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record AttributeSyncPayload(List<ResourceLocation> ids, List<String> jsons) implements CustomPacketPayload {
    public static final Type<AttributeSyncPayload> TYPE = new Type<>(TieredNeo.rl("attribute_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AttributeSyncPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.collection(ArrayList::new, ResourceLocation.STREAM_CODEC),
                    AttributeSyncPayload::ids, ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8),
                    AttributeSyncPayload::jsons, AttributeSyncPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {return TYPE;}

    public static AttributeSyncPayload fromLoader(AttributeDataLoader loader) {
        List<ResourceLocation> ids = new ArrayList<>();
        List<String> jsons = new ArrayList<>();

        loader.getItemAttributes().forEach((id, attribute) -> {
            ids.add(id);
            jsons.add(AttributeDataLoader.GSON.toJson(attribute));
        });

        return new AttributeSyncPayload(ids, jsons);
    }
}