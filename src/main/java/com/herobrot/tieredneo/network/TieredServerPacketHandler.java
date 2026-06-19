package com.herobrot.tieredneo.network;

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

            if (payload.reforgingScreen()) {
                // Switching to ReforgeScreen — open our custom menu
                player.openMenu(new SimpleMenuProvider(
                        (syncId, inv, p) -> new ReforgeMenu(
                                syncId, inv,
                                ContainerLevelAccess.create(p.level(), finalPos)
                        ),
                        Component.translatable("screen.tieredneo.container.reforge")
                ), buf -> buf.writeBlockPos(finalPos));
            } else {
                // Switching back to vanilla AnvilScreen
                player.openMenu(new SimpleMenuProvider(
                        (syncId, inv, p) -> new AnvilMenu(
                                syncId, inv,
                                ContainerLevelAccess.create(p.level(), finalPos)
                        ),
                        Component.translatable("screen.tieredneo.container.repair")
                ));
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

    private static BlockPos resolveBlockPos(ServerPlayer player) {
        if (player.containerMenu instanceof ReforgeMenu reforgeMenu) {
            return reforgeMenu.getPos();
        }
        if (player.containerMenu instanceof AnvilMenu anvilMenu) {
            return anvilMenu.access.evaluate((level, pos) -> pos).orElse(null);
        }
        return null;
    }
}