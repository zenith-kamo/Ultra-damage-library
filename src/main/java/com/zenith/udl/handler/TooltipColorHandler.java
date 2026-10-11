package com.zenith.udl.handler;

import com.zenith.udl.item.IColoredTooltip;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "udl", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class TooltipColorHandler {

    private TooltipColorHandler() {}

    @SubscribeEvent
    public static void onRenderTooltipColor(RenderTooltipEvent.Color event) {
        ItemStack stack = event.getItemStack();

        if (!(stack.getItem() instanceof IColoredTooltip item)) {
            return;
        }

        event.setBackground(item.getTooltipBackgroundColor());
        event.setBorderStart(item.getTooltipBorderTopColor());
        event.setBorderEnd(item.getTooltipBorderBottomColor());
    }
}