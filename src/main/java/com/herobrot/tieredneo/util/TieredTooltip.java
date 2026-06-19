package com.herobrot.tieredneo.util;

import com.herobrot.tieredneo.api.BorderTemplate;
import com.herobrot.tieredneo.config.ConfigInit;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class TieredTooltip {
    public static void renderTieredTooltip(GuiGraphics graphics, Font font, List<ClientTooltipComponent> components,
                                           int x, int y, BorderTemplate borderTemplate) {
        if (components.isEmpty()) {
            return;
        }

        int width = 0;
        int height = components.size() == 1 ? -2 : 0;

        for (ClientTooltipComponent component : components) {
            int k = component.getWidth(font);
            if (k > width) {
                width = k;
            }
            height += component.getHeight();
        }

        if (width < 64) width = 64;
        if (height < 16) height = 16;

        graphics.pose().pushPose();

        int backgroundColor = borderTemplate.backgroundGradient();
        int colorStart = borderTemplate.startGradient();
        int colorEnd = borderTemplate.endGradient();

        renderTooltipBackground(graphics, x, y, width, height, backgroundColor, colorStart, colorEnd);

        graphics.pose().translate(0.0f, 0.0f, 400.0f);

        int currentY = y;
        for (int r = 0; r < components.size(); ++r) {
            int nameCentering = 0;
            ClientTooltipComponent component = components.get(r);

            if (r == 0 && ConfigInit.CONFIG.centerName) {
                nameCentering = width / 2 - component.getWidth(font) / 2;
            }

            component.renderText(font, x + nameCentering, currentY, graphics.pose().last().pose(),
                    graphics.bufferSource());
            currentY += component.getHeight() + (r == 0 ? 2 : 0);
        }

        currentY = y;
        for (int r = 0; r < components.size(); ++r) {
            ClientTooltipComponent component = components.get(r);
            component.renderImage(font, x, currentY, graphics);
            currentY += component.getHeight() + (r == 0 ? 2 : 0);
        }

        graphics.pose().popPose();

        int border = borderTemplate.index();
        int secondHalf = border > 7 ? 1 : 0;
        if (border > 7) {
            border -= 8;
        }

        graphics.pose().pushPose();
        graphics.pose().translate(0.0f, 0.0f, 400.0f);

        ResourceLocation texture = borderTemplate.identifier();

        graphics.blit(texture, x - 6, y - 6, secondHalf * 64, border * 16, 8, 8, 128, 128);

        graphics.blit(texture, x + width - 2, y - 6, 56 + secondHalf * 64, border * 16, 8, 8, 128, 128);

        graphics.blit(texture, x - 6, y + height - 2, secondHalf * 64, 8 + border * 16, 8, 8, 128, 128);

        graphics.blit(texture, x + width - 2, y + height - 2, 56 + secondHalf * 64, 8 + border * 16, 8, 8, 128, 128);

        graphics.blit(texture, (x - 6 + x + width + 6) / 2 - 24, y - 9, 8 + secondHalf * 64, border * 16, 48, 8, 128,
                128);

        graphics.blit(texture, (x - 6 + x + width + 6) / 2 - 24, y + height + 1, 8 + secondHalf * 64, 8 + border * 16
                , 48, 8, 128, 128);

        graphics.pose().popPose();
    }

    private static void renderTooltipBackground(GuiGraphics graphics, int x, int y, int width, int height,
                                                int backgroundColor, int colorStart, int colorEnd) {
        int i = x - 3;
        int j = y - 3;
        int k = width + 6;
        int l = height + 6;
        renderHorizontalLine(graphics, i, j - 1, k, backgroundColor);
        renderHorizontalLine(graphics, i, j + l, k, backgroundColor);
        renderRectangle(graphics, i, j, k, l, backgroundColor);
        renderVerticalLine(graphics, i - 1, j, l, backgroundColor);
        renderVerticalLine(graphics, i + k, j, l, backgroundColor);
        renderBorder(graphics, i, j + 1, k, l, colorStart, colorEnd);
    }

    private static void renderBorder(GuiGraphics graphics, int x, int y, int width, int height, int startColor,
                                     int endColor) {
        renderVerticalLineGradient(graphics, x, y, height - 2, startColor, endColor);
        renderVerticalLineGradient(graphics, x + width - 1, y, height - 2, startColor, endColor);
        renderHorizontalLine(graphics, x, y - 1, width, startColor);
        renderHorizontalLine(graphics, x, y - 1 + height - 1, width, endColor);
    }

    private static void renderVerticalLine(GuiGraphics graphics, int x, int y, int height, int color) {
        graphics.fill(x, y, x + 1, y + height, 400, color);
    }

    private static void renderVerticalLineGradient(GuiGraphics graphics, int x, int y, int height, int startColor,
                                                   int endColor) {
        graphics.fillGradient(x, y, x + 1, y + height, 400, startColor, endColor);
    }

    private static void renderHorizontalLine(GuiGraphics graphics, int x, int y, int width, int color) {
        graphics.fill(x, y, x + width, y + 1, 400, color);
    }

    private static void renderRectangle(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + height, 400, color);
    }
}