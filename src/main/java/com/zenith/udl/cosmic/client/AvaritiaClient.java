package com.zenith.udl.cosmic.client;

import com.zenith.udl.Udl;
import com.zenith.udl.cosmic.client.model.CosmicModelLoader;
import com.zenith.udl.cosmic.client.shader.AvaritiaShaders;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Udl.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class AvaritiaClient {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRegisterShaders(RegisterShadersEvent event) {
        AvaritiaShaders.onRegisterShaders(event);
    }

    @SubscribeEvent
    public static void registerLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register("cosmic", CosmicModelLoader.INSTANCE);
    }
}