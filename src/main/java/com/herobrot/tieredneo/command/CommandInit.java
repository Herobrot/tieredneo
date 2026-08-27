package com.herobrot.tieredneo.command;

import com.herobrot.tieredneo.TieredNeo;
import com.herobrot.tieredneo.api.ModifierUtils;
import com.herobrot.tieredneo.api.PotentialAttribute;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CommandInit {
    public static final List<String> TIER_LIST = List.of("common", "uncommon", "rare", "epic", "legendary", "unique");

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        ArgumentBuilder<CommandSourceStack, ?> targetsArgument = Commands.argument("targets", EntityArgument.players());
        for (int i = 0; i < TIER_LIST.size(); i++) {
            int tierIndex = i;
            String tierName = TIER_LIST.get(i);
            targetsArgument.then(Commands.literal(tierName).executes(ctx ->
                    executeCommand(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), tierIndex)
            ));
        }

        dispatcher.register(Commands.literal("tiered")
                .requires(source -> source.hasPermission(3))
                .then(Commands.literal("tier")
                        .then(targetsArgument)
                )
                .then(Commands.literal("untier")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(ctx -> executeCommand(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), -1))
                        )
                )
        );
    }

    private static int executeCommand(CommandSourceStack source, Collection<ServerPlayer> targets, int tierIndex) {
        for (ServerPlayer player : targets) {
            ItemStack stack = player.getMainHandItem();
            if (stack.isEmpty()) {
                source.sendSuccess(() -> Component.translatable("commands.tieredneo.failed", player.getDisplayName()), true);
                continue;
            }
            Component itemDisplayName = stack.getHoverName();
            if (tierIndex == -1) {
                if (ModifierUtils.getAttributeId(stack) != null) {
                    ModifierUtils.removeItemStackAttribute(stack);
                    stack.remove(DataComponents.ITEM_NAME);
                    source.sendSuccess(() -> Component.translatable("commands.tieredneo.untier", itemDisplayName, player.getDisplayName()), true);
                } else
                    source.sendSuccess(() -> Component.translatable("commands.tieredneo.untier_failed", itemDisplayName, player.getDisplayName()), true);
            } else {
                String targetTier = TIER_LIST.get(tierIndex);
                List<ResourceLocation> potentialTiers = new ArrayList<>();
                TieredNeo.ATTRIBUTE_DATA_LOADER.getItemAttributes().forEach((id, attr) -> {
                    if (attr.isValid(stack.getItem())) {
                        String path = id.getPath();
                        if (path.contains(targetTier)) {
                            if (targetTier.equals("common") && path.contains("uncommon")) return;
                            potentialTiers.add(id);
                        }
                    }
                });
                if (potentialTiers.isEmpty()) {
                    source.sendSuccess(() -> Component.translatable("commands.tieredneo.tiering_failed", itemDisplayName, player.getDisplayName()), true);
                    continue;
                }
                ModifierUtils.removeItemStackAttribute(stack);
                ResourceLocation chosenId = potentialTiers.get(player.getRandom().nextInt(potentialTiers.size()));
                PotentialAttribute chosenAttr = TieredNeo.ATTRIBUTE_DATA_LOADER.getItemAttributes().get(chosenId);
                if (chosenAttr != null) {
                    ModifierUtils.applyTierToStack(stack, chosenId, chosenAttr);
                    source.sendSuccess(() -> Component.translatable("commands.tieredneo.tier", itemDisplayName, player.getDisplayName()), true);
                }
            }
        }
        return 1;
    }
}