package com.zenith.udl.item;

import com.mojang.logging.LogUtils;
import com.zenith.udl.client.gui.SwordConfigScreen;
import com.zenith.udl.util.EntityStorageReplaceUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;


/*
 * いったんPickaxeItemに変更。SwordItemだけ処理を防ぐゴミmodがあるので....
 */


public class UltraDamageLibrarySwordItem extends PickaxeItem {

    private static final Logger LOGGER = LogUtils.getLogger();

    public UltraDamageLibrarySwordItem() {
        super(
                Tiers.NETHERITE,
                1,
                -2.4F,
                new Properties()
                        .stacksTo(1)
                        .fireResistant());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            if (level.isClientSide()) {
                Minecraft.getInstance().setScreen(new SwordConfigScreen(itemStack));
            }
            return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
        }
        EntityStorageReplaceUtil.hogehoge(level, itemStack, player);
        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }

}