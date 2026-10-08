package com.zenith.udl.cosmic.client.shader;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.zenith.udl.Udl;
import com.zenith.udl.cosmic.api.client.shader.CCShaderInstance;
import com.zenith.udl.cosmic.api.client.shader.CCUniform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = Udl.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class AvaritiaShaders {
    private static class RenderStateShardAccess extends RenderStateShard {
        private static final DepthTestStateShard EQUAL_DEPTH_TEST = RenderStateShard.EQUAL_DEPTH_TEST;
        private static final LightmapStateShard LIGHT_MAP = RenderStateShard.LIGHTMAP;
        private static final TransparencyStateShard TRANSLUCENT_TRANSPARENCY = RenderStateShard.TRANSLUCENT_TRANSPARENCY;
        private static final TextureStateShard BLOCK_SHEET_MIPPED = RenderStateShard.BLOCK_SHEET_MIPPED;

        private RenderStateShardAccess(String pName, Runnable pSetupState, Runnable pClearState) {
            super(pName, pSetupState, pClearState);
        }
    }

    public static final float[] COSMIC_UVS = new float[40];
    public static boolean inventoryRender = false;
    public static int renderTime;
    public static float tick;
    public static float renderFrame;
    public static CCShaderInstance cosmicShader;
    public static CCUniform cosmicTime;
    public static CCUniform cosmicYaw;
    public static CCUniform cosmicPitch;
    public static CCUniform cosmicExternalScale;
    public static CCUniform cosmicOpacity;
    public static CCUniform cosmicUVs;
    public static CCShaderInstance rainbowCosmicShader;
    public static CCUniform rainbowCosmicTime;
    public static CCUniform rainbowCosmicYaw;
    public static CCUniform rainbowCosmicPitch;
    public static CCUniform rainbowCosmicExternalScale;
    public static CCUniform rainbowCosmicOpacity;
    public static CCUniform rainbowCosmicUVs;
    public static CCShaderInstance glowEdgeShader;
    public static CCUniform glowEdgeColor;
    public static CCUniform glowEdgeWidth;
    public static final RenderType COSMIC_RENDER_TYPE = RenderType.create(Udl.MODID + ":cosmic", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 2097152, true, false, RenderType.CompositeState.builder().setShaderState(new RenderStateShard.ShaderStateShard(() -> cosmicShader)).setDepthTestState(RenderStateShardAccess.EQUAL_DEPTH_TEST).setLightmapState(RenderStateShardAccess.LIGHT_MAP).setTransparencyState(RenderStateShardAccess.TRANSLUCENT_TRANSPARENCY).setTextureState(RenderStateShardAccess.BLOCK_SHEET_MIPPED).createCompositeState(true));
    public static final RenderType RAINBOW_COSMIC_RENDER_TYPE = RenderType.create(Udl.MODID + ":rainbow_cosmic", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 2097152, true, false, RenderType.CompositeState.builder().setShaderState(new RenderStateShard.ShaderStateShard(() -> rainbowCosmicShader)).setDepthTestState(RenderStateShardAccess.EQUAL_DEPTH_TEST).setLightmapState(RenderStateShardAccess.LIGHT_MAP).setTransparencyState(RenderStateShardAccess.TRANSLUCENT_TRANSPARENCY).setTextureState(RenderStateShardAccess.BLOCK_SHEET_MIPPED).createCompositeState(true));
    public static final RenderType GLOW_EDGE_RENDER_TYPE = RenderType.create(Udl.MODID + ":glow_edge", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 2097152, true, false, RenderType.CompositeState.builder().setShaderState(new RenderStateShard.ShaderStateShard(() -> glowEdgeShader)).setDepthTestState(RenderStateShardAccess.EQUAL_DEPTH_TEST).setLightmapState(RenderStateShardAccess.LIGHT_MAP).setTransparencyState(RenderStateShardAccess.TRANSLUCENT_TRANSPARENCY).setTextureState(RenderStateShardAccess.BLOCK_SHEET_MIPPED).createCompositeState(true));

    public static void onRegisterShaders(RegisterShadersEvent event) {
        event.registerShader(CCShaderInstance.create(event.getResourceProvider(), new ResourceLocation(Udl.MODID, "cosmic"), DefaultVertexFormat.BLOCK), e -> {
            cosmicShader = (CCShaderInstance) e;
            cosmicTime = Objects.requireNonNull(cosmicShader.getUniform("time"));
            cosmicYaw = Objects.requireNonNull(cosmicShader.getUniform("yaw"));
            cosmicPitch = Objects.requireNonNull(cosmicShader.getUniform("pitch"));
            cosmicExternalScale = Objects.requireNonNull(cosmicShader.getUniform("externalScale"));
            cosmicOpacity = Objects.requireNonNull(cosmicShader.getUniform("opacity"));
            cosmicUVs = Objects.requireNonNull(cosmicShader.getUniform("cosmicuvs"));
            cosmicTime.set((float) renderTime + renderFrame);
            cosmicShader.onApply(() -> cosmicTime.set((float) renderTime + renderFrame));
        });
        event.registerShader(CCShaderInstance.create(event.getResourceProvider(), new ResourceLocation(Udl.MODID, "rainbow_cosmic"), DefaultVertexFormat.BLOCK), e -> {
            rainbowCosmicShader = (CCShaderInstance) e;
            rainbowCosmicTime = Objects.requireNonNull(rainbowCosmicShader.getUniform("time"));
            rainbowCosmicYaw = Objects.requireNonNull(rainbowCosmicShader.getUniform("yaw"));
            rainbowCosmicPitch = Objects.requireNonNull(rainbowCosmicShader.getUniform("pitch"));
            rainbowCosmicExternalScale = Objects.requireNonNull(rainbowCosmicShader.getUniform("externalScale"));
            rainbowCosmicOpacity = Objects.requireNonNull(rainbowCosmicShader.getUniform("opacity"));
            rainbowCosmicUVs = Objects.requireNonNull(rainbowCosmicShader.getUniform("cosmicuvs"));
            rainbowCosmicTime.set((float) renderTime + renderFrame);
            rainbowCosmicShader.onApply(() -> rainbowCosmicTime.set((float) renderTime + renderFrame));
        });
        event.registerShader(CCShaderInstance.create(event.getResourceProvider(), new ResourceLocation(Udl.MODID, "glow_edge"), DefaultVertexFormat.BLOCK), e -> {
            glowEdgeShader = (CCShaderInstance) e;
            glowEdgeColor = Objects.requireNonNull(glowEdgeShader.getUniform("glowColor"));
            glowEdgeWidth = Objects.requireNonNull(glowEdgeShader.getUniform("glowWidth"));
        });
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (!Minecraft.getInstance().isPaused() && event.phase == TickEvent.Phase.END) {
            ++renderTime;
            tick += 1F;
            if (tick >= 720.0f) {
                tick = 0.0F;
            }
        }
    }

    @SubscribeEvent
    public static void renderTick(TickEvent.RenderTickEvent event) {
        if (!Minecraft.getInstance().isPaused() && event.phase == TickEvent.Phase.START) {
            renderFrame = event.renderTickTime;
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void drawScreenPre(final ScreenEvent.Render.Pre e) {
        AvaritiaShaders.inventoryRender = true;
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void drawScreenPost(final ScreenEvent.Render.Post e) {
        AvaritiaShaders.inventoryRender = false;
    }
}