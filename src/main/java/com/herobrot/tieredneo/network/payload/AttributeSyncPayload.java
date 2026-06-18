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

/**
 * S2C payload: sends all loaded PotentialAttributes to the client on join/reload.

 * Encoding strategy:
 *   Two parallel lists of equal length:
 *     ids   — List<ResourceLocation> (the attribute registry key)
 *     jsons — List<String> (each PotentialAttribute serialized as a JSON string)

 * Why JSON strings for the values?
 *   PotentialAttribute has complex nested types (Style, ItemVerifier, AttributeTemplate[])
 *   that would require a full recursive StreamCodec chain. Since the client already
 *   has the GSON instance via AttributeDataLoader.GSON, reusing it for deserialization
 *   keeps the code consistent and avoids duplicating codec logic.
 *   The payload is sent once on join and once per reload — bandwidth is not a concern.

 * The client handler (TieredClientPacketHandler.handleAttributeSync) deserializes
 * each JSON string back into a PotentialAttribute and stores it in the loader's map.
 */
public record AttributeSyncPayload(List<ResourceLocation> ids, List<String> jsons)
        implements CustomPacketPayload {

    public static final Type<AttributeSyncPayload> TYPE =
            new Type<>(TieredNeo.rl("attribute_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AttributeSyncPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.collection(ArrayList::new, ResourceLocation.STREAM_CODEC),
                    AttributeSyncPayload::ids,
                    ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8),
                    AttributeSyncPayload::jsons,
                    AttributeSyncPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    /** Builds the payload from the server's loaded attribute map. */
    public static AttributeSyncPayload fromLoader(AttributeDataLoader loader) {
        List<ResourceLocation> ids   = new ArrayList<>();
        List<String>           jsons = new ArrayList<>();

        loader.getItemAttributes().forEach((id, attribute) -> {
            ids.add(id);
            jsons.add(AttributeDataLoader.GSON.toJson(attribute));
        });

        return new AttributeSyncPayload(ids, jsons);
    }
}