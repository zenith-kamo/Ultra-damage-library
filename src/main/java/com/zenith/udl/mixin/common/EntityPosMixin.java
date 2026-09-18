package com.zenith.udl.mixin.common;

import com.zenith.udl.manager.TargetManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityPosMixin {

    @Inject(method = "getX", at = @At("HEAD"), cancellable = true)
    private void onGetX(CallbackInfoReturnable<Double> cir) {
        Entity entity = (Entity) (Object) this;
        if (TargetManager.isTpTarget(entity)) {
            cir.setReturnValue(Double.MAX_VALUE);
        }
    }

    @Inject(method = "getY", at = @At("HEAD"), cancellable = true)
    private void onGetY(CallbackInfoReturnable<Double> cir) {
        Entity entity = (Entity) (Object) this;
        if (TargetManager.isTpTarget(entity)) {
            cir.setReturnValue(-Double.MIN_VALUE);
        }
    }

    @Inject(method = "getZ", at = @At("HEAD"), cancellable = true)
    private void onGetZ(CallbackInfoReturnable<Double> cir) {
        Entity entity = (Entity) (Object) this;
        if (TargetManager.isTpTarget(entity)) {
            cir.setReturnValue(Double.MAX_VALUE);
        }
    }
}
