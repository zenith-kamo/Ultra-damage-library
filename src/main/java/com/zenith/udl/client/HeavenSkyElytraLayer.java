package com.zenith.udl.client;

import com.zenith.udl.init.ModItems;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class HeavenSkyElytraLayer<T extends AbstractClientPlayer, M extends EntityModel<T>> extends ElytraLayer<T, M> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("udl", "textures/entity/equipment/heaven_sky_elytra.png");

    public HeavenSkyElytraLayer(RenderLayerParent<T, M> parent, EntityModelSet modelSet) {
        super(parent, modelSet);
    }

    @Override
    public boolean shouldRender(ItemStack stack, T entity) {
        return stack.is(ModItems.HEAVEN_SKY_ELYTRA.get());
    }

    @Override
    public ResourceLocation getElytraTexture(ItemStack stack, T entity) {
        return TEXTURE;
    }
}
