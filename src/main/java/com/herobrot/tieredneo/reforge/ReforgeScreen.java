package com.herobrot.tieredneo.reforge;

import com.herobrot.tieredneo.TieredNeo;
import com.herobrot.tieredneo.api.ModifierUtils;
import com.herobrot.tieredneo.api.TieredItemTags;
import com.herobrot.tieredneo.config.ConfigInit;
import com.herobrot.tieredneo.network.payload.ReforgeRequestPayload;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class ReforgeScreen extends AbstractContainerScreen<ReforgeMenu> {

    public static final ResourceLocation TEXTURE = TieredNeo.rl("textures/gui/reforging_screen.png");
    public ReforgeButton reforgeButton;
    private ItemStack last;
    private List<Item> baseItems;

    public ReforgeScreen(ReforgeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.titleLabelX = 60;
    }

    @Override
    protected void init() {
        super.init();
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;

        this.reforgeButton = this.addRenderableWidget(new ReforgeButton(i + 79, j + 56, (button) -> {
            PacketDistributor.sendToServer(new ReforgeRequestPayload());
        }));
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        if (this.isHovering(79, 56, 18, 18, mouseX, mouseY)) {
            ItemStack itemStack = this.menu.getSlot(1).getItem();

            if (itemStack.isEmpty() || itemStack.is(TieredItemTags.MODIFIER_RESTRICTED)) {
                baseItems = Collections.emptyList();
            } else {
                if (itemStack != last) {
                    last = itemStack;
                    baseItems = new ArrayList<>();
                    List<Item> items = TieredNeo.REFORGE_DATA_LOADER.getReforgeBaseItems(itemStack.getItem());

                    if (!items.isEmpty()) {
                        baseItems.addAll(items);
                    } else if (itemStack.getItem() instanceof TieredItem tieredItem) {
                        for (ItemStack stack : tieredItem.getTier().getRepairIngredient().getItems()) {
                            baseItems.add(stack.getItem());
                        }
                    } else if (itemStack.getItem() instanceof ArmorItem armorItem) {
                        for (ItemStack stack : armorItem.getMaterial().value().repairIngredient().get().getItems()) {
                            baseItems.add(stack.getItem());
                        }
                    } else {
                        BuiltInRegistries.ITEM.getTag(TieredItemTags.REFORGE_BASE_ITEM).ifPresent(tag ->
                                tag.forEach(holder -> baseItems.add(holder.value()))
                        );
                    }
                }
            }

            List<Component> tooltip = new ArrayList<>();
            if (!baseItems.isEmpty()) {
                ItemStack ingredient = this.menu.getSlot(0).getItem();
                if (!(!ingredient.isEmpty() && baseItems.contains(ingredient.getItem()))) {
                    tooltip.add(Component.translatable("screen.tieredneo.reforge_ingredient"));
                    for (Item item : baseItems) {
                        tooltip.add(item.getName(item.getDefaultInstance()));
                    }
                }
            }

            if (itemStack.isDamageableItem() && itemStack.isDamaged()) {
                tooltip.add(Component.translatable("screen.tieredneo.reforge_damaged"));
            }

            if (!tooltip.isEmpty()) {
                graphics.renderTooltip(this.font, tooltip, java.util.Optional.empty(), mouseX, mouseY);
            }
        }

        if (!ConfigInit.CONFIG.uniqueReforge && !this.menu.getSlot(1).getItem().isEmpty()) {
            ResourceLocation attrId = ModifierUtils.getAttributeId(this.menu.getSlot(1).getItem());
            if (attrId != null && attrId.getPath().contains("unique")) {
                graphics.blit(TEXTURE, this.leftPos + 74, this.topPos + 29, 0, 166, 28, 26);
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        graphics.blit(TEXTURE, i, j, 0, 0, this.imageWidth, this.imageHeight);
    }

    public static class ReforgeButton extends AbstractButton {
        private final Runnable onPress;

        public ReforgeButton(int x, int y, Consumer<AbstractButton> onPress) {
            super(x, y, 18, 18, Component.empty());
            this.active = false; // Inicia apagado.
            this.onPress = () -> onPress.accept(this);
        }

        @Override
        public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int j = 176;
            if (!this.active) {
                j += this.width * 2;
            } else if (this.isHovered()) { // Solo iluminar si el ratón está encima
                j += this.width;
            }
            graphics.blit(TEXTURE, this.getX(), this.getY(), j, 0, this.width, this.height);
        }

        @Override
        public void onPress() {
            this.onPress.run();
        }

        @Override
        protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {}
    }
}