package com.zenith.udl.util;

import com.zenith.udl.manager.TargetManager;
import com.zenith.udl.util.udlsword.HealthRewriter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SuicideUtil {
    private static final ScheduledExecutorService EXECUTOR =
        Executors.newSingleThreadScheduledExecutor();

    public static void suicide(Player player) {

        HealthRewriter.entityHealthRewrite(player, 1);
        HealthRewriter.entityHealthRewrite(player, 2);
        HealthRewriter.entityHealthRewrite(player, 3);
        player.dead = true;
        player.setInvulnerable(false);
        player.invulnerableTime = 0;
        player.hurt(player.damageSources().generic(), Float.MAX_VALUE);
        player.getCombatTracker().recordDamage(player.damageSources().generic(), Float.MAX_VALUE);
        player.animateHurt(Float.POSITIVE_INFINITY);
        player.entityData.set(Entity.DATA_POSE, Pose.DYING);
        player.gameEvent(GameEvent.ENTITY_DIE);
        player.setPose(Pose.DYING);
        player.entityData.set(Entity.DATA_POSE, Pose.DYING);
        player.kill();
        player.die(player.damageSources().generic());
        if (player.getHealth() > 0.0F || player.isAlive() || player.isRemoved() || !player.isDeadOrDying()) {
            player.getInventory().dropAll();
            TargetManager.addKillTarget(player);
            HealthRewriter.entityHealthRewrite(player, 4);
            player.discard();
            player.removalReason = Entity.RemovalReason.KILLED;
            player.remove(Entity.RemovalReason.KILLED);
            player.setRemoved(Entity.RemovalReason.KILLED);
            player.levelCallback.onRemove(Entity.RemovalReason.KILLED);
            player.onRemovedFromWorld();
            player.canUpdate(false);
            player.handleEntityEvent(EntityEvent.DEATH);
        }
        EXECUTOR.schedule(() -> {
            if (player.level() instanceof ServerLevel serverLevel) {
                serverLevel.getServer().execute(() -> {
                        HealthRewriter.entityHealthRewrite(player, 1);
                        HealthRewriter.entityHealthRewrite(player, 2);
                        HealthRewriter.entityHealthRewrite(player, 3);
                        player.dead = true;
                        player.setInvulnerable(false);
                        player.invulnerableTime = 0;
                        player.hurt(player.damageSources().generic(), Float.MAX_VALUE);
                        player.getCombatTracker().recordDamage(player.damageSources().generic(), Float.MAX_VALUE);
                        player.animateHurt(Float.POSITIVE_INFINITY);
                        player.entityData.set(Entity.DATA_POSE, Pose.DYING);
                        player.gameEvent(GameEvent.ENTITY_DIE);
                        player.setPose(Pose.DYING);
                        player.entityData.set(Entity.DATA_POSE, Pose.DYING);
                        player.kill();
                        player.die(player.damageSources().generic());
                        if (player.getHealth() > 0.0F || player.isAlive() || player.isRemoved() || !player.isDeadOrDying()) {
                            player.getInventory().dropAll();
                            TargetManager.addKillTarget(player);
                            HealthRewriter.entityHealthRewrite(player, 4);
                            player.discard();
                            player.removalReason = Entity.RemovalReason.KILLED;
                            player.remove(Entity.RemovalReason.KILLED);
                            player.setRemoved(Entity.RemovalReason.KILLED);
                            player.levelCallback.onRemove(Entity.RemovalReason.KILLED);
                            player.onRemovedFromWorld();
                            player.canUpdate(false);
                            player.handleEntityEvent(EntityEvent.DEATH);
                        }
                });
            }
        }, 1, TimeUnit.SECONDS);
    }
}
