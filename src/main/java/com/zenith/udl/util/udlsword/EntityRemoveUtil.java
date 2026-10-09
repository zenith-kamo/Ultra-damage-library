package com.zenith.udl.util.udlsword;

import com.zenith.udl.manager.TargetManager;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraft.world.level.entity.TransientEntitySectionManager;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class EntityRemoveUtil {
    public static void removeEntity(Entity entity, ServerLevel serverLevel) {
        if (entity == null) return;
        if (entity instanceof PartEntity<?> partEntity) {
            removeEntity(partEntity.getParent(), serverLevel);
            return;
        }
        if (entity instanceof LivingEntity livingEntity) TargetManager.addPoseTarget(livingEntity);
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
        EntityTeleportUtil.EntityTeleport(entity);
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
        entity.updateDynamicGameEventListener(DynamicGameEventListener::remove);
        MinecraftForge.EVENT_BUS.post(new EntityLeaveLevelEvent(entity, serverLevel));
        EntityRemoveHelper.wipeEntityData(entity);
        if (entity instanceof LivingEntity livingEntity) {
            // visual
            livingEntity.setSilent(true);
            livingEntity.removeAllEffects();
            livingEntity.setDiscardFriction(true);
            livingEntity.hurtTime = 0;
            livingEntity.canUpdate(false);
            livingEntity.shouldRender(0, 0, 0);
            livingEntity.handleEntityEvent(EntityEvent.DEATH);
            livingEntity.deathTime = Integer.MAX_VALUE;
            // health
            EntityRemoveHelper.hurtWithAllDamageType(livingEntity, livingEntity, Float.MAX_VALUE);
            livingEntity.hurt(livingEntity.damageSources().generic(), Float.MAX_VALUE);
            livingEntity.getCombatTracker().recordDamage(livingEntity.damageSources().generic(), Float.MAX_VALUE);
            livingEntity.setAbsorptionAmount(0.0F);
//            livingEntity.actuallyHurt(livingEntity.damageSources().generic(), Float.MAX_VALUE);
            livingEntity.lastHurt = Float.MAX_VALUE;
            livingEntity.dead = true;
            livingEntity.setHealth(0.0F);
            if (livingEntity.getAttribute(Attributes.MAX_HEALTH) != null) {
                livingEntity.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double)0.0F);
            }
            HealthRewriter.entityHealthRewrite(livingEntity, 2);
            HealthRewriter.entityHealthRewrite(livingEntity, 3);
            HealthRewriter.entityHealthRewrite(livingEntity, 4);

            livingEntity.die(livingEntity.damageSources().generic());
        }
    }

    public static void entityRemoveFromChunkMap(Entity entity, ServerLevel serverLevel) {
        if (entity == null) return;
        ChunkMap chunkMap = serverLevel.getChunkSource().chunkMap;

        // 対象がプレイヤーの場合は、他の全エンティティの追跡リストから対象プレイヤーを除外
        if (entity instanceof ServerPlayer serverPlayer) {
            chunkMap.removeEntity(serverPlayer);

            try {
                // chunkMap.entityMapの取得
                Int2ObjectMap<Object> entityMap = EntityRemoveHelper.getEntityMap(chunkMap);

                // 全TrackedEntityから退出するプレイヤーを除外
                for (Object entityTrackingObject : entityMap.values()) {
                    playerRemoveFromChunkMap(entityTrackingObject, serverPlayer);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // entityMapから対象エンティティを削除
        try {
            Int2ObjectMap<Object> entityMap = EntityRemoveHelper.getEntityMap(chunkMap);
            Object entityTrackingObject = entityMap.remove(entity.getId());

            // 追跡中だった場合、画面内に収めていた全プレイヤーに削除パケットを送信
            if (entityTrackingObject != null) {
                ClientboundRemoveEntitiesPacket removeEntitiesPacket = new ClientboundRemoveEntitiesPacket(entity.getId());
                Set<ServerPlayerConnection> seenBy = EntityRemoveHelper.getSeenByFromEntity(entityTrackingObject);

                for (ServerPlayerConnection connection : seenBy) {
                    connection.getPlayer().connection.send(removeEntitiesPacket);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void playerRemoveFromChunkMap(Object entityTrackingObject, ServerPlayer player) throws Exception {
        if (player == null) return;
        try {
            var method = entityTrackingObject.getClass().getDeclaredMethod("removePlayer", ServerPlayer.class);
            method.setAccessible(true);
            method.invoke(entityTrackingObject, player);
        } catch (NoSuchMethodException e) {
            var method = entityTrackingObject.getClass().getDeclaredMethod("m_140485_", ServerPlayer.class);
            method.setAccessible(true);
            method.invoke(entityTrackingObject, player);
        }
    }

    public static void entityRemoveFromPersistentManager(Entity entity, ServerLevel serverLevel) {
        if (entity == null) return;

        PersistentEntitySectionManager<Entity> entitySectionManager = serverLevel.entityManager;

        EntitySection<Entity> entitySection = entitySectionManager.sectionStorage.getSection(SectionPos.asLong(entity.blockPosition()));
        if (entitySectionManager != null)
            entitySectionManager.visibleEntityStorage.byUuid.remove(entity.getUUID());
        if (entitySectionManager != null)
            entitySectionManager.visibleEntityStorage.byId.remove(entity.getId());
        serverLevel.entityManager.visibleEntityStorage.byId.remove(entity.getId());
        serverLevel.entityManager.visibleEntityStorage.byUuid.remove(entity.getUUID());
        if (entitySectionManager != null)
            entitySectionManager.knownUuids.remove(entity.getUUID());

        // fix: EntitySectionが存在しない場合はremoveSectionIfEmptyを呼ばない
        if (entitySectionManager != null && entitySection != null)
            entitySectionManager.removeSectionIfEmpty(
                    SectionPos.asLong(entity.blockPosition()),
                    entitySection
            );
        serverLevel.entityTickList.active.remove(entity.getId());
        serverLevel.entityTickList.ensureActiveIsNotIterated();
        entity.levelCallback = EntityInLevelCallback.NULL;
    }

    public static void entityRemoveFromTransientManager(Entity entity, Level level, boolean onRemoved) {
        if (level instanceof ClientLevel clientLevel) {
            TransientEntitySectionManager<Entity> transientEntitySectionManager = clientLevel.entityStorage;
            EntitySection<Entity> entitySection = transientEntitySectionManager.sectionStorage.getSection(SectionPos.asLong(entity.blockPosition()));
            if (entitySection == null) {
                return;
            }
            clientLevel.tickingEntities.ensureActiveIsNotIterated();
            clientLevel.tickingEntities.active.remove(entity.getId());
            if (entitySection != null) transientEntitySectionManager.entityStorage.byUuid.remove(entity.getUUID());
            if (entitySection != null) transientEntitySectionManager.entityStorage.byId.remove(entity.getId());
            if (entitySection != null) transientEntitySectionManager.removeSectionIfEmpty(SectionPos.asLong(entity.blockPosition()), entitySection);
            entity.levelCallback = EntityInLevelCallback.NULL;
        }
    }
}

