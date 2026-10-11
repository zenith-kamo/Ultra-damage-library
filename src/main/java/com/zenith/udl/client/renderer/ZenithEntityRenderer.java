
package com.zenith.udl.client.renderer;

import com.zenith.udl.entity.ZenithEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ZenithEntityRenderer
        extends MobRenderer<ZenithEntity, ZenithEntityModel> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("minecraft", "textures/entity/player/wide/steve.png");

    public ZenithEntityRenderer(
            EntityRendererProvider.Context context
    ) {
        super(
                context,
                new ZenithEntityModel(
                        context.bakeLayer(ModelLayers.PLAYER)
                ),
                0.0F
        );
    }

    @Override
    public ResourceLocation getTextureLocation(ZenithEntity entity) {
        return TEXTURE;
    }
}
