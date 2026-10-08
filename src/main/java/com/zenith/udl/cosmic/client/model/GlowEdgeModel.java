package com.zenith.udl.cosmic.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.math.Transformation;
import com.zenith.udl.cosmic.api.client.model.PerspectiveModelState;
import com.zenith.udl.cosmic.client.shader.AvaritiaShaders;
import com.zenith.udl.cosmic.util.client.TransformUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.Collections;
import java.util.List;

public final class GlowEdgeModel implements BakedModel {
    private final BakedModel wrapped;
    private final int glowColor;
    private final float glowWidth;
    private final ItemOverrides overrideList;
    private final ModelState parentState;
    private BakedModel resolvedModel;

    public GlowEdgeModel(BakedModel wrapped, int glowColor, float glowWidth) {
        this.wrapped = wrapped;
        this.glowColor = glowColor;
        this.glowWidth = glowWidth;
        this.parentState = TransformUtils.stateFromItemTransforms(wrapped.getTransforms());
        this.resolvedModel = wrapped;
        this.overrideList = new ItemOverrides() {
            @Override
            public BakedModel resolve(@NotNull BakedModel originalModel, @NotNull ItemStack stack,
                                      ClientLevel world, LivingEntity entity, int seed) {
                BakedModel resolved = GlowEdgeModel.this.wrapped.getOverrides()
                        .resolve(GlowEdgeModel.this.wrapped, stack, world, entity, seed);
                if (resolved == null) {
                    throw new IllegalStateException("Item model override resolved to null for " + stack.getItem());
                }
                GlowEdgeModel.this.resolvedModel = resolved;
                return GlowEdgeModel.this;
            }
        };
    }

    public void renderItem(ItemStack stack, PoseStack poseStack, MultiBufferSource buffers,
                           int packedLight, int packedOverlay) {
        Minecraft minecraft = Minecraft.getInstance();
        BakedModel model = this.resolvedModel;
        for (BakedModel bakedModel : model.getRenderPasses(stack, true)) {
            for (net.minecraft.client.renderer.RenderType renderType : bakedModel.getRenderTypes(stack, true)) {
                minecraft.getItemRenderer().renderModelLists(bakedModel, stack, packedLight, packedOverlay,
                        poseStack, buffers.getBuffer(renderType));
            }
        }
        if (buffers instanceof MultiBufferSource.BufferSource bufferSource) {
            bufferSource.endBatch();
        }

        AvaritiaShaders.glowEdgeColor.set(this.glowColor);
        AvaritiaShaders.glowEdgeWidth.set(this.glowWidth);
        for (BakedModel bakedModel : model.getRenderPasses(stack, true)) {
            minecraft.getItemRenderer().renderModelLists(bakedModel, stack, packedLight, packedOverlay,
                    poseStack, buffers.getBuffer(AvaritiaShaders.GLOW_EDGE_RENDER_TYPE));
        }
    }

    @Override
    public boolean isCustomRenderer() {
        return true;
    }

    @Override
    public @NotNull BakedModel applyTransform(@NotNull net.minecraft.world.item.ItemDisplayContext context,
                                              @NotNull PoseStack poseStack, boolean leftFlip) {
        if (this.parentState instanceof PerspectiveModelState perspectiveState) {
            Transformation transform = perspectiveState.getTransform(context);
            Vector3f translation = transform.getTranslation();
            Vector3f scale = transform.getScale();
            poseStack.translate(translation.x(), translation.y(), translation.z());
            poseStack.mulPose(transform.getLeftRotation());
            poseStack.scale(scale.x(), scale.y(), scale.z());
            poseStack.mulPose(transform.getRightRotation());
            if (leftFlip) {
                poseStack.mulPose(Axis.YN.rotationDegrees(180.0F));
            }
            return this;
        }
        return BakedModel.super.applyTransform(context, poseStack, leftFlip);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(BlockState state, Direction side, @NotNull RandomSource random) {
        return Collections.emptyList();
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleIcon() {
        return this.wrapped.getParticleIcon();
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
        return this.wrapped.getParticleIcon(data);
    }

    @Override
    public @NotNull ItemOverrides getOverrides() {
        return this.overrideList;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return this.wrapped.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return this.wrapped.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return this.wrapped.usesBlockLight();
    }
}
