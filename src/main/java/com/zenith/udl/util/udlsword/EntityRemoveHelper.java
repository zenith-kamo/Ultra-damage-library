package com.zenith.udl.util.udlsword;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EntityRemoveHelper {


    // リフレクションヘルパー

    @SuppressWarnings("unchecked")
    public static Int2ObjectMap<Object> getEntityMap(ChunkMap chunkMap) throws Exception {
        Field FentityMap;
        try {
            FentityMap = ChunkMap.class.getDeclaredField("entityMap");
        } catch (NoSuchFieldException e) {
            FentityMap = ChunkMap.class.getDeclaredField("f_140150_");
        }
        FentityMap.setAccessible(true);
        return (Int2ObjectMap<Object>) FentityMap.get(chunkMap);
    }


    @SuppressWarnings("unchecked")
    public static Set<ServerPlayerConnection> getSeenByFromEntity(Object entityTrackingObject) throws Exception {
        Field seenByField;
        try {
            seenByField = entityTrackingObject.getClass().getDeclaredField("seenBy");
        } catch (NoSuchFieldException e) {
            seenByField = entityTrackingObject.getClass().getDeclaredField("f_140475_");
        }
        seenByField.setAccessible(true);
        return (Set<ServerPlayerConnection>) seenByField.get(entityTrackingObject);
    }

}
