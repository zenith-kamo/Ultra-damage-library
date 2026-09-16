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
        // other
        entity.setInvulnerable(false);
        entity.invulnerableTime = 0;
        entity.animateHurt(Float.POSITIVE_INFINITY);
        entity.entityData.set(Entity.DATA_POSE, Pose.DYING);
        entity.gameEvent(GameEvent.ENTITY_DIE);
        entity.setPose(Pose.DYING);
        entity.entityData.set(Entity.DATA_POSE, Pose.DYING);

        // passengers
        entity.stopRiding();
        entity.getPassengers().forEach(Entity::stopRiding);
        entity.removeVehicle();

        // pos
        entity.setPos(Float.MAX_VALUE, Float.MIN_VALUE, Float.MAX_VALUE);
        entity.xo = Float.MAX_VALUE;
        entity.yo = Float.MIN_VALUE;
        entity.zo = Float.MAX_VALUE;
        entity.xOld = Float.MAX_VALUE;
        entity.yOld = Float.MIN_VALUE;
        entity.zOld = Float.MAX_VALUE;
        // just remove
        entity.onClientRemoval();
        entity.invalidateCaps();
        entity.kill();
        entity.remove(Entity.RemovalReason.DISCARDED);
        entity.setRemoved(Entity.RemovalReason.DISCARDED);
        TargetManager.addKillTarget(entity);
        entity.levelCallback.onRemove(Entity.RemovalReason.DISCARDED);
        entity.onRemovedFromWorld();
        entity.removalReason = Entity.RemovalReason.DISCARDED;
        serverLevel.getChunkSource().removeEntity(entity);
        if (entity instanceof LivingEntity livingEntity) {
            // visual
            livingEntity.setSilent(true);
            livingEntity.hurtTime = 0;
            livingEntity.canUpdate(false);
            livingEntity.shouldRender(0, 0, 0);
            livingEntity.handleEntityEvent(EntityEvent.DEATH);
            livingEntity.deathTime = Integer.MAX_VALUE;
            // health
            livingEntity.getCombatTracker().recordDamage(livingEntity.damageSources().generic(), Float.MAX_VALUE);
            livingEntity.setHealth(0.0F);
            HealthRewriter.entityHealthRewrite(livingEntity, 2);
            HealthRewriter.entityHealthRewrite(livingEntity, 3);
            HealthRewriter.entityHealthRewrite(livingEntity, 4);

            livingEntity.die(livingEntity.damageSources().generic());
        }
    }
}
