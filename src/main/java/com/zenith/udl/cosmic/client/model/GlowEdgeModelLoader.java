package com.zenith.udl.cosmic.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import java.util.function.Function;

public final class GlowEdgeModelLoader implements IGeometryLoader<GlowEdgeModelLoader.GlowEdgeGeometry> {
    public static final GlowEdgeModelLoader INSTANCE = new GlowEdgeModelLoader();
    private static final int DEFAULT_GLOW_COLOR = 0xFFFFFF;
    private static final float DEFAULT_GLOW_WIDTH = 1.0F;
    private static final float MAX_GLOW_WIDTH = 8.0F;

    private GlowEdgeModelLoader() {
    }

    @Override
    public GlowEdgeGeometry read(JsonObject modelContents, JsonDeserializationContext deserializationContext) throws JsonParseException {
        JsonObject glowEdgeObject = modelContents.getAsJsonObject("glow_edge");
        if (glowEdgeObject == null) {
            throw new JsonParseException("Missing 'glow_edge' object.");
        }

        int color = readColor(glowEdgeObject);
        float width = GsonHelper.getAsFloat(glowEdgeObject, "glowWidth", DEFAULT_GLOW_WIDTH);
        if (!Float.isFinite(width) || width <= 0.0F || width > MAX_GLOW_WIDTH) {
            throw new JsonParseException("'glowWidth' must be greater than 0 and at most " + MAX_GLOW_WIDTH + ".");
        }

        JsonObject clean = modelContents.deepCopy();
        clean.remove("glow_edge");
        clean.remove("loader");
        BlockModel baseModel = deserializationContext.deserialize(clean, BlockModel.class);
        return new GlowEdgeGeometry(baseModel, color, width);
    }

    private static int readColor(JsonObject glowEdgeObject) {
        if (!glowEdgeObject.has("glowColor")) {
            return DEFAULT_GLOW_COLOR;
        }

        JsonPrimitive primitive = glowEdgeObject.getAsJsonPrimitive("glowColor");
        try {
            int color;
            if (primitive.isNumber()) {
                color = primitive.getAsInt();
            } else {
                String value = primitive.getAsString();
                if (value.startsWith("#")) {
                    value = value.substring(1);
                } else if (value.startsWith("0x") || value.startsWith("0X")) {
                    value = value.substring(2);
                }
                color = Integer.parseInt(value, 16);
            }
            if (color < 0 || color > 0xFFFFFF) {
                throw new NumberFormatException("RGB color is outside the 24-bit range");
            }
            return color;
        } catch (NumberFormatException exception) {
            throw new JsonParseException("'glowColor' must be a 24-bit RGB number or hex string.", exception);
        }
    }

    public static class GlowEdgeGeometry implements IUnbakedGeometry<GlowEdgeGeometry> {
        private final BlockModel baseModel;
        private final int color;
        private final float width;

        private GlowEdgeGeometry(BlockModel baseModel, int color, float width) {
            this.baseModel = baseModel;
            this.color = color;
            this.width = width;
        }

        @Override
        public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                               Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState,
                               ItemOverrides overrides, ResourceLocation modelLocation) {
            BakedModel baseBakedModel = this.baseModel.bake(baker, this.baseModel, spriteGetter, modelState, modelLocation, true);
            return new GlowEdgeModel(baseBakedModel, this.color, this.width);
        }

        @Override
        public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context) {
            this.baseModel.resolveParents(modelGetter);
        }
    }
}
