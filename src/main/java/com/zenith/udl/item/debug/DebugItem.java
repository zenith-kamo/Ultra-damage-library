package com.zenith.udl.item.debug;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DebugItem extends Item{
    public DebugItem(Item.Properties properties) {
        super(properties);
    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            Pig pig = EntityType.PIG.create(serverLevel);
            if (pig == null) return InteractionResultHolder.sidedSuccess(stack, false);
            pig.moveTo(player.getX(), player.getY(), player.getZ());
            boolean success = serverLevel.entityManager.addNewEntityWithoutEvent(pig);
            if (success) {
                pig.onAddedToWorld();
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

}
