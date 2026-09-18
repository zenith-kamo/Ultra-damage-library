package com.zenith.udl.item.debug;

import com.zenith.udl.manager.TargetManager;
import com.zenith.udl.util.GetAllEntitiesUtil;
import com.zenith.udl.util.udlsword.EntityTeleportUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class Debug2Item extends Item{
    public Debug2Item(Properties properties) {
        super(properties);
    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            Iterable<Entity> entities = GetAllEntitiesUtil.getServerEntities(serverLevel);
            if (player.isShiftKeyDown()) {
                for (Entity entity : entities) {
                    if (entity != null && (entity != player)) {
                        TargetManager.addTpTarget(entity);
                    }
                }
            } else {
                for (Entity entity : entities) {
                    if (entity != null && (entity != player)) {
                        EntityTeleportUtil.EntityTeleport(entity);
                    }
                }
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

}
