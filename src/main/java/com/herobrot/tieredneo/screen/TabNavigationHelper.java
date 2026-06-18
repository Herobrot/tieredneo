package com.herobrot.tieredneo.screen;

import com.herobrot.tieredneo.TieredNeo;
import com.herobrot.tieredneo.network.payload.ReforgeScreenPayload;
import com.herobrot.tieredneo.reforge.ReforgeScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = TieredNeo.MODID, value = Dist.CLIENT)
public class TabNavigationHelper {

    private static final ResourceLocation ANVIL_ICON = TieredNeo.rl("textures/gui/anvil_tab_icon.png");
    private static final ResourceLocation REFORGE_ICON = TieredNeo.rl("textures/gui/reforge_tab_icon.png");

    // Sprites nativos de Minecraft 1.21.1 para pestañas superiores
    private static final ResourceLocation TAB_SELECTED = ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_selected_1");
    private static final ResourceLocation TAB_UNSELECTED = ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_unselected_1");

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();

        if (screen instanceof AbstractContainerScreen<?> containerScreen) {
            boolean isAnvil = screen instanceof AnvilScreen;
            boolean isReforge = screen instanceof ReforgeScreen;

            if (isAnvil || isReforge) {
                int leftPos = containerScreen.getGuiLeft();
                int topPos = containerScreen.getGuiTop();

                // Las pestañas superiores estándar miden 28x32.
                // Se posicionan en Y = topPos - 28 para que la base se solape con el GUI.

                // Pestaña Yunque (Reparar)
                event.addListener(new TabButton(
                        leftPos + 10, topPos - 28, 28, 32,
                        ANVIL_ICON, isAnvil,
                        (button) -> sendTabChangePacket(false)
                ));

                // Pestaña Reforja
                event.addListener(new TabButton(
                        leftPos + 39, topPos - 28, 28, 32,
                        REFORGE_ICON, isReforge,
                        (button) -> sendTabChangePacket(true)
                ));
            }
        }
    }

    private static void sendTabChangePacket(boolean isReforge) {
        // Capturamos las coordenadas exactas del mouse del sistema operativo
        double mX = Minecraft.getInstance().mouseHandler.xpos();
        double mY = Minecraft.getInstance().mouseHandler.ypos();
        PacketDistributor.sendToServer(new ReforgeScreenPayload(mX, mY, isReforge));
    }

    public static class TabButton extends AbstractButton {
        private final ResourceLocation icon;
        private final boolean isSelected;
        private final Runnable onPress;

        public TabButton(int x, int y, int width, int height, ResourceLocation icon, boolean isSelected, java.util.function.Consumer<AbstractButton> onPress) {
            super(x, y, width, height, Component.empty());
            this.icon = icon;
            this.isSelected = isSelected;
            this.onPress = () -> onPress.accept(this);
        }

        @Override
        public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            // 1. DIBUJAR FONDO GRIS: Usamos el sprite nativo de Minecraft
            ResourceLocation bgSprite = this.isSelected ? TAB_SELECTED : TAB_UNSELECTED;

            // Bajamos la pestaña no seleccionada 2 píxeles para dar efecto 3D
            int yOffset = this.isSelected ? 0 : 2;
            graphics.blitSprite(bgSprite, this.getX(), this.getY() + yOffset, this.width, this.height);

            // 2. DIBUJAR TU ICONO PNG: Centrado en la pestaña
            graphics.blit(this.icon, this.getX() + 6, this.getY() + 8 + yOffset, 0, 0, 16, 16, 16, 16);
        }

        @Override
        public void onPress() {
            if (!this.isSelected) {
                this.onPress.run();
            }
        }

        @Override
        protected void updateWidgetNarration(@NotNull NarrationElementOutput narration) {}
    }
}