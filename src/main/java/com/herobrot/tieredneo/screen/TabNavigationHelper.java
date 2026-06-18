package com.herobrot.tieredneo.screen;

import com.herobrot.tieredneo.TieredNeo;
import com.herobrot.tieredneo.mixin.MouseHandlerAccessor;
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
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = TieredNeo.MODID, value = Dist.CLIENT)
public class TabNavigationHelper {

    private static final ResourceLocation ANVIL_ICON = TieredNeo.rl("textures/gui/anvil_tab_icon.png");
    private static final ResourceLocation REFORGE_ICON = TieredNeo.rl("textures/gui/reforge_tab_icon.png");
    private static final ResourceLocation TAB_SELECTED = ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_selected_1");
    private static final ResourceLocation TAB_UNSELECTED = ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_unselected_1");

    private static boolean expectingTabChange = false;
    private static double savedMouseX = 0;
    private static double savedMouseY = 0;

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();

        if (screen instanceof AbstractContainerScreen<?> containerScreen) {
            boolean isAnvil = screen instanceof AnvilScreen;
            boolean isReforge = screen instanceof ReforgeScreen;

            if (isAnvil || isReforge) {
                if (expectingTabChange) {
                    expectingTabChange = false;

                    Minecraft mc = Minecraft.getInstance();
                    GLFW.glfwSetCursorPos(mc.getWindow().getWindow(), savedMouseX, savedMouseY);

                    MouseHandlerAccessor accessor = (MouseHandlerAccessor) mc.mouseHandler;
                    accessor.setXpos(savedMouseX);
                    accessor.setYpos(savedMouseY);
                }

                int leftPos = containerScreen.getGuiLeft();
                int topPos = containerScreen.getGuiTop();

                event.addListener(new TabButton(
                        leftPos + 10, topPos - 28, 28, isAnvil ? 32 : 28,
                        ANVIL_ICON, isAnvil,
                        (button) -> sendTabChangePacket(false)
                ));

                event.addListener(new TabButton(
                        leftPos + 39, topPos - 28, 28, isReforge ? 32 : 28,
                        REFORGE_ICON, isReforge,
                        (button) -> sendTabChangePacket(true)
                ));
            }
        }
    }

    private static void sendTabChangePacket(boolean isReforge) {
        Minecraft mc = Minecraft.getInstance();
        savedMouseX = mc.mouseHandler.xpos();
        savedMouseY = mc.mouseHandler.ypos();
        expectingTabChange = true;
        PacketDistributor.sendToServer(new ReforgeScreenPayload(isReforge));
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
            ResourceLocation bgSprite = this.isSelected ? TAB_SELECTED : TAB_UNSELECTED;
            graphics.blitSprite(bgSprite, this.getX(), this.getY(), this.width, this.height);
            graphics.blit(this.icon, this.getX() + 6, this.getY() + 6, 0, 0, 16, 16, 16, 16);
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