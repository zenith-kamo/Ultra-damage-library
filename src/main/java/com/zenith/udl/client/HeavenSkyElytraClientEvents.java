package com.zenith.udl.client;

import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "udl", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class HeavenSkyElytraClientEvents {
    private HeavenSkyElytraClientEvents() {}

    @SubscribeEvent
    public static void addPlayerLayers(EntityRenderersEvent.AddLayers event) {
        for (String skin : event.getSkins()) {
            PlayerRenderer renderer = event.getPlayerSkin(skin);
            if (renderer != null) {
                renderer.addLayer(new HeavenSkyElytraLayer<>(renderer, event.getEntityModels()));
            }
        }
    }
}
