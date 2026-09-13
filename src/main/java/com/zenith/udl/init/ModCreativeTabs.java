package com.zenith.udl.init;

import com.zenith.udl.Udl;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Udl.MODID);

    // 2. 新しいクリエイティブタブの登録
    public static final RegistryObject<CreativeModeTab> UDL_TAB = CREATIVE_MODE_TABS.register("udl_tab",
            () -> CreativeModeTab.builder()
                    // タブのアイコンアイテムを指定
                    .icon(() -> new ItemStack(ModItems.UDL_SWORD.get()))
                    // タブの表示名（langファイルで翻訳可能）
                    .title(Component.translatable("creativetab.udl_tab"))
                    // タブに追加するアイテムを設定
                    .displayItems((displayParameters, output) -> {
                        output.accept(ModItems.UDL_SWORD.get());
                        output.accept(ModItems.POCKET_WATCH.get());
                        output.accept(ModItems.DEBUG_ITEM.get());
                    })
                    .build());

    // 3. メインModクラスから呼び出す登録メソッド
    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
