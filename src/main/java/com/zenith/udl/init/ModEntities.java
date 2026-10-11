package com.zenith.udl.init;

import com.zenith.udl.Udl;
import com.zenith.udl.entity.ZenithEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Udl.MODID);

    public static final RegistryObject<EntityType<ZenithEntity>> ZENITH =
            ENTITY_TYPES.register("zenith", () -> EntityType.Builder.of(ZenithEntity::new, MobCategory.MISC).sized(0.6F, 1.8F).clientTrackingRange(10).build("zenith"));

    private ModEntities() {
    }

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return net.minecraft.world.entity.Mob
                .createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D);
    }

    @Mod.EventBusSubscriber(modid = Udl.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class AttributeEvents {

        @SubscribeEvent
        public static void registerAttributes(EntityAttributeCreationEvent event) {
            event.put(ZENITH.get(), createAttributes().build());
        }
    }
}
