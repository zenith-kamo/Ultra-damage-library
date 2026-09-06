package com.zenith.udl.item;

import com.zenith.udl.Udl;
import com.zenith.udl.manager.TimeStopManager;
import com.zenith.udl.network.NetworkHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class PocketWatchItem extends Item {

    public PocketWatchItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal("Zenith's pocket watch");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        Udl.LOGGER.info("Use!");
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
                if (TimeStopManager.isTimeStopped()) {
                    Udl.LOGGER.info("Start Time");
                    TimeStopManager.resumeTime();
                    NetworkHandler.sendToAll(false);
                } else {
                    Udl.LOGGER.info("Stop Time");
                    TimeStopManager.stopTime(player.getUUID());
                    NetworkHandler.sendToAll(true);
                }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}