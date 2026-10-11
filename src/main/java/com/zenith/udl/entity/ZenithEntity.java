
package com.zenith.udl.entity;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;

public class ZenithEntity extends PathfinderMob {

    public ZenithEntity(EntityType<? extends ZenithEntity> entityType, Level level) {
        super(entityType, level);
        this.setPersistenceRequired();
        this.setNoAi(true);
        this.setInvulnerable(true);
        this.setNoGravity(true);
    }

    @Override
    protected void registerGoals() {
        // AIなし
    }
    @Override
    public void tick() {
        super.tick();

        // ノックバックなどによる移動も抑制
        this.setDeltaMovement(0.0D, 0.0D, 0.0D);
        this.hasImpulse = false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // ダメージを受けない
        return false;
    }

    @Override
    public void knockback(double strength, double x, double z) {
        // ノックバック無効
    }

    @Override
    public boolean isPushable() {
        return false;
    }
}
