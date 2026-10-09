package com.zenith.udl.cosmic.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class CosmicModelLoader implements IGeometryLoader<CosmicModelLoader.CosmicGeometry> {
    public static final CosmicModelLoader INSTANCE = new CosmicModelLoader(false);
    public static final CosmicModelLoader RAINBOW_INSTANCE = new CosmicModelLoader(true);
    private final boolean rainbow;

    private CosmicModelLoader(boolean rainbow) {
        this.rainbow = rainbow;
    }

    @Override
    public CosmicGeometry read(JsonObject modelContents, JsonDeserializationContext deserializationContext) throws JsonParseException {
        String geometryKey = this.rainbow ? "rainbow_cosmic" : "cosmic";
        JsonObject cosmicObj = modelContents.getAsJsonObject(geometryKey);
        if (cosmicObj == null) {
            throw new IllegalStateException("Missing '" + geometryKey + "' object.");
        } else {
            List<String> maskTexture = new ArrayList<>();
            if (cosmicObj.has("mask") && cosmicObj.get("mask").isJsonArray()) {
                JsonArray masks = cosmicObj.getAsJsonArray("mask");
                for (int i = 0; i < masks.size(); i++) {
                    maskTexture.add(masks.get(i).getAsString());
                }
            } else {
                maskTexture.add(GsonHelper.getAsString(cosmicObj, "mask"));
            }
            GlowEdgeModelLoader.GlowEdgeSettings glowEdgeSettings = modelContents.has("glow_edge")
                    ? GlowEdgeModelLoader.readSettings(GsonHelper.getAsJsonObject(modelContents, "glow_edge"))
                    : null;
            JsonObject clean = modelContents.deepCopy();
            clean.remove(geometryKey);
            clean.remove("glow_edge");
            clean.remove("loader");
            BlockModel baseModel = deserializationContext.deserialize(clean, BlockModel.class);
            return new CosmicGeometry(baseModel, maskTexture, this.rainbow, glowEdgeSettings);
        }
    }

    public static class CosmicGeometry implements IUnbakedGeometry<CosmicGeometry> {
        private final BlockModel baseModel;
        private final List<String> maskTextures;
        private final boolean rainbow;
        private final GlowEdgeModelLoader.GlowEdgeSettings glowEdgeSettings;

        public CosmicGeometry(final BlockModel baseModel, final List<String> maskTextures, boolean rainbow,
                              GlowEdgeModelLoader.GlowEdgeSettings glowEdgeSettings) {
            this.baseModel = baseModel;
            this.maskTextures = maskTextures;
            this.rainbow = rainbow;
            this.glowEdgeSettings = glowEdgeSettings;
        }

        @Override
        public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation) {
            BakedModel baseBakedModel = this.baseModel.bake(baker, this.baseModel, spriteGetter, modelState, modelLocation, true);
            List<ResourceLocation> textures = new ArrayList<>();
            this.maskTextures.forEach(mask -> textures.add(new ResourceLocation(mask)));
            return new CosmicBakeModel(baseBakedModel, textures, this.rainbow, this.glowEdgeSettings);
        }

        @Override
        public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context) {
            this.baseModel.resolveParents(modelGetter);
        }
    }
}