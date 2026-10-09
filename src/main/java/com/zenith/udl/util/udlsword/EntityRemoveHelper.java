package com.zenith.udl.util.udlsword;

import com.zenith.udl.manager.TargetManager;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class EntityRemoveHelper {

    private static final UUID DUMMY_UUID = UUID.randomUUID();

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

    public static void wipeEntityData(Entity target) {
        if (shouldNotAttack(target)) {
            return;
        }

        try {
            // 1. 基本的なエンティティフラグを強制的に false に書き換え
            FieldUtils.writeField(target, "isAddedToWorld", false, true);
            FieldUtils.writeField(target, "canUpdate", false, true);
            FieldUtils.writeField(target, "valid", false, true);
            FieldUtils.writeField(target, "initialized", false, true);
            FieldUtils.writeField(target, "isLazy", false, true);

            // 2. クラス内の非finalフィールドをリフレクションで一括初期化（メモリ汚染）
            Field[] declaredFields = target.getClass().getDeclaredFields();
            for (Field field : declaredFields) {
                field.setAccessible(true);
                int modifiers = field.getModifiers();

                // finalフィールドは除外
                if (!Modifier.isFinal(modifiers)) {
                    Class<?> fieldType = field.getType();
                    Object instanceTarget = Modifier.isStatic(modifiers) ? null : target;

                    try {
                        // プリミティブ型およびラッパークラスの初期化
                        if (fieldType == Boolean.TYPE || fieldType == Boolean.class) {
                            field.set(instanceTarget, false);
                        } else if (fieldType == Byte.TYPE || fieldType == Byte.class) {
                            field.set(instanceTarget, (byte) 0);
                        } else if (fieldType == Short.TYPE || fieldType == Short.class) {
                            field.set(instanceTarget, (short) 0);
                        } else if (fieldType == Integer.TYPE || fieldType == Integer.class) {
                            field.set(instanceTarget, 0);
                        } else if (fieldType == Long.TYPE || fieldType == Long.class) {
                            field.set(instanceTarget, 0L);
                        } else if (fieldType == Float.TYPE || fieldType == Float.class) {
                            field.set(instanceTarget, 0.0F);
                        } else if (fieldType == Double.TYPE || fieldType == Double.class) {
                            field.set(instanceTarget, 0.0D);
                        }
                        // コレクション・反復可能オブジェクトの要素全削除
                        else if (Collection.class.isAssignableFrom(fieldType)) {
                            Collection<?> collection = (Collection<?>) field.get(instanceTarget);
                            if (collection != null) {
                                collection.clear();
                            }
                        } else if (Iterable.class.isAssignableFrom(fieldType)) {
                            Iterable<?> iterable = (Iterable<?>) field.get(instanceTarget);
                            if (iterable != null) {
                                Iterator<?> iterator = iterable.iterator();
                                while (iterator.hasNext()) {
                                    iterator.next();
                                    try {
                                        iterator.remove();
                                    } catch (Throwable ignored) {
                                    }
                                }
                            }
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }

            // 3. NBTタグの改ざんと再ロード
            try {
                CompoundTag originalNbt = new CompoundTag();
                target.saveWithoutId(originalNbt);

                CompoundTag modifiedNbt = new CompoundTag();
                ListTag emptyList = new ListTag();

                // 元のNBTキーを走査し、キー名に応じて値を異常値へ置換
                for (String key : originalNbt.getAllKeys()) {
                    Tag tag = originalNbt.get(key);
                    if (tag == null) continue;

                    byte tagId = tag.getId();
                    String lowerKey = key.toLowerCase();

                    boolean isHealthOrArmor = lowerKey.contains("health") || lowerKey.contains("armor");
                    boolean isPosition = lowerKey.contains("pos") || lowerKey.contains("position");
                    boolean isName = lowerKey.contains("name");

                    // ステータス関連のタグリセット
                    if (isHealthOrArmor && (tagId == 3 || tagId == 4 || tagId == 5 || tagId == 6)) {
                        if (tagId == 5) modifiedNbt.putFloat(key, 0.0F);
                        else if (tagId == 6) modifiedNbt.putDouble(key, 0.0D);
                        else if (tagId == 4) modifiedNbt.putLong(key, 0L);
                        else modifiedNbt.putInt(key, 0);
                    }
                    // 座標関連の異常値化
                    else if (isPosition && (tagId == 3 || tagId == 4 || tagId == 6)) {
                        if (tagId == 3) modifiedNbt.putInt(key, -999999);
                        else if (tagId == 4) modifiedNbt.putLong(key, -999999L);
                        else modifiedNbt.putDouble(key, -999999.0D);
                    }
                    // 名前タグの削除
                    else if (isName && tagId == 8) {
                        modifiedNbt.putString(key, "");
                    }
                    // その他のタグはそのままコピー
                    else {
                        modifiedNbt.put(key, tag.copy());
                    }
                }

                // 共通の異常状態フラグ・パラメータの設定
                modifiedNbt.putBoolean("CustomNameVisible", false);
                modifiedNbt.putBoolean("Invulnerable", false);
                modifiedNbt.putBoolean("OnGround", false);
                modifiedNbt.putBoolean("Silent", true);
                modifiedNbt.putBoolean("CanUpdate", false);
                modifiedNbt.putBoolean("HasVisualFire", false);
                modifiedNbt.putBoolean("Glowing", false);
                modifiedNbt.putString("CustomName", "");
                modifiedNbt.putShort("Fire", (short) 32767);
                modifiedNbt.putFloat("FallDistance", Float.MAX_VALUE);
                modifiedNbt.putShort("Air", Short.MIN_VALUE);
                modifiedNbt.putInt("TicksFrozen", Integer.MAX_VALUE);
                modifiedNbt.putUUID("UUID", DUMMY_UUID);

                ListTag posList = new ListTag();
                for (int i = 0; i < 3; i++) {
                    posList.add(DoubleTag.valueOf(-999999.0D));
                }
                modifiedNbt.put("Pos", posList);
                modifiedNbt.putString("id", "0");
                modifiedNbt.put("ForgeCaps", new CompoundTag());
                modifiedNbt.put("ForgeData", new CompoundTag());
                modifiedNbt.put("Tags", emptyList);
                modifiedNbt.put("Passengers", emptyList);

                // エンティティの型に応じた固有データ改ざんと書き戻し
                if (target instanceof LivingEntity livingTarget) {
                    modifiedNbt.putFloat("Health", 0.0F);
                    modifiedNbt.putShort("HurtTime", (short) 32767);
                    modifiedNbt.putInt("HurtByTimestamp", Integer.MAX_VALUE);
                    modifiedNbt.putShort("DeathTime", (short) 32767);
                    modifiedNbt.put("Attributes", emptyList);
                    modifiedNbt.putFloat("AbsorptionAmount", 0.0F);
                    modifiedNbt.putInt("SleepingX", -999999);
                    modifiedNbt.putInt("SleepingY", -999999);
                    modifiedNbt.putInt("SleepingZ", -999999);
                    modifiedNbt.put("ActiveEffects", emptyList);
                    modifiedNbt.put("Brain", new CompoundTag());
                    modifiedNbt.remove("Team");
                    modifiedNbt.putBoolean("FallFlying", false);

                    if (livingTarget instanceof Mob mobTarget) {
                        modifiedNbt.putBoolean("CanPickUpLoot", false);
                        modifiedNbt.putBoolean("PersistenceRequired", false);
                        modifiedNbt.putBoolean("LeftHanded", true);
                        modifiedNbt.putBoolean("NoAI", true);
                        modifiedNbt.putLong("DeathLootTableSeed", 0L);
                        modifiedNbt.put("ArmorItems", emptyList);
                        modifiedNbt.put("HandItems", emptyList);
                        modifiedNbt.put("ArmorDropChances", emptyList);
                        modifiedNbt.put("HandDropChances", emptyList);
                        modifiedNbt.put("Leash", new CompoundTag());
                        modifiedNbt.put("DeathLootTable", new CompoundTag());
                        modifiedNbt.putString("forge:spawn_type", "");

                        mobTarget.load(modifiedNbt);

                    } else if (livingTarget instanceof Player playerTarget) {
                        modifiedNbt.put("Inventory", emptyList);
                        modifiedNbt.putInt("SelectedItemSlot", 0);
                        modifiedNbt.putFloat("XpP", 0.0F);
                        modifiedNbt.putInt("XpLevel", 0);
                        modifiedNbt.putInt("XpTotal", 0);
                        modifiedNbt.putInt("XpSeed", 0);
                        modifiedNbt.putInt("Score", 0);
                        modifiedNbt.remove("EnderItems");
                        modifiedNbt.remove("ShoulderEntityLeft");
                        modifiedNbt.remove("ShoulderEntityRight");

                        if (playerTarget instanceof ServerPlayer serverPlayerTarget) {
                            modifiedNbt.putInt("playerGameType", 0);
                            modifiedNbt.putInt("previousPlayerGameType", 0);
                            modifiedNbt.put("recipeBook", modifiedNbt);
                            modifiedNbt.remove("enteredNetherPosition");
                            modifiedNbt.put("RootVehicle", modifiedNbt);
                            modifiedNbt.putString("Dimension", "minecraft:overworld");
                            modifiedNbt.putInt("SpawnX", -999999);
                            modifiedNbt.putInt("SpawnY", -999999);
                            modifiedNbt.putInt("SpawnZ", -999999);
                            modifiedNbt.putBoolean("SpawnForced", false);
                            modifiedNbt.putFloat("SpawnAngle", -999999.0F);
                            modifiedNbt.remove("SpawnDimension");
                            modifiedNbt.remove("warden_spawn_tracker");
                            modifiedNbt.putBoolean("seenCredits", false);

                            serverPlayerTarget.load(modifiedNbt);
                        } else {
                            playerTarget.load(modifiedNbt);
                        }
                    } else {
                        livingTarget.load(modifiedNbt);
                    }
                } else {
                    target.load(modifiedNbt);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static boolean shouldNotAttack(Entity entity) {
        return entity == null;
    }

    public static void hurtWithAllDamageType(LivingEntity target, Entity attacker, float amount) {
        if (!(target.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Registry<DamageType> registry = serverLevel.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE);

        for (Holder.Reference<DamageType> holder : registry.holders()
                .toList()) {

            DamageSource source = new DamageSource(holder, attacker, attacker);

            target.hurt(source, amount);
            target.getCombatTracker().recordDamage(source, amount);
        }
    }
}
