package com.zenith.udl.client;

import com.zenith.udl.init.ModItems; // ご自身のItem登録クラスに置き換えてください
import com.zenith.udl.item.UltraDamageLibrarySwordItem;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedGoldenAppleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = "udl", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // スレッドセーフに呼び出すため enqueueWork を使用
        event.enqueueWork(() -> {
            registerBlockingProperty(ModItems.UDL_SWORD.get());
        });
    }

    /**
     * アイテムに blocking プロパティ（0.0 または 1.0）を登録するヘルパーメソッド
     */
    private static void registerBlockingProperty(Item item) {
        ItemProperties.register(
                item,
                new ResourceLocation("blocking"),
                (stack, level, entity, seed) -> {
                    // プレイヤー/エンティティが存在し、アイテムを使用中(右クリック中)かつ
                    // 現在使用しているアイテムがこのアイテム自体である場合に 1.0F を返す
                    return entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F;
                }
        );
    }
}