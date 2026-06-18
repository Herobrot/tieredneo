package com.herobrot.tieredneo.network;

import com.herobrot.tieredneo.network.payload.MousePositionPayload;
import com.herobrot.tieredneo.network.payload.ReforgeRequestPayload;
import com.herobrot.tieredneo.network.payload.ReforgeScreenPayload;
import com.herobrot.tieredneo.reforge.ReforgeMenu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class TieredServerPacketHandler {

    public static void handleReforgeScreen(ReforgeScreenPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            BlockPos pos = resolveBlockPos(player);
            if (pos == null) return;

            final BlockPos finalPos = pos;

            MousePositionPayload mousePositionPayload = new MousePositionPayload(payload.mouseX(), payload.mouseY());
            if (payload.reforgingScreen()) {
                // Switching to ReforgeScreen — open our custom menu
                player.openMenu(new SimpleMenuProvider(
                        (syncId, inv, p) -> new ReforgeMenu(
                                syncId, inv,
                                ContainerLevelAccess.create(p.level(), finalPos)
                        ),
                        Component.translatable("container.reforge")
                ), buf -> buf.writeBlockPos(finalPos));
                TieredNetwork.sendToPlayer(player, mousePositionPayload);
            } else {
                // Switching back to vanilla AnvilScreen
                player.openMenu(new SimpleMenuProvider(
                        (syncId, inv, p) -> new AnvilMenu(
                                syncId, inv,
                                ContainerLevelAccess.create(p.level(), finalPos)
                        ),
                        Component.translatable("container.repair")
                ));
                TieredNetwork.sendToPlayer(player, mousePositionPayload);
            }
        });
    }

    public static void handleReforgeRequest(ReforgeRequestPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof ReforgeMenu reforgeMenu) {
                reforgeMenu.reforge();
            }
        });
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Resolves the BlockPos of the block the player has open.

     * ContainerLevelAccess is package-private in vanilla AnvilMenu (stored as
     * 'access' field). We cannot access it directly without an Access Transformer.

     * Instead, we ask ReforgeMenu for its pos (which we store explicitly), and
     * for AnvilMenu we use the ContainerLevelAccess.evaluate() indirection by
     * calling the public API: the menu exposes getBlockPos() via our own ReforgeMenu,
     * and for vanilla AnvilMenu we evaluate access ourselves using the AT in
     * META-INF/accesstransformer.cfg — OR we store the pos separately.

     * Decision: Add an Access Transformer entry for AnvilMenu.access so we can
     * call it correctly without reflection. Until that AT is added, we fall back
     * to ReforgeMenu's stored pos only.

     * See: META-INF/accesstransformer.cfg → "public net.minecraft.world.inventory.AnvilMenu access"
     */
    private static BlockPos resolveBlockPos(ServerPlayer player) {
        if (player.containerMenu instanceof ReforgeMenu reforgeMenu) {
            // ReforgeMenu stores the pos explicitly — Step 6 implements getBlockPos()
            return reforgeMenu.getPos();
        }
        if (player.containerMenu instanceof AnvilMenu anvilMenu) {
            // Requires AT: public-ifying AnvilMenu.access
            // Will be resolved when the Access Transformer is added in Step 6
            return anvilMenu.access.evaluate((level, pos) -> pos).orElse(null);
        }
        return null;
    }
}