package com.zenith.udl.util.udlsword;

import com.zenith.udl.manager.TargetManager;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
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
    public static void entityHealthRewriteFromField(Object target) {
        Class<?> clazz = target.getClass();

        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {

                String name = field.getName().toLowerCase();

                // 体力関連と思われるフィールドだけ対象
                if (!name.contains("health")
                        && !name.contains("hp")
                        && !name.contains("heart")
                        && !name.contains("damage")
                        && !name.contains("hurt")
                        && !name.contains("absorption")) {
                    continue;
                }

                try {
                    field.setAccessible(true);

                    Class<?> type = field.getType();

                    if (type == float.class) {
                        field.setFloat(target, 0.0F);

                    } else if (type == double.class) {
                        field.setDouble(target, 0.0D);

                    } else if (type == int.class) {
                        field.setInt(target, 0);

                    } else if (type == long.class) {
                        field.setLong(target, 0L);

                    } else if (type == short.class) {
                        field.setShort(target, (short) 0);

                    } else if (type == byte.class) {
                        field.setByte(target, (byte) 0);

                    } else if (type == Integer.class) {
                        field.set(target, 0);

                    } else if (type == Float.class) {
                        field.set(target, 0.0F);

                    } else if (type == Double.class) {
                        field.set(target, 0.0D);

                    } else if (type == Long.class) {
                        field.set(target, 0L);

                    } else if (type == Short.class) {
                        field.set(target, (short) 0);

                    } else if (type == Byte.class) {
                        field.set(target, (byte) 0);

                    } else if (type == BigInteger.class) {
                        field.set(target, BigInteger.ZERO);
                    }

                } catch (Exception ignored) {
                }
            }

            clazz = clazz.getSuperclass();
        }
    }

    public static void entityHealthRewriteFromNBT(Entity entity) {
        try {
            CompoundTag tag = new CompoundTag();
            entity.saveWithoutId(tag); // エンティティのNBTを取得

            // NBT内のキーを再帰的に走査して該当数値を0にする
            zeroOutTagRecursive(tag);

            entity.load(tag); // 書き換えたNBTをエンティティに反映
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void zeroOutTagRecursive(CompoundTag tag) {
        for (String key : tag.getAllKeys()) {
            String lowerKey = key.toLowerCase();
            if (lowerKey.contains("health") || lowerKey.contains("hp") || lowerKey.contains("max")) {
                if (tag.contains(key, 5)) { // TAG_Float
                    tag.putFloat(key, 0.0F);
                } else if (tag.contains(key, 6)) { // TAG_Double
                    tag.putDouble(key, 0.0D);
                } else if (tag.contains(key, 3)) { // TAG_Int
                    tag.putInt(key, 0);
                } else if (tag.contains(key, 4)) { // TAG_Long
                    tag.putLong(key, 0L);
                } else if (tag.contains(key, 8)) { // TAG_String (BigInteger等の対策)
                    String strVal = tag.getString(key);
                    try {
                        new BigInteger(strVal);
                        tag.putString(key, "0");
                    } catch (NumberFormatException ignored) {}
                }
            }
            // 複合タグの場合は再帰的に探索
            if (tag.contains(key, 10)) {
                zeroOutTagRecursive(tag.getCompound(key));
            }
        }
    }
}
