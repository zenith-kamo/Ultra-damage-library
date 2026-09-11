package com.zenith.udl.item;

import com.mojang.logging.LogUtils;
import com.zenith.udl.Udl;
import com.zenith.udl.client.gui.SwordConfigScreen;
import com.zenith.udl.config.item.ItemSettingModule;
import com.zenith.udl.config.item.SwordConfig;
import com.zenith.udl.manager.EntityBanManager;
import com.zenith.udl.util.EntityRemoveUtil;
import net.minecraft.udl.EntityStorageReplaceUtil;
import com.zenith.udl.util.GetAllEntitiesUtil;
import com.zenith.udl.util.udlsword.EntityResetUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;


/*
 * いったんPickaxeItemに変更。SwordItemだけ処理を防ぐゴミmodがあるので....
 */


public class UltraDamageLibrarySwordItem extends SwordItem {

    private static final Logger LOGGER = LogUtils.getLogger();

    public UltraDamageLibrarySwordItem() {
        super(
                Tiers.NETHERITE,
                1,
                -2.4F,
                new Properties()
                        .stacksTo(1)
                        .fireResistant());
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
            if (SwordConfig.isFeatureEnabled(itemStack, ItemSettingModule.ENTITY_BAN)) {
                Iterable<Entity> entities = GetAllEntitiesUtil.getServerEntities(serverLevel);

                for (Entity entity : entities) {
                    if (entity != null) {
                        EntityBanManager.addBan(entity.getClass().getName());
                    }
                }
            }
            for (Entity entity : GetAllEntitiesUtil.getServerEntities(serverLevel)) {
                if (entity != player) {
                    EntityRemoveUtil.removeEntity(entity, serverLevel);
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
        return UseAnim.BLOCK; // 盾や旧バージョンの剣のガードポーズ
    }
    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000; // 72000 ticks (約1時間)
    }
}