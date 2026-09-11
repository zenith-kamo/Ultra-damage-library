package com.zenith.udl.util;

import com.zenith.udl.manager.TargetManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.gameevent.GameEvent;

public class EntityRemoveUtil {
    public static void removeEntity(Entity entity, ServerLevel serverLevel) {
        entity.remove(Entity.RemovalReason.DISCARDED);
        entity.setRemoved(Entity.RemovalReason.DISCARDED);
        entity.gameEvent(GameEvent.ENTITY_DIE);
        entity.onClientRemoval();
        entity.invalidateCaps();
        entity.stopRiding();
        entity.getPassengers().forEach(Entity::stopRiding);
        entity.removeVehicle();
        entity.levelCallback.onRemove(Entity.RemovalReason.DISCARDED);
        entity.entityData.set(Entity.DATA_POSE, Pose.DYING);
        entity.onRemovedFromWorld();
        entity.setInvulnerable(false);
        entity.invulnerableTime = 0;
        entity.animateHurt(Float.POSITIVE_INFINITY);
        entity.kill();
        TargetManager.addKillTarget(entity);
//        entity.isAddedToWorld = false;
        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.deathTime = Integer.MAX_VALUE;
            livingEntity.isDeadOrDying();
            livingEntity.setHealth(0.0F);
            TargetManager.addHealthTarget(livingEntity);
            livingEntity.canUpdate(false);
            livingEntity.shouldRender(0,0,0);
            livingEntity.handleEntityEvent(EntityEvent.DEATH);
            EntityUltraHurtUtil.EntityUltraHurt(livingEntity, LivingEntity.DATA_HEALTH_ID, 0.0F);
            EntityUltraHurtUtil.EntityHurt(livingEntity, LivingEntity.DATA_HEALTH_ID, 0.0F, true);
            TargetManager.addHealthTarget(livingEntity);
        }
//        serverLevel.getChunkSource().chunkMap.removeEntity(entity);

    }
}
