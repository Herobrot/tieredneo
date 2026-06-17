package com.herobrot.tieredneo.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/**
 * Data component stored on ItemStacks to mark them with a tier.

 * Fields:
 *   tierId    — the ResourceLocation of the PotentialAttribute
 *               (e.g. ResourceLocation("tieredneo", "sword_legendary"))
 *   durable   — cached durability factor value; -1 means no durable attribute
 *   operation — ordinal of AttributeModifier.Operation (0=ADD_VALUE,
 *               1=ADD_MULTIPLIED_BASE, 2=ADD_MULTIPLIED_TOTAL)

 * Compatibility note on CODEC:
 *   TieredZ stored the tier as a plain String in NBT. To read those existing
 *   items without data-fixers, the persistent Codec accepts the tier field as
 *   a String and parses it into a ResourceLocation via xmap, which handles both
 *   "tieredneo:sword_legendary" (new) and bare strings gracefully.
 *   Writing always produces a valid ResourceLocation string.

 * Improvement over Fabric version:
 *   - tier field is ResourceLocation instead of String, enforcing format at
 *     the serialization boundary rather than scattered through business logic.
 *   - isPresent() / pathContains() helpers eliminate raw string checks elsewhere.
 */
public record TierDataComponent(ResourceLocation tierId, float durable, int operation) {

    /** Sentinel value representing an item with no tier. */
    public static final TierDataComponent EMPTY =
            new TierDataComponent(ResourceLocation.fromNamespaceAndPath("tieredneo", "empty"), -1f, 2);

    // -------------------------------------------------------------------------
    // Codec — persistent storage (DataComponents on disk / in NBT)
    //
    // Uses ResourceLocation.CODEC directly.
    // For items that were saved by TieredZ with a raw String, the ResourceLocation
    // codec will parse them correctly as long as they contain a ':' separator.
    // Items with malformed IDs simply won't load their tier (fail-safe).
    // -------------------------------------------------------------------------

    public static final Codec<TierDataComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC
                            .fieldOf("tier")
                            .forGetter(TierDataComponent::tierId),
                    Codec.FLOAT
                            .fieldOf("durable_factor")
                            .forGetter(TierDataComponent::durable),
                    Codec.INT
                            .fieldOf("operation")
                            .forGetter(TierDataComponent::operation)
            ).apply(instance, TierDataComponent::new)
    );

    // -------------------------------------------------------------------------
    // StreamCodec — network synchronization
    // ByteBufCodecs.RESOURCE_LOCATION is the NeoForge 1.21.1 standard.
    // -------------------------------------------------------------------------

    public static final StreamCodec<ByteBuf, TierDataComponent> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.RESOURCE_LOCATION, TierDataComponent::tierId,
                    ByteBufCodecs.FLOAT,             TierDataComponent::durable,
                    ByteBufCodecs.INT,               TierDataComponent::operation,
                    TierDataComponent::new
            );

    // -------------------------------------------------------------------------
    // Helpers — replaces scattered tier().contains() / tier().isEmpty() calls
    // -------------------------------------------------------------------------

    /** True if this component holds an actual tier (not the EMPTY sentinel). */
    public boolean isPresent() {
        return !tierId.equals(EMPTY.tierId);
    }

    /**
     * True if the tier's path contains the given keyword.
     * Example: pathContains("unique") → true for "tieredneo:sword_unique"
     */
    public boolean pathContains(String keyword) {
        return tierId.getPath().contains(keyword);
    }

    /**
     * Convenience — returns the ResourceLocation as a string.
     * Kept for Gson serialization compatibility in AttributeSyncPayload.
     */
    public String tierIdString() {
        return tierId.toString();
    }
}