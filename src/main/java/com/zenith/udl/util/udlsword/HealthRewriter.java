package com.zenith.udl.util.udlsword;

import com.zenith.udl.manager.TargetManager;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class HealthRewriter {
    public static void entityHealthRewrite(Entity entity, Integer level) {
        if (entity == null || entity.level().isClientSide()) {
            return;
        }
        if (entity instanceof LivingEntity livingEntity) {
            switch (level) {
                case 1:
                    livingEntity.setHealth(0.0F);
                    break;
                case 2:
                    livingEntity.entityData.set(LivingEntity.DATA_HEALTH_ID, 0.0F, true);
                    livingEntity.onSyncedDataUpdated(LivingEntity.DATA_HEALTH_ID);
                    break;
                case 3:
                    SynchedEntityData entityData = livingEntity.entityData;

                    Int2ObjectMap<SynchedEntityData.DataItem<?>> items = entityData.itemsById;
                    if (items == null || items.isEmpty()) return;

                    Set<EntityDataAccessor<?>> healthAccessors = scanHealthAccessors(livingEntity.getClass());

                    for (SynchedEntityData.DataItem<?> item : items.values()) {
                        EntityDataAccessor<?> accessor = item.getAccessor();
                        Object value = item.getValue();

                        // 値が Float かつ、取得した healthAccessors に含まれるか判定
                        if (value instanceof Float && healthAccessors.contains(accessor)) {
                            @SuppressWarnings("unchecked")
                            EntityDataAccessor<Float> floatAccessor = (EntityDataAccessor<Float>) accessor;

                            entityData.set(floatAccessor, 0.0F, true);
                        }
                    }
                    break;
                case 4:
                    TargetManager.addHealthTarget(livingEntity);
            }
        }
    }

    private static Set<EntityDataAccessor<?>> scanHealthAccessors(Class<?> clazz) {
        Set<EntityDataAccessor<?>> accessors = new HashSet<>();
        Class<?> current = clazz;

        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                // static かつ EntityDataAccessor 型のフィールドを対象とする
                if (Modifier.isStatic(field.getModifiers()) && EntityDataAccessor.class.isAssignableFrom(field.getType())) {
                    try {
                        field.setAccessible(true);
                        EntityDataAccessor<?> accessor = (EntityDataAccessor<?>) field.get(null);

                        if (accessor != null) {
                            // 名前判定に加え、型パラメータチェックの代わりに contains 判定用として登録
                            String fieldName = field.getName().toUpperCase();
                            if (fieldName.contains("HEALTH") || fieldName.contains("HP")) {
                                accessors.add(accessor);
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
            current = current.getSuperclass();
        }
        return accessors;
    }
}
