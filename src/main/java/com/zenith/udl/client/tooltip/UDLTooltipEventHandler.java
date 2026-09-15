package com.zenith.udl.client.tooltip;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = "udl", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class UDLTooltipEventHandler {

    private UDLTooltipEventHandler() {}

    private static final int PADDING = 10;
    private static final int LINE_HEIGHT = 10;
    private static final float CORNER_RADIUS = 8f;

    @SubscribeEvent
    public static void onTooltipPre(RenderTooltipEvent.Pre event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof IUDLGlowTooltip glow)) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        GuiGraphics gg = event.getGraphics();
        Font font = event.getFont();

        // ツールチップ本文を取得
        TooltipFlag flag = mc.options.advancedItemTooltips
                ? TooltipFlag.Default.ADVANCED
                : TooltipFlag.Default.NORMAL;

        // Componentのリストをそのまま保持（スタイル情報を失わないため）
        List<Component> rawLines = stack.getTooltipLines(mc.player, flag);
        List<Component> lines = new ArrayList<>(rawLines);

        if (lines.isEmpty()) {
            return; // 表示するものが無ければ何もしない
        }

        event.setCanceled(true);

        boolean showItem = glow.udl$showItemModel();
        float itemAreaSize = showItem ? 68f : 0f;

        int maxTextWidth = 0;
        // 幅の計算には getString() を使用
        for (Component c : lines) {
            maxTextWidth = Math.max(maxTextWidth, font.width(c.getString()));
        }

        List<Integer> separators = glow.udl$separatorAfterLines(stack);

        int contentWidth = Math.max(maxTextWidth, showItem ? 90 : 60);
        int boxWidth = contentWidth + PADDING * 2;
        int textBlockHeight = lines.size() * LINE_HEIGHT;
        int separatorSpace = separators.size() * 8;
        int boxHeight = PADDING * 2 + (int) itemAreaSize + (showItem ? 6 : 0)
                + textBlockHeight + separatorSpace;

        int screenW = event.getScreenWidth();
        int screenH = event.getScreenHeight();
        int x = event.getX();
        int y = event.getY();
        if (x + boxWidth > screenW) x = Math.max(4, screenW - boxWidth - 4);
        if (y + boxHeight > screenH) y = Math.max(4, screenH - boxHeight - 4);

        float x0 = x;
        float y0 = y;
        float x1 = x + boxWidth;
        float y1 = y + boxHeight;

        float time = (System.currentTimeMillis() % 1_000_000L) / 1000f;
        int glowColor = glow.udl$getGlowColor();
        int gradTop = glow.udl$getGradientTop();
        int gradBottom = glow.udl$getGradientBottom();

        gg.pose().pushPose();
        gg.pose().translate(0, 0, 400); // 手前に描画してワールド/他GUI要素とのZファイティングを回避

        // 発光ボーダー（背景より先＝外側に滲む）
        TooltipRenderUtil.drawGlowBorder(gg, x0, y0, x1, y1, CORNER_RADIUS, glowColor, time);
        // グラデーション背景
        TooltipRenderUtil.fillRoundedGradient(gg, x0, y0, x1, y1, CORNER_RADIUS, gradTop, gradBottom);
        // 背景の浮遊発光パーティクル
        if (glow.udl$showSparkles()) {
            TooltipRenderUtil.drawSparkles(gg, x0 + 2, y0 + 2, x1 - 2, y1 - 2, time, glowColor, 14);
        }

        float centerX = (x0 + x1) / 2f;
        float cursorY = y0 + PADDING;

        // アイテム名を先に（元の色を保持）
        // fallbackColor は -1 を指定し、Component側の色または白を使用させる
        TooltipRenderUtil.drawCenteredGlowText(gg, font, lines.get(0), centerX, cursorY, -1, glowColor);
        cursorY += LINE_HEIGHT;

        // 3Dアイテムモデル
        if (showItem) {
            float rotation = (time * glow.udl$rotationSpeed()) % 360f;
            TooltipRenderUtil.renderRotatingItem(
                    gg, stack, centerX, cursorY + itemAreaSize / 2f,
                    itemAreaSize * 0.9f, rotation, glow.udl$tiltDegrees()
            );
            cursorY += itemAreaSize + 6;
        }

        if (separators.contains(0)) {
            cursorY += 2;
            TooltipRenderUtil.drawSeparator(gg, x0 + PADDING, x1 - PADDING, cursorY, glowColor);
            cursorY += 6;
        }

        // その他行（元の色を保持）
        for (int i = 1; i < lines.size(); i++) {
            TooltipRenderUtil.drawCenteredGlowText(gg, font, lines.get(i), centerX, cursorY, -1, glowColor);
            cursorY += LINE_HEIGHT;

            if (separators.contains(i)) {
                cursorY += 2;
                TooltipRenderUtil.drawSeparator(gg, x0 + PADDING, x1 - PADDING, cursorY, glowColor);
                cursorY += 6;
            }
        }

        gg.pose().popPose();
    }
}