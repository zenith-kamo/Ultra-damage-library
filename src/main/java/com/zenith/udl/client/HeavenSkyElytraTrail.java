package com.zenith.udl.client;

import com.zenith.udl.init.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

@Mod.EventBusSubscriber(
        modid = "udl",
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT
)
public final class HeavenSkyElytraTrail {

    private static final DustParticleOptions BLUE =
            new DustParticleOptions(new Vector3f(0.08F, 0.38F, 1.0F), 1.25F);

    private static final DustParticleOptions CYAN =
            new DustParticleOptions(new Vector3f(0.12F, 0.85F, 1.0F), 1.0F);

    private static final DustParticleOptions PALE_BLUE =
            new DustParticleOptions(new Vector3f(0.58F, 0.88F, 1.0F), 0.85F);

    private static final DustParticleOptions WHITE =
            new DustParticleOptions(new Vector3f(1.0F, 1.0F, 1.0F), 0.9F);

    private static final DustParticleOptions DEEP_BLUE =
            new DustParticleOptions(new Vector3f(0.025F, 0.12F, 0.8F), 1.1F);

    private HeavenSkyElytraTrail() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        for (Player player : minecraft.level.players()) {
            if (!player.isFallFlying()
                    || !player.getItemBySlot(EquipmentSlot.CHEST)
                    .is(ModItems.HEAVEN_SKY_ELYTRA.get())) {
                continue;
            }

            Vec3 motion = player.getDeltaMovement();
            double speed = motion.length();

            if (speed < 0.1D) {
                continue;
            }

            // 速度に応じて演出を強化
            double intensity = Math.min(speed / 1.5D, 2.0D);

            // 左右の光翼
            spawnWingTrail(minecraft, player, motion, -1.0D, intensity);
            spawnWingTrail(minecraft, player, motion, 1.0D, intensity);

            // 背後に流れる蒼い残光
            int trailCount = 2 + (int) (intensity * 2.0D);

            for (int i = 0; i < trailCount; i++) {
                double back = 0.25D + player.getRandom().nextDouble() * 0.8D;
                double spread = 0.35D;

                Vec3 position = player.position()
                        .add(0.0D, 0.8D, 0.0D)
                        .add(-motion.x * back, -motion.y * back, -motion.z * back)
                        .add(
                                (player.getRandom().nextDouble() - 0.5D) * spread,
                                (player.getRandom().nextDouble() - 0.5D) * spread,
                                (player.getRandom().nextDouble() - 0.5D) * spread
                        );

                DustParticleOptions color = switch (player.getRandom().nextInt(4)) {
                    case 0 -> DEEP_BLUE;
                    case 1 -> BLUE;
                    case 2 -> CYAN;
                    default -> PALE_BLUE;
                };

                minecraft.level.addParticle(
                        color,
                        position.x, position.y, position.z,
                        -motion.x * 0.12D,
                        -motion.y * 0.12D,
                        -motion.z * 0.12D
                );
            }

            // 翼の間に浮かぶ光の粒子
            if (player.getRandom().nextFloat() < 0.7F) {
                Vec3 position = player.position()
                        .add(0.0D, 0.75D, 0.0D)
                        .add(-motion.x * 0.3D, -motion.y * 0.3D, -motion.z * 0.3D);

                minecraft.level.addParticle(
                        ParticleTypes.END_ROD,
                        position.x, position.y, position.z,
                        -motion.x * 0.08D,
                        -motion.y * 0.08D,
                        -motion.z * 0.08D
                );
            }

            // 高速飛行時の閃光
            if (intensity > 0.8D
                    && player.getRandom().nextFloat() < 0.25F * (float) intensity) {

                Vec3 position = player.position()
                        .add(
                                (player.getRandom().nextDouble() - 0.5D) * 1.4D,
                                0.3D + player.getRandom().nextDouble() * 1.4D,
                                (player.getRandom().nextDouble() - 0.5D) * 1.4D
                        );

                minecraft.level.addParticle(
                        ParticleTypes.ELECTRIC_SPARK,
                        position.x, position.y, position.z,
                        -motion.x * 0.15D,
                        -motion.y * 0.15D,
                        -motion.z * 0.15D
                );
            }
        }
    }

    private static void spawnWingTrail(
            Minecraft minecraft,
            Player player,
            Vec3 motion,
            double side,
            double intensity
    ) {
        double yaw = Math.toRadians(player.getYRot());

        double wingX = Math.cos(yaw) * side * 0.65D;
        double wingZ = Math.sin(yaw) * side * 0.65D;

        // 翼の基準位置
        Vec3 wingPosition = player.position()
                .add(wingX, 1.0D, wingZ)
                .add(-motion.x * 0.18D, -motion.y * 0.18D, -motion.z * 0.18D);

        // 翼の主軌跡
        int count = 2 + (int) intensity;

        for (int i = 0; i < count; i++) {
            double jitterX = (player.getRandom().nextDouble() - 0.5D) * 0.22D;
            double jitterY = (player.getRandom().nextDouble() - 0.5D) * 0.28D;
            double jitterZ = (player.getRandom().nextDouble() - 0.5D) * 0.22D;

            Vec3 position = wingPosition.add(jitterX, jitterY, jitterZ);

            DustParticleOptions color;

            if (player.getRandom().nextInt(8) == 0) {
                color = WHITE;
            } else if (player.getRandom().nextBoolean()) {
                color = BLUE;
            } else {
                color = CYAN;
            }

            minecraft.level.addParticle(
                    color,
                    position.x, position.y, position.z,
                    -motion.x * 0.18D + jitterX * 0.1D,
                    -motion.y * 0.18D + 0.025D,
                    -motion.z * 0.18D + jitterZ * 0.1D
            );
        }

        // 翼の輪郭を走る明るい粒子
        if (player.getRandom().nextFloat() < 0.85F) {
            double offset = (player.getRandom().nextDouble() - 0.5D) * 0.35D;

            Vec3 position = wingPosition.add(
                    Math.cos(yaw) * offset,
                    0.1D + player.getRandom().nextDouble() * 0.25D,
                    Math.sin(yaw) * offset
            );

            minecraft.level.addParticle(
                    WHITE,
                    position.x, position.y, position.z,
                    -motion.x * 0.1D,
                    -motion.y * 0.1D,
                    -motion.z * 0.1D
            );
        }

        // 翼の先端から飛び散る流星のような光
        if (player.getRandom().nextFloat() < 0.35F * (float) intensity) {
            Vec3 position = wingPosition.add(
                    wingX * 0.35D,
                    (player.getRandom().nextDouble() - 0.5D) * 0.3D,
                    wingZ * 0.35D
            );

            minecraft.level.addParticle(
                    ParticleTypes.END_ROD,
                    position.x, position.y, position.z,
                    -motion.x * 0.22D + (player.getRandom().nextDouble() - 0.5D) * 0.08D,
                    -motion.y * 0.22D,
                    -motion.z * 0.22D + (player.getRandom().nextDouble() - 0.5D) * 0.08D
            );
        }
    }
}