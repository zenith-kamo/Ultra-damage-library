package com.zenith.udl.cosmic.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonArray;
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
    private static final float DEFAULT_GLOW_SPEED = 1.0F;
    private static final float MAX_GLOW_SPEED = 10.0F;
    private static final float DEFAULT_GLOW_CYCLE_WIDTH = 125.0F;
    private static final float MAX_GLOW_CYCLE_WIDTH = 4096.0F;

    private GlowEdgeModelLoader() {
    }

    @Override
    public GlowEdgeGeometry read(JsonObject modelContents, JsonDeserializationContext deserializationContext) throws JsonParseException {
        JsonObject glowEdgeObject = modelContents.getAsJsonObject("glow_edge");
        if (glowEdgeObject == null) {
            throw new JsonParseException("Missing 'glow_edge' object.");
        }

        GlowEdgeSettings settings = readSettings(glowEdgeObject);

        JsonObject clean = modelContents.deepCopy();
        clean.remove("glow_edge");
        clean.remove("loader");
        BlockModel baseModel = deserializationContext.deserialize(clean, BlockModel.class);
        return new GlowEdgeGeometry(baseModel, settings);
    }

    public static GlowEdgeSettings readSettings(JsonObject glowEdgeObject) throws JsonParseException {
        int color = readColor(glowEdgeObject);
        int[] colors = readColors(glowEdgeObject, color);
        float width = GsonHelper.getAsFloat(glowEdgeObject, "glowWidth", DEFAULT_GLOW_WIDTH);
        if (!Float.isFinite(width) || width <= 0.0F || width > MAX_GLOW_WIDTH) {
            throw new JsonParseException("'glowWidth' must be greater than 0 and at most " + MAX_GLOW_WIDTH + ".");
        }
        float speed = GsonHelper.getAsFloat(glowEdgeObject, "glowSpeed", DEFAULT_GLOW_SPEED);
        if (!Float.isFinite(speed) || speed <= 0.0F || speed > MAX_GLOW_SPEED) {
            throw new JsonParseException("'glowSpeed' must be greater than 0 and at most " + MAX_GLOW_SPEED + ".");
        }
        float cycleWidth = GsonHelper.getAsFloat(glowEdgeObject, "glowCycleWidth", DEFAULT_GLOW_CYCLE_WIDTH);
        if (!Float.isFinite(cycleWidth) || cycleWidth <= 0.0F || cycleWidth > MAX_GLOW_CYCLE_WIDTH) {
            throw new JsonParseException("'glowCycleWidth' must be greater than 0 and at most "
                    + MAX_GLOW_CYCLE_WIDTH + ".");
        }
        int animation = readAnimation(glowEdgeObject);
        return new GlowEdgeSettings(colors, width, speed, cycleWidth, animation);
    }

    public static final class GlowEdgeSettings {
        private final int[] colors;
        private final float width;
        private final float speed;
        private final float cycleWidth;
        private final int animation;

        private GlowEdgeSettings(int[] colors, float width, float speed, float cycleWidth, int animation) {
            this.colors = colors.clone();
            this.width = width;
            this.speed = speed;
            this.cycleWidth = cycleWidth;
            this.animation = animation;
        }

        int[] colors() {
            return this.colors.clone();
        }

        float width() {
            return this.width;
        }

        float speed() {
            return this.speed;
        }

        float cycleWidth() {
            return this.cycleWidth;
        }

        int animation() {
            return this.animation;
        }
    }

    private static int[] readColors(JsonObject glowEdgeObject, int fallbackColor) {
        if (!glowEdgeObject.has("glowColors")) {
            return new int[]{fallbackColor};
        }

        JsonArray colorArray = GsonHelper.getAsJsonArray(glowEdgeObject, "glowColors");
        if (colorArray.isEmpty() || colorArray.size() > 3) {
            throw new JsonParseException("'glowColors' must contain between 1 and 3 colors.");
        }
        int[] colors = new int[colorArray.size()];
        for (int index = 0; index < colorArray.size(); index++) {
            colors[index] = parseColor(colorArray.get(index).getAsJsonPrimitive(), "glowColors[" + index + "]");
        }
        return colors;
    }

    private static int readColor(JsonObject glowEdgeObject) {
        if (!glowEdgeObject.has("glowColor")) {
            return DEFAULT_GLOW_COLOR;
        }
        return parseColor(glowEdgeObject.getAsJsonPrimitive("glowColor"), "glowColor");
    }

    private static int parseColor(JsonPrimitive primitive, String property) {
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
            throw new JsonParseException("'" + property + "' must be a 24-bit RGB number or hex string.", exception);
        }
    }

    private static int readAnimation(JsonObject glowEdgeObject) {
        String animation = GsonHelper.getAsString(glowEdgeObject, "glowAnimation", "none");
        return switch (animation.toLowerCase(java.util.Locale.ROOT)) {
            case "none" -> 0;
            case "cycle" -> 1;
            case "pulse" -> 2;
            default -> throw new JsonParseException("'glowAnimation' must be 'none', 'cycle', or 'pulse'.");
        };
    }

    public static class GlowEdgeGeometry implements IUnbakedGeometry<GlowEdgeGeometry> {
        private final BlockModel baseModel;
        private final GlowEdgeSettings settings;

        private GlowEdgeGeometry(BlockModel baseModel, GlowEdgeSettings settings) {
            this.baseModel = baseModel;
            this.settings = settings;
        }

        @Override
        public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                               Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState,
                               ItemOverrides overrides, ResourceLocation modelLocation) {
            BakedModel baseBakedModel = this.baseModel.bake(baker, this.baseModel, spriteGetter, modelState, modelLocation, true);
            return new GlowEdgeModel(baseBakedModel, this.settings);
        }

        @Override
        public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context) {
            this.baseModel.resolveParents(modelGetter);
        }
    }
}
