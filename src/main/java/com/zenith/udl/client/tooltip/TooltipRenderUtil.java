package com.zenith.udl.client.tooltip;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.Random;

public final class TooltipRenderUtil {

    private TooltipRenderUtil() {}

    // ... (他のメソッド: fillRoundedGradient, fillRoundedSolid, cornerInset, lerpArgb, drawGlowBorder, drawSeparator は変更なし) ...
    public static void fillRoundedGradient(GuiGraphics gg, float x0, float y0, float x1, float y1,
                                           float radius, int colorTop, int colorBottom) {
        float width = x1 - x0;
        float height = y1 - y0;
        float r = Math.min(radius, Math.min(width, height) / 2f);

        int rows = Math.max(1, Math.round(height));
        for (int i = 0; i < rows; i++) {
            int rowY0 = Math.round(y0 + i);
            int rowY1 = Math.min(Math.round(y0 + i + 1), Math.round(y1));
            if (rowY1 <= rowY0) rowY1 = rowY0 + 1;

            float inset = cornerInset(i, rows, r);
            int lx0 = Math.round(x0 + inset);
            int lx1 = Math.round(x1 - inset);
            if (lx1 <= lx0) continue;

            float t = (i + 0.5f) / rows;
            int color = lerpArgb(colorTop, colorBottom, t);

            gg.fill(lx0, rowY0, lx1, rowY1, color);
        }
    }

    public static void fillRoundedSolid(GuiGraphics gg, float x0, float y0, float x1, float y1,
                                        float radius, int argb) {
        fillRoundedGradient(gg, x0, y0, x1, y1, radius, argb, argb);
    }

    private static float cornerInset(int i, int rows, float radius) {
        if (radius <= 0) return 0;
        float dyTop = radius - (i + 0.5f);
        float dyBot = radius - (rows - i - 0.5f);
        float dy = Math.max(dyTop, dyBot);
        if (dy <= 0) return 0;
        if (dy >= radius) return radius;
        float dx = (float) Math.sqrt(Math.max(0, radius * radius - dy * dy));
        return radius - dx;
    }

    private static int lerpArgb(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int aa = (a >> 24) & 0xFF, ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int ba = (b >> 24) & 0xFF, br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        int ra = (int) (aa + (ba - aa) * t);
        int rr = (int) (ar + (br - ar) * t);
        int rg = (int) (ag + (bg - ag) * t);
        int rb = (int) (ab + (bb - ab) * t);
        return (ra << 24) | (rr << 16) | (rg << 8) | rb;
    }

    public static void drawGlowBorder(GuiGraphics gg, float x0, float y0, float x1, float y1,
                                      float radius, int glowRgb, float time) {
        float pulse = 0.65f + 0.35f * (float) Math.sin(time * 3.0);
        int layers = 5;
        for (int i = layers; i >= 1; i--) {
            float spread = i * 1.6f;
            int alpha = (int) (30 * pulse * (1f - (float) i / (layers + 1)));
            alpha = Math.max(8, alpha);
            int color = (alpha << 24) | (glowRgb & 0xFFFFFF);
            fillRoundedSolid(gg, x0 - spread, y0 - spread, x1 + spread, y1 + spread, radius + spread, color);
        }
        int rimAlpha = (int) (220 * pulse);
        int rim = (rimAlpha << 24) | (glowRgb & 0xFFFFFF);
        fillRoundedSolid(gg, x0 - 1.2f, y0 - 1.2f, x1 + 1.2f, y1 + 1.2f, radius + 1.2f, rim);
    }

    public static void drawSeparator(GuiGraphics gg, float x0, float x1, float y, int colorRgb) {
        int width = Math.round(x1 - x0);
        if (width <= 0) return;
        int step = 2;
        int yInt = Math.round(y);
        for (int px = 0; px < width; px += step) {
            float t = (float) px / width;
            float fade = (float) Math.sin(Math.PI * t);
            int alpha = (int) (220 * fade);
            if (alpha <= 2) continue;
            int color = (alpha << 24) | (colorRgb & 0xFFFFFF);
            int sx0 = Math.round(x0) + px;
            int sx1 = Math.min(sx0 + step, Math.round(x1));
            gg.fill(sx0, yInt, sx1, yInt + 1, color);
        }
    }

    /**
     * Componentのスタイル（色）を保持して中央揃えで描画し、周囲に発光エフェクトを追加します。
     */
    public static int drawCenteredGlowText(GuiGraphics gg, Font font, Component component,
                                           float centerX, float y, int fallbackColor, int glowRgb) {
        // 幅の計算
        int width = font.width(component);
        int x = Math.round(centerX - width / 2f);

        // 視覚的な順序に変換
        FormattedCharSequence visualOrder = component.getVisualOrderText();
        // 発光レイヤー
        int glowAlpha = 70;
        int glow = (glowAlpha << 24) | (glowRgb & 0xFFFFFF);
        int[][] offsets = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {1, 1}, {-1, 1}, {1, -1}};
        for (int[] off : offsets) {
            gg.drawString(font, visualOrder, x + off[0], (int) y + off[1], glow, false);
        }

        // メインテキスト
        gg.drawString(font, visualOrder, x, (int) y, -1, true);

        return width;
    }

    public static void renderRotatingItem(GuiGraphics gg, ItemStack stack, float centerX, float centerY,
                                          float pixelSize, float rotationDeg, float tiltDeg) {
        if (stack.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        ItemRenderer itemRenderer = mc.getItemRenderer();

        gg.flush();
        PoseStack pose = gg.pose();
        pose.pushPose();
        pose.translate(centerX, centerY, 200f);
        pose.scale(1f, -1f, 1f);
        pose.mulPose(Axis.XP.rotationDegrees(tiltDeg));
        pose.mulPose(Axis.YP.rotationDegrees(rotationDeg));
        pose.scale(pixelSize, pixelSize, pixelSize);

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        Lighting.setupFor3DItems();
        itemRenderer.renderStatic(stack, ItemDisplayContext.GUI, 15728880,
                OverlayTexture.NO_OVERLAY, pose, bufferSource, mc.level, 0);
        bufferSource.endBatch();
        Lighting.setupForFlatItems();

        pose.popPose();
    }

    public static void drawSparkles(GuiGraphics gg, float x0, float y0, float x1, float y1,
                                    float time, int glowRgb, int count) {
        float width = x1 - x0;
        float height = y1 - y0;
        if (width <= 0 || height <= 0) return;

        Random rnd = new Random(9001);
        for (int i = 0; i < count; i++) {
            float seedX = rnd.nextFloat();
            float seedSpeed = 6f + rnd.nextFloat() * 10f;
            float seedPhase = rnd.nextFloat() * 1000f;

            float px = x0 + seedX * width;
            float py = y1 - ((time * seedSpeed + seedPhase) % height);
            float alphaF = (float) (0.5 + 0.5 * Math.sin(time * 4f + seedPhase));
            int alpha = (int) (140 * alphaF);
            if (alpha <= 4) continue;

            int col = (alpha << 24) | (glowRgb & 0xFFFFFF);
            int s = 1;
            gg.fill(Math.round(px - s), Math.round(py - s), Math.round(px + s), Math.round(py + s), col);
        }
    }
}