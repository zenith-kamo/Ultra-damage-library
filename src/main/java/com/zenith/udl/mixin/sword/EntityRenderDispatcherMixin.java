package com.zenith.udl.mixin.sword;

import com.zenith.udl.client.renderer.DummyEntityRenderer;
import com.zenith.udl.manager.TargetManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.zenith.udl.item.UltraDamageLibrarySwordItem.isForceHiddenEntity;

@Mixin(value = EntityRenderDispatcher.class, priority = Integer.MAX_VALUE)
public class EntityRenderDispatcherMixin {
    @Unique
    private DummyEntityRenderer<Entity> udl$dummyRenderer;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private <E extends Entity> void cancelNonPlayerEntities(E entity, double x, double y, double z, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        if (!(entity instanceof Player)) {
            if (isForceHiddenEntity()) ci.cancel();
            if (TargetManager.isHiddenTarget(entity)) ci.cancel();
        }
    }

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private <E extends Entity> void onShouldRender(E pEntity, Frustum pFrustum, double pCamX, double pCamY, double pCamZ, CallbackInfoReturnable<Boolean> cir) {
        if (!(pEntity instanceof Player)) {
            if (isForceHiddenEntity()) cir.setReturnValue(false);
            if (TargetManager.isHiddenTarget(pEntity)) cir.setReturnValue(false);
        }
    }

    @Inject(method = "getRenderer", at = @At("HEAD"), cancellable = true)
    private <T extends Entity> void onGetRenderer(T entity, CallbackInfoReturnable<EntityRenderer<? super T>> cir) {
        if (!(entity instanceof Player)) {
            if (this.udl$dummyRenderer == null) {
                Minecraft mc = Minecraft.getInstance();
                EntityRendererProvider.Context context = new EntityRendererProvider.Context(
                        (EntityRenderDispatcher) (Object) this,
                        mc.getItemRenderer(),
                        mc.getBlockRenderer(),
                        mc.getEntityRenderDispatcher().getItemInHandRenderer(),
                        mc.getResourceManager(),
                        mc.getEntityModels(),
                        mc.font
                );
                this.udl$dummyRenderer = new DummyEntityRenderer<>(context);
            }

            if (isForceHiddenEntity()) cir.setReturnValue((EntityRenderer<? super T>) this.udl$dummyRenderer);
            if (TargetManager.isHiddenTarget(entity)) cir.setReturnValue((EntityRenderer<? super T>) this.udl$dummyRenderer);
        }
    }
}
