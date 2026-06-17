package com.herobrot.tieredneo.network.payload;

import com.herobrot.tieredneo.TieredNeo;
import com.herobrot.tieredneo.data.ReforgeDataLoader;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record ReforgeItemSyncPayload(Map<Item, List<Item>> reforgeMap) implements CustomPacketPayload {
    public static final Type<ReforgeItemSyncPayload> TYPE = new Type<>(TieredNeo.rl("reforge_item_sync"));

    // NeoForge magia: Un códec que maneja todo el mapa y las listas de items registrados automáticamente.
    public static final StreamCodec<RegistryFriendlyByteBuf, ReforgeItemSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new,
                    ByteBufCodecs.registry(Registries.ITEM),
                    ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.registry(Registries.ITEM))
            ),
            ReforgeItemSyncPayload::reforgeMap,
            ReforgeItemSyncPayload::new
    );

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static ReforgeItemSyncPayload fromLoader(ReforgeDataLoader loader) {
        return new ReforgeItemSyncPayload(loader.getRawMap()); // Necesitarás añadir un getter público para 'reforgeBaseMap' en ReforgeDataLoader
    }
}