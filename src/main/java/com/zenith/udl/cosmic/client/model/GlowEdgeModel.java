package com.zenith.udl.cosmic.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zenith.udl.cosmic.client.shader.AvaritiaShaders;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GlowEdgeModel implements BakedModel {
    private static final float[][] GLOW_DIRECTIONS = {
            {1.0F, 1.0F, 1.0F},
            {-1.0F, 1.0F, 1.0F},
            {1.0F, -1.0F, 1.0F},
            {1.0F, 1.0F, -1.0F},
            {-1.0F, -1.0F, 1.0F},
            {-1.0F, 1.0F, -1.0F},
            {1.0F, -1.0F, -1.0F},
            {-1.0F, -1.0F, -1.0F}
    };
    private static final float GLOW_WIDTH_SCALE = 0.008F;

    private final BakedModel wrapped;
    private final int[] glowColors;
    private final float glowWidth;
    private final float glowSpeed;
    private final float glowCycleWidth;
    private final int glowAnimation;
    private final ItemOverrides overrideList;
    private BakedModel resolvedModel;

    public GlowEdgeModel(BakedModel wrapped, int[] glowColors, float glowWidth, float glowSpeed,
                         float glowCycleWidth, int glowAnimation) {
        this.wrapped = wrapped;
        this.glowColors = glowColors.clone();
        this.glowWidth = glowWidth;
        this.glowSpeed = glowSpeed;
        this.glowCycleWidth = glowCycleWidth;
        this.glowAnimation = glowAnimation;
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
        if (!(buffers instanceof MultiBufferSource.BufferSource bufferSource)) {
            throw new IllegalStateException("Glow edge rendering requires a buffered item render source.");
        }

        bufferSource.endBatch();
        setGlowShaderParameters();
        List<BakedModel> renderPasses = model.getRenderPasses(stack, true);
        float offset = this.glowWidth * GLOW_WIDTH_SCALE;
        for (float[] direction : GLOW_DIRECTIONS) {
            poseStack.pushPose();
            try {
                poseStack.translate(direction[0] * offset, direction[1] * offset, direction[2] * offset);
                for (BakedModel bakedModel : renderPasses) {
                    BakedModel glowModel = new GlowEdgePassModel(
                            bakedModel, direction[0], direction[1], direction[2]);
                    minecraft.getItemRenderer().renderModelLists(glowModel, stack, packedLight, packedOverlay,
                            poseStack, bufferSource.getBuffer(AvaritiaShaders.GLOW_EDGE_RENDER_TYPE));
                }
            } finally {
                poseStack.popPose();
            }
        }
        bufferSource.endBatch(AvaritiaShaders.GLOW_EDGE_RENDER_TYPE);

        for (BakedModel bakedModel : renderPasses) {
            for (net.minecraft.client.renderer.RenderType renderType : bakedModel.getRenderTypes(stack, true)) {
                minecraft.getItemRenderer().renderModelLists(bakedModel, stack, packedLight, packedOverlay,
                        poseStack, buffers.getBuffer(renderType));
            }
        }
    }

    private void setGlowShaderParameters() {
        int firstColor = this.glowColors[0];
        int secondColor = this.glowColors[Math.min(1, this.glowColors.length - 1)];
        int thirdColor = this.glowColors[Math.min(2, this.glowColors.length - 1)];
        AvaritiaShaders.glowEdgeColor1.set(
                ((firstColor >> 16) & 0xFF) / 255.0F,
                ((firstColor >> 8) & 0xFF) / 255.0F,
                (firstColor & 0xFF) / 255.0F);
        AvaritiaShaders.glowEdgeColor2.set(
                ((secondColor >> 16) & 0xFF) / 255.0F,
                ((secondColor >> 8) & 0xFF) / 255.0F,
                (secondColor & 0xFF) / 255.0F);
        AvaritiaShaders.glowEdgeColor3.set(
                ((thirdColor >> 16) & 0xFF) / 255.0F,
                ((thirdColor >> 8) & 0xFF) / 255.0F,
                (thirdColor & 0xFF) / 255.0F);
        AvaritiaShaders.glowEdgeColorCount.set(this.glowColors.length);
        double cycle = (AvaritiaShaders.renderTime + (double) AvaritiaShaders.renderFrame)
                * this.glowSpeed / 20.0D;
        AvaritiaShaders.glowEdgeTime.set((float) (cycle - Math.floor(cycle)));
        AvaritiaShaders.glowEdgeAnimation.set(this.glowAnimation);
        AvaritiaShaders.glowEdgeCycleWidth.set(this.glowCycleWidth);
    }

    private static final class GlowEdgePassModel extends BakedModelWrapper<BakedModel> {
        private final float directionX;
        private final float directionY;
        private final float directionZ;

        private GlowEdgePassModel(BakedModel model, float directionX, float directionY, float directionZ) {
            super(model);
            this.directionX = directionX;
            this.directionY = directionY;
            this.directionZ = directionZ;
        }

        @Override
        public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource random) {
            List<BakedQuad> sourceQuads = this.originalModel.getQuads(state, side, random);
            List<BakedQuad> visibleQuads = new ArrayList<>(sourceQuads.size());
            for (BakedQuad quad : sourceQuads) {
                Direction normal = quad.getDirection();
                float dot = this.directionX * normal.getStepX()
                        + this.directionY * normal.getStepY()
                        + this.directionZ * normal.getStepZ();
                if (dot > 0.0F) {
                    visibleQuads.add(quad);
                }
            }
            return visibleQuads;
        }
    }

    @Override
    public boolean isCustomRenderer() {
        return true;
    }

    @Override
    public @NotNull BakedModel applyTransform(@NotNull net.minecraft.world.item.ItemDisplayContext context,
                                              @NotNull PoseStack poseStack, boolean leftFlip) {
        this.wrapped.applyTransform(context, poseStack, leftFlip);
        return this;
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
