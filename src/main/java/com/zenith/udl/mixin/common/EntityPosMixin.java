package com.zenith.udl.mixin.common;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityPosMixin {

    // getX() メソッドの返り値を上書き
    @Inject(method = "getX", at = @At("HEAD"), cancellable = true)
    private void onGetX(CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(0.0D);
    }

    // getY() や getZ() も同様に記述可能
    @Inject(method = "getY", at = @At("HEAD"), cancellable = true)
    private void onGetY(CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(20.0D);
    }

    @Inject(method = "getZ", at = @At("HEAD"), cancellable = true)
    private void onGetZ(CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(0.0D);
    }
}
