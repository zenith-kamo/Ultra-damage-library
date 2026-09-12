package com.zenith.udl.mixin.sword;

import com.zenith.udl.manager.TargetManager;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.zenith.udl.item.UltraDamageLibrarySwordItem.isForceHiddenEntity;

@Mixin(value = EntityRenderDispatcher.class, priority = Integer.MAX_VALUE)
public class EntityRenderDispatcherMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private <E extends Entity> void cancelNonPlayerEntities(E entity, double x, double y, double z, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        if (!(entity instanceof Player)) {
            if (isForceHiddenEntity()) ci.cancel();
            if (TargetManager.isHiddenTarget(entity)) ci.cancel();
        }
    }
}
