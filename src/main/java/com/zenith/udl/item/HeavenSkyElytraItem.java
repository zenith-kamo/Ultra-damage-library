
package com.zenith.udl.item;

import com.zenith.udl.util.ColorUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class HeavenSkyElytraItem extends ElytraItem {

    public HeavenSkyElytraItem() {
        super(new Item.Properties().durability(Integer.MAX_VALUE));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(ColorUtil.makeRainbow("天を覆いし蒼穹の、その頂に至るもの。"));
        tooltip.add(ColorUtil.makeRainbow("雲は道となり、風は従者となる。"));
        tooltip.add(ColorUtil.makeRainbow("天地の境界に、この翼を阻むものはない。"));

    }
}
