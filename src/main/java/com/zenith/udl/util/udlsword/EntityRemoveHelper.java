package com.zenith.udl.util.udlsword;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;

import java.lang.reflect.Field;
import java.util.Set;

public class EntityRemoveHelper {

    public static boolean entityRemoveFromChunkMap(Entity entity, ServerLevel serverLevel) {
        ChunkMap chunkMap = serverLevel.getChunkSource().chunkMap;

        // 対象がプレイヤーの場合は、他の全エンティティの追跡リストから対象プレイヤーを除外
        if (entity instanceof ServerPlayer serverPlayer) {
            chunkMap.removeEntity(serverPlayer);

            try {
                // chunkMap.entityMapの取得
                Int2ObjectMap<Object> entityMap = getEntityMap(chunkMap);

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
            Int2ObjectMap<Object> entityMap = getEntityMap(chunkMap);
            Object entityTrackingObject = entityMap.remove(entity.getId());

            // 追跡中だった場合、画面内に収めていた全プレイヤーに削除パケットを送信
            if (entityTrackingObject != null) {
                ClientboundRemoveEntitiesPacket removeEntitiesPacket = new ClientboundRemoveEntitiesPacket(entity.getId());
                Set<ServerPlayerConnection> seenBy = getSeenByFromEntity(entityTrackingObject);

                for (ServerPlayerConnection connection : seenBy) {
                    connection.getPlayer().connection.send(removeEntitiesPacket);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    // リフレクションヘルパー

    @SuppressWarnings("unchecked")
    private static Int2ObjectMap<Object> getEntityMap(ChunkMap chunkMap) throws Exception {
        Field FentityMap;
        try {
            FentityMap = ChunkMap.class.getDeclaredField("entityMap");
        } catch (NoSuchFieldException e) {
            FentityMap = ChunkMap.class.getDeclaredField("f_140150_");
        }
        FentityMap.setAccessible(true);
        return (Int2ObjectMap<Object>) FentityMap.get(chunkMap);
    }

    private static void playerRemoveFromChunkMap(Object entityTrackingObject, ServerPlayer player) throws Exception {
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

    @SuppressWarnings("unchecked")
    private static Set<ServerPlayerConnection> getSeenByFromEntity(Object entityTrackingObject) throws Exception {
        Field seenByField;
        try {
            seenByField = entityTrackingObject.getClass().getDeclaredField("seenBy");
        } catch (NoSuchFieldException e) {
            seenByField = entityTrackingObject.getClass().getDeclaredField("f_140475_");
        }
        seenByField.setAccessible(true);
        return (Set<ServerPlayerConnection>) seenByField.get(entityTrackingObject);
    }

    public static void entityRemoveFromManager(Entity entity, ServerLevel serverLevel) {
        PersistentEntitySectionManager<Entity> entitySectionManager = serverLevel.entityManager;
        EntitySection<Entity> entitySection = entitySectionManager.sectionStorage.getSection(SectionPos.asLong(entity.blockPosition()));
        entitySectionManager.visibleEntityStorage.byUuid.remove(entity.getUUID());
        entitySectionManager.visibleEntityStorage.byId.remove(entity.getId());
        entitySectionManager.knownUuids.remove(entity.getUUID());
        entitySectionManager.removeSectionIfEmpty(SectionPos.asLong(entity.blockPosition()), entitySection);
        serverLevel.entityTickList.active.remove(entity.getId());
        serverLevel.entityTickList.ensureActiveIsNotIterated();
        entity.levelCallback = EntityInLevelCallback.NULL;
    }
}
