package com.zenith.udl.handler;

import com.zenith.udl.init.ModItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "udl", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class HeavenSkyElytraFlightHandler {
    private static final double MIN_FLIGHT_SPEED = 1.0D;
    private static final double FLIGHT_BOOST_PER_TICK = 0.04D;
    private static final double DIRECTION_EPSILON = 1.0E-6D;

    private HeavenSkyElytraFlightHandler() {}

    @SubscribeEvent
    public static void keepMinimumFlightSpeed(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Player player = event.player;
        if (player.level().isClientSide
                || !player.isFallFlying()
                || !player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.HEAVEN_SKY_ELYTRA.get())) {
            return;
        }

        Vec3 motion = player.getDeltaMovement();
        double speedSqr = motion.lengthSqr();
        if (speedSqr >= MIN_FLIGHT_SPEED * MIN_FLIGHT_SPEED) {
            return;
        }

        Vec3 look = player.getLookAngle();
        Vec3 horizontalLook = new Vec3(look.x, 0.0D, look.z);
        if (horizontalLook.lengthSqr() < DIRECTION_EPSILON) {
            double yaw = Math.toRadians(player.getYRot());
            horizontalLook = new Vec3(-Math.sin(yaw), 0.0D, Math.cos(yaw));
        } else {
            horizontalLook = horizontalLook.normalize();
        }

        double verticalBoost = Math.max(-0.15D, Math.min(0.15D, look.y));
        Vec3 boostDirection = new Vec3(horizontalLook.x, verticalBoost, horizontalLook.z).normalize();
        double speed = Math.sqrt(speedSqr);
        double boostAmount = Math.min(FLIGHT_BOOST_PER_TICK, MIN_FLIGHT_SPEED - speed);
        player.setDeltaMovement(motion.add(boostDirection.scale(boostAmount)));
        player.hurtMarked = true;
    }
}
