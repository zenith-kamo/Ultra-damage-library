package com.zenith.udl.util;

import com.zenith.udl.manager.TargetManager;
import com.zenith.udl.util.udlsword.HealthRewriter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.entity.PartEntity;

public class EntityRemoveUtil {
    public static void removeEntity(Entity entity, ServerLevel serverLevel) {
        if (entity instanceof PartEntity<?> partEntity) {
            removeEntity(partEntity.getParent(), serverLevel);
            return;
        }
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
        serverLevel.getChunkSource().removeEntity(entity);
        entity.setPos(Float.MAX_VALUE, Float.MIN_VALUE, Float.MAX_VALUE);
        entity.xo = Float.MAX_VALUE;
        entity.yo = Float.MIN_VALUE;
        entity.zo = Float.MAX_VALUE;
        entity.xOld = Float.MAX_VALUE;
        entity.yOld = Float.MIN_VALUE;
        entity.zOld = Float.MAX_VALUE;
        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.deathTime = Integer.MAX_VALUE;
            livingEntity.isDeadOrDying();
            livingEntity.setHealth(0.0F);
            TargetManager.addHealthTarget(livingEntity);
            livingEntity.canUpdate(false);
            livingEntity.shouldRender(0,0,0);
            livingEntity.handleEntityEvent(EntityEvent.DEATH);
            HealthRewriter.entityHealthRewrite(livingEntity, 2);
            HealthRewriter.entityHealthRewrite(livingEntity, 3);
            livingEntity.deathTime = 20;
            livingEntity.hurtTime = 0;
            livingEntity.setSilent(true);
            livingEntity.getCombatTracker().recordDamage(livingEntity.damageSources().generic(), Float.MAX_VALUE);
            livingEntity.die(livingEntity.damageSources().generic());
        }

    }
}
