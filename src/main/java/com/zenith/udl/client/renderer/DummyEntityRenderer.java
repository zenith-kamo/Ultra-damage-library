package com.zenith.udl.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/**
 * 何も描画しない空のダミーレンダラー。
 */
public class DummyEntityRenderer<T extends Entity> extends EntityRenderer<T> {

    public DummyEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // 描画処理を行わない
    }

    @Override
    public boolean shouldRender(T entity, Frustum frustum, double x, double y, double z) {
        return false; // 描画判定を無条件で false にする
    }

    @Override
    protected boolean shouldShowName(T entity) {
        return false; // ネームタグ非表示
    }

    @Override
    protected void renderNameTag(T entity, Component component, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // ネームタグ描画を行わない
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return null;
    }
}