package com.zenith.udl.item;

import com.mojang.logging.LogUtils;
import com.zenith.udl.client.gui.SwordConfigScreen;
import com.zenith.udl.config.item.ItemSettingModule;
import com.zenith.udl.config.item.SwordConfig;
import com.zenith.udl.cosmic.client.renderer.CosmicItemExtensions;
import com.zenith.udl.manager.EntityBanManager;
import com.zenith.udl.manager.TargetManager;
import com.zenith.udl.util.EntityRemoveUtil;
import net.minecraft.udl.EntityStorageReplaceUtil;
import com.zenith.udl.util.GetAllEntitiesUtil;
import com.zenith.udl.util.udlsword.EntityResetUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.udl.world.item.UdlSwordItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.slf4j.Logger;

import java.util.function.Consumer;

public class UltraDamageLibrarySwordItem extends UdlSwordItem {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean HIDDEN_ENTITY_ALL = false;

    public UltraDamageLibrarySwordItem() {
        super(
                Tiers.NETHERITE,
                1,
                -2.4F,
                new Properties()
                        .stacksTo(1)
                        .fireResistant());
    }

    public static boolean isForceHiddenEntity() {
        return HIDDEN_ENTITY_ALL;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        player.startUsingItem(hand);

        if (player.isShiftKeyDown()) {
            if (level.isClientSide()) {
                Minecraft.getInstance().setScreen(new SwordConfigScreen(itemStack));
            }
            return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
        }
        if (level instanceof ServerLevel serverLevel) {
            // エンティティが存在するかに関わらず非表示にする
            if (SwordConfig.isFeatureEnabled(itemStack, ItemSettingModule.HIDDEN_ENTITY_ALL)) {
                HIDDEN_ENTITY_ALL = !HIDDEN_ENTITY_ALL;
                player.displayClientMessage(Component.literal("HIDDEN_ENTITY_ALL: ")
                        .append(String.valueOf(HIDDEN_ENTITY_ALL)), true);
            }

            Iterable<Entity> entities = GetAllEntitiesUtil.getServerEntities(serverLevel);
            for (Entity entity : entities) {
                if (entity != null && (entity != player)) {
                    if (SwordConfig.isFeatureEnabled(itemStack, ItemSettingModule.ENTITY_BAN))
                        EntityBanManager.addBan(entity.getClass().getName());
                    if (SwordConfig.isFeatureEnabled(itemStack, ItemSettingModule.HIDDEN_ENTITY))
                        TargetManager.addHiddenTarget(entity);
                    if (SwordConfig.isFeatureEnabled(itemStack, ItemSettingModule.NORMAL_ATTACK))
                        EntityRemoveUtil.removeEntity(entity, serverLevel);

                    if (SwordConfig.isFeatureEnabled(itemStack, ItemSettingModule.HIDDEN_ENTITY))
                        TargetManager.addHiddenTarget(entity);
                }
            }
        }

        EntityStorageReplaceUtil.hogehoge(level, itemStack, player);
        if (level.isClientSide()) {
            if (SwordConfig.isFeatureEnabled(itemStack, ItemSettingModule.DELETE_ENTITY_SAVE_DATA)) {
                boolean started = EntityResetUtil.getInstance().startResetSequence();
                if (!started) {
                    player.sendSystemMessage(Component.literal("シングルプレイ限定、または実行中です"));
                }
            }
        }
        return InteractionResultHolder.consume(itemStack);
    }


    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BLOCK;
    }
    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(CosmicItemExtensions.INSTANCE);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        // スロット自体が切り替わっていない場合（同じアイテムを手に持ったまま右クリックした場合）は
        // 再装備アニメーション（下から持ち上げる動き）をキャンセルする
        if (!slotChanged && ItemStack.isSameItem(oldStack, newStack)) {
            return false;
        }
        return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
    }
}