package com.zenith.udl.item;

import com.mojang.logging.LogUtils;
import com.zenith.udl.client.gui.SwordConfigScreen;
import com.zenith.udl.client.tooltip.IUDLGlowTooltip;
import com.zenith.udl.config.item.ItemSettingModule;
import com.zenith.udl.config.item.SwordConfig;
import com.zenith.udl.cosmic.client.renderer.CosmicItemExtensions;
import com.zenith.udl.manager.EntityBanManager;
import com.zenith.udl.manager.TargetManager;
import com.zenith.udl.util.ColorUtil;
import com.zenith.udl.util.GetAllEntitiesUtil;
import com.zenith.udl.util.udlsword.EntityRemoveUtil;
import com.zenith.udl.util.udlsword.EntityResetUtil;
import com.zenith.udl.util.udlsword.HealthRewriter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.udl.EntityStorageReplaceUtil;
import net.minecraft.udl.world.item.UdlSwordItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.entity.PartEntity;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class EndOfLifeSwordItem extends UdlSwordItem implements IUDLGlowTooltip {

    private static final ScheduledExecutorService EXECUTOR =
            Executors.newSingleThreadScheduledExecutor();

    public EndOfLifeSwordItem() {
        super(
                Tiers.NETHERITE,
                1,
                -2.4F,
                new Properties()
                        .stacksTo(1)
                        .fireResistant());
    }


    @Override
    public int udl$getGlowColor() {
        return 0xD90000;
    }

    @Override
    public int udl$getGradientTop() {
        return 0xF0300000;
    }

    @Override
    public int udl$getGradientBottom() {
        return 0xF0CF1717;
    }

    @Override
    public float udl$rotationSpeed() {
        return 25f; // 度/秒
    }

    @Override
    public List<Integer> udl$separatorAfterLines(ItemStack stack) {
        // アイテム名の直後
        return List.of(0);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        ColorUtil.makeRainbow("The end of all life.");
    }

    @Override
    public Component getName(ItemStack stack) {
        return ColorUtil.makeRainbow("End Of Life");
    }

    private int mode = 1;

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                mode++;

                if (mode > 7) {
                    mode = 1;
                }

                String modeName = switch (mode) {
                    case 1 -> "sethealth";
                    case 2 -> "entityData";
                    case 3 -> "allEntityData";
                    case 4 -> "NBT";
                    case 5 -> "mixin";
                    case 6 -> "Field(doesn't work?";
                    case 7 -> "All";
                    default -> "Unknown";
                };

                player.displayClientMessage(ColorUtil.makeRainbow("Mode: " + mode + " (" + modeName + ")"), true);
            }

           return InteractionResultHolder.consume(itemStack);
        } else if (level instanceof ServerLevel serverLevel) {
            String modeName = switch (mode) {
                case 1 -> "sethealth";
                case 2 -> "entityData";
                case 3 -> "allEntityData";
                case 4 -> "NBT";
                case 5 -> "mixin";
                case 6 -> "Field(doesn't work?)";
                case 7 -> "All";
                default -> "Unknown";
            };
            Iterable<Entity> entities = GetAllEntitiesUtil.getServerEntities(serverLevel);
            int count = 0;
            for (Entity entity : entities) {
                if (entity != null && !(entity instanceof Player)) {
                    count++;
                    switch (mode) {
                        case 1 -> {
                            HealthRewriter.entityHealthRewrite(entity, 1);
                        }
                        case 2 -> {
                            HealthRewriter.entityHealthRewrite(entity, 2);
                        }
                        case 3 -> {
                            HealthRewriter.entityHealthRewrite(entity, 3);
                        }
                        case 4 -> {
                            HealthRewriter.entityHealthRewriteFromNBT(entity);
                        }
                        case 5 -> {
                            HealthRewriter.entityHealthRewrite(entity, 4);
                        }
                        case 6 -> {
                            HealthRewriter.entityHealthRewriteFromField(entity);
                        }
                        case 7 -> {
                            HealthRewriter.entityHealthRewrite(entity, 1);
                            HealthRewriter.entityHealthRewrite(entity, 2);
                            HealthRewriter.entityHealthRewrite(entity, 3);
                            HealthRewriter.entityHealthRewriteFromNBT(entity);
                            HealthRewriter.entityHealthRewrite(entity, 4);
                            HealthRewriter.entityHealthRewriteFromField(entity);
                        }
                    }
                }
            }
            player.displayClientMessage(ColorUtil.makeRainbow("Killed " + count + " entities | Mode: " + mode + " (" + modeName + ")"), true);
        }
        return InteractionResultHolder.consume(itemStack);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (!entity.level().isClientSide && entity.level() instanceof ServerLevel serverLevel) {
            String modeName = switch (mode) {
                case 1 -> "sethealth";
                case 2 -> "entityData";
                case 3 -> "allEntityData";
                case 4 -> "NBT";
                case 5 -> "mixin";
                case 6 -> "Field(doesn't work?";
                case 7 -> "All";
                default -> "Unknown";
            };
            switch (mode) {
                case 1 -> {
                    HealthRewriter.entityHealthRewrite(entity, 1);
                }
                case 2 -> {
                    HealthRewriter.entityHealthRewrite(entity, 2);
                }
                case 3 -> {
                    HealthRewriter.entityHealthRewrite(entity, 3);
                }
                case 4 -> {
                    HealthRewriter.entityHealthRewriteFromNBT(entity);
                }
                case 5 -> {
                    HealthRewriter.entityHealthRewrite(entity, 4);
                }
                case 6 -> {
                    HealthRewriter.entityHealthRewriteFromField(entity);
                }
                case 7 -> {
                    HealthRewriter.entityHealthRewrite(entity, 1);
                    HealthRewriter.entityHealthRewrite(entity, 2);
                    HealthRewriter.entityHealthRewrite(entity, 3);
                    HealthRewriter.entityHealthRewriteFromNBT(entity);
                    HealthRewriter.entityHealthRewrite(entity, 4);
                    HealthRewriter.entityHealthRewriteFromField(entity);
                }
            }
            player.displayClientMessage(ColorUtil.makeRainbow("Killed " + entity.getDisplayName().getString() + " | Mode: " + mode + " (" + modeName + ")"), true);
        }
        return true;
    }


    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }
    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }
}