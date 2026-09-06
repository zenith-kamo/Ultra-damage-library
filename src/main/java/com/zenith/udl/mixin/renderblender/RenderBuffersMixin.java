package com.zenith.udl.mixin.renderblender;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.zenith.udl.renderblender.client.glint.GlintLayerManager;
import com.zenith.udl.renderblender.client.shader.RBRenderTypes;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(RenderBuffers.class)
public class RenderBuffersMixin {
    @Inject(method = "put", at = @At("HEAD"))
    private static void registerGlints(Object2ObjectLinkedOpenHashMap<RenderType, BufferBuilder> builderStorage, RenderType layer, CallbackInfo ci) {
        GlintLayerManager.registerAll(builderStorage);
        builderStorage.put(RBRenderTypes.COSMIC, new BufferBuilder(RBRenderTypes.COSMIC.bufferSize()));
        builderStorage.put(RBRenderTypes.ETERNAL, new BufferBuilder(RBRenderTypes.ETERNAL.bufferSize()));
        builderStorage.put(RBRenderTypes.HELL, new BufferBuilder(RBRenderTypes.HELL.bufferSize()));
        builderStorage.put(RBRenderTypes.UNSTABLE, new BufferBuilder(RBRenderTypes.UNSTABLE.bufferSize()));
        builderStorage.put(RBRenderTypes.GLOWING_OUTLINE, new BufferBuilder(RBRenderTypes.GLOWING_OUTLINE.bufferSize()));
    }
}
