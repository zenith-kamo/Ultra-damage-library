package com.zenith.udl.manager;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public class TargetManager {
    // LivingEntityのみ
    private static final Set<LivingEntity> HEALTH_TARGETS =
            Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
    // Entity全般
    private static final Set<Entity> KILL_TARGETS =
            Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    private static final Set<Entity> HIDDEN_TARGETS =
            Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    private static final Set<Entity> TP_TARGETS =
            Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    private static final Set<LivingEntity> POSE_TARGETS =
            Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    public static void addHealthTarget(LivingEntity entity) {
        if (entity != null && !entity.isRemoved()) {
            HEALTH_TARGETS.add(entity);
        }
    }

    public static void removeHealthTarget(LivingEntity entity) {
        if (entity != null) {
            HEALTH_TARGETS.remove(entity);
        }
    }

    public static void clearHealthTarget() {
        HEALTH_TARGETS.clear();
    }

    public static boolean isHealthTarget(LivingEntity entity) {
        return entity != null && !entity.isRemoved() && HEALTH_TARGETS.contains(entity);
    }

    public static Set<LivingEntity> getHealthTargets() {
        return HEALTH_TARGETS;
    }

    // Entity全般
    public static void addKillTarget(Entity entity) {
        if (entity != null && !entity.isRemoved()) {
            KILL_TARGETS.add(entity);
        }
    }

    public static void removeKillTarget(Entity entity) {
        if (entity != null) {
            KILL_TARGETS.remove(entity);
        }
    }

    public static void clearKillTarget() {
        KILL_TARGETS.clear();
    }

    public static boolean isKillTarget(Entity entity) {
        return entity != null && !entity.isRemoved() && KILL_TARGETS.contains(entity);
    }

    public static Set<Entity> getKillTargets() {
        return KILL_TARGETS;
    }

    // Hidden
    public static void addHiddenTarget(Entity entity) {
        if (entity != null && !entity.isRemoved()) {
            HIDDEN_TARGETS.add(entity);
        }
    }

    public static void removeHiddenTarget(Entity entity) {
        if (entity != null) {
            HIDDEN_TARGETS.remove(entity);
        }
    }

    public static void clearHiddenTarget() {
        HIDDEN_TARGETS.clear();
    }

    public static boolean isHiddenTarget(Entity entity) {
        return entity != null && !entity.isRemoved() && HIDDEN_TARGETS.contains(entity);
    }

    public static Set<Entity> getHiddenTargets() {
        return HIDDEN_TARGETS;
    }

    public static void addTpTarget(Entity entity) {
        if (entity != null && !entity.isRemoved()) {
            TP_TARGETS.add(entity);
        }
    }

    public static void removeTpTarget(Entity entity) {
        if (entity != null) {
            TP_TARGETS.remove(entity);
        }
    }

    public static void clearTpTarget() {
        TP_TARGETS.clear();
    }

    public static boolean isTpTarget(Entity entity) {
        return entity != null && !entity.isRemoved() && TP_TARGETS.contains(entity);
    }

    public static Set<Entity> getTpTargets() {
        return TP_TARGETS;
    }

    public static void addPoseTarget(LivingEntity livingEntity) {
        if (livingEntity != null && !livingEntity.isRemoved()) {
            POSE_TARGETS.add(livingEntity);
        }
    }

    public static void removePoseTarget(LivingEntity livingEntity) {
        if (livingEntity != null) {
            POSE_TARGETS.remove(livingEntity);
        }
    }

    public static void clearPoseTarget() {
        POSE_TARGETS.clear();
    }

    public static boolean isPoseTarget(LivingEntity livingEntity) {
        return livingEntity != null && !livingEntity.isRemoved() && POSE_TARGETS.contains(livingEntity);
    }

    public static Set<LivingEntity> getPoseTargets() {
        return POSE_TARGETS;
    }
}