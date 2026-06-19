package com.herobrot.tieredneo.reforge;

import com.herobrot.tieredneo.TieredNeo;
import com.herobrot.tieredneo.api.ModifierUtils;
import com.herobrot.tieredneo.api.TieredItemTags;
import com.herobrot.tieredneo.config.ConfigInit;
import com.herobrot.tieredneo.network.TieredNetwork;
import com.herobrot.tieredneo.network.payload.ReforgeReadyPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.block.LevelEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class ReforgeMenu extends AbstractContainerMenu {
    private final SimpleContainer container = new SimpleContainer(3) {
        @Override
        public void setChanged() {
            super.setChanged();
            ReforgeMenu.this.slotsChanged(this);
        }
    };
    public final ContainerLevelAccess access;
    private final Player player;
    private BlockPos pos;

    public ReforgeMenu(int windowId, Inventory playerInventory, FriendlyByteBuf data) {
        this(windowId, playerInventory, ContainerLevelAccess.NULL);
        if (data != null) {
            this.pos = data.readBlockPos();
        }
    }

    public ReforgeMenu(int windowId, Inventory playerInventory, ContainerLevelAccess access) {
        super(TieredNeo.REFORGE_MENU.get(), windowId);
        this.access = access;
        this.player = playerInventory.player;

        this.addSlot(new Slot(this.container, 0, 45, 47));

        this.addSlot(new Slot(this.container, 1, 80, 34));

        this.addSlot(new Slot(this.container, 2, 115, 47) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return stack.is(TieredItemTags.REFORGE_ADDITION);
            }
        });

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }

        this.access.execute((level, blockPos) -> this.setPos(blockPos));
    }

    @Override
    @SuppressWarnings("resource")
    public void slotsChanged(@NotNull Container container) {
        super.slotsChanged(container);
        if (!this.player.level().isClientSide && container == this.container) {
            this.updateResult();
        }
    }

    private void updateResult() {
        ItemStack stack = this.getSlot(1).getItem();
        boolean reforgeReady;
        if (this.getSlot(0).hasItem() && this.getSlot(1).hasItem() && this.getSlot(2).hasItem()) {
            Item item = stack.getItem();

            if (!stack.is(TieredItemTags.MODIFIER_RESTRICTED) && ModifierUtils.getRandomAttributeIDFor(null, item,
                    true) != null && !stack.isDamaged()) {

                List<Item> items = TieredNeo.REFORGE_DATA_LOADER.getReforgeBaseItems(item);
                ItemStack baseItem = this.getSlot(0).getItem();

                if (!items.isEmpty()) {
                    reforgeReady = items.stream().anyMatch(it -> it == baseItem.getItem());
                } else if (item instanceof TieredItem tieredItem) {
                    reforgeReady = tieredItem.getTier().getRepairIngredient().test(baseItem);
                } else if (item instanceof ArmorItem armorItem) {
                    reforgeReady = armorItem.getMaterial().value().repairIngredient().get().test(baseItem);
                } else {
                    reforgeReady = baseItem.is(TieredItemTags.REFORGE_BASE_ITEM);
                }
            } else {
                reforgeReady = false;
            }
        } else {
            reforgeReady = false;
        }

        if (reforgeReady && !ConfigInit.CONFIG.uniqueReforge) {
            if (ModifierUtils.getAttributeId(stack) != null && Objects.requireNonNull(ModifierUtils.getAttributeId(stack)).getPath().contains("unique")) {
                reforgeReady = false;
            }
        }

        if (this.player instanceof ServerPlayer serverPlayer) {
            TieredNetwork.sendToPlayer(serverPlayer, new ReforgeReadyPayload(!reforgeReady));
        }
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        this.access.execute((level, pos) -> this.clearContainer(player, this.container));
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(this.access, player, net.minecraft.world.level.block.Blocks.ANVIL);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemStack = slotStack.copy();

            if (index == 1) {
                if (!this.moveItemStackTo(slotStack, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(slotStack, itemStack);
            } else if (index == 0 || index == 2) {
                if (!this.moveItemStackTo(slotStack, 3, 39, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 3 && index < 39) {
                if (itemStack.is(TieredItemTags.REFORGE_ADDITION) && !this.moveItemStackTo(slotStack, 2, 3, false)) {
                    return ItemStack.EMPTY;
                }
                if (this.getSlot(1).hasItem()) {
                    Item item = this.getSlot(1).getItem().getItem();
                    if (item instanceof TieredItem tieredItem && tieredItem.getTier().getRepairIngredient().test(itemStack) && !this.moveItemStackTo(slotStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                    if (item instanceof ArmorItem armorItem && armorItem.getMaterial().value().repairIngredient().get().test(itemStack) && !this.moveItemStackTo(slotStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                    if (itemStack.is(TieredItemTags.REFORGE_BASE_ITEM) && !this.moveItemStackTo(slotStack, 0, 1,
                            false)) {
                        return ItemStack.EMPTY;
                    }
                    List<Item> items = TieredNeo.REFORGE_DATA_LOADER.getReforgeBaseItems(item);
                    if (items.stream().anyMatch(it -> it == slotStack.copy().getItem()) && !this.moveItemStackTo(slotStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                if (ModifierUtils.getRandomAttributeIDFor(null, itemStack.getItem(), false) != null && !this.moveItemStackTo(slotStack, 1, 2, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, slotStack);
        }
        return itemStack;
    }

    public void reforge() {
        ItemStack itemStack = this.getSlot(1).getItem();
        ModifierUtils.removeItemStackAttribute(itemStack);
        ModifierUtils.setItemStackAttribute(player, itemStack, true);

        this.decrementStack(0);
        this.decrementStack(2);
        this.access.execute((level, pos) -> level.levelEvent(LevelEvent.SOUND_ANVIL_USED, pos, 0));
    }

    public void setPos(BlockPos pos) {
        this.pos = pos;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    private void decrementStack(int slot) {
        ItemStack itemStack = this.container.getItem(slot);
        itemStack.shrink(1);
        this.container.setItem(slot, itemStack);
    }
}