package com.zenith.udl.mixin.sword;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.zenith.udl.manager.TargetManager;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
@OnlyIn(Dist.CLIENT)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {

    // モデルを取得するためにShadowで定義（Redirectで使用）
    @Shadow protected M model;

    /**
     * 【方法1：PoseStack】横倒しにする回転処理（前回の実装）
     */
    @Inject(method = "setupRotations", at = @At("TAIL"))
    private void applyCustomDeathPose(T pEntityLiving, PoseStack pPoseStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks, CallbackInfo ci) {
        // 対象かつ、バニラの死亡処理中でない場合、横倒しに回転
        if (TargetManager.isPoseTarget(pEntityLiving) && pEntityLiving.deathTime <= 0) {
            pPoseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
        }
    }

    /**
     * 【赤色フラッシュ】モデル描画時にオーバーレイを赤に強制する
     * Redirect を使用して、モデルの renderToBuffer 呼び出し時の引数を差し替えます。
     */
    @Redirect(
            method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"
            )
    )
    private void forcedRedOverlayIfTarget(
            EntityModel<?> model, PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha, // 元の引数
            T entity, float entityYaw, float partialTicks, PoseStack poseStack2, MultiBufferSource bufferSource, int packedLight2 // renderメソッドの引数
    ) {
        // ローカル変数の `packedOverlay` をシャドウイングして再定義
        int finalOverlay = packedOverlay;

        // 対象判定が真の場合のみ、赤色オーバーレイを強制
        if (TargetManager.isPoseTarget(entity)) {
            // パッキングされたオーバーレイ座標を作成。
            // 0 (U: 通常/白) ではなく 10 (U: 赤色) に設定
            finalOverlay = OverlayTexture.pack(OverlayTexture.u(0), OverlayTexture.v(true));
        }

        // 差し替えた overlay (finalOverlay) を使って元のメソッドを呼び出す
        this.model.renderToBuffer(poseStack, buffer, packedLight, finalOverlay, red, green, blue, alpha);
    }
}