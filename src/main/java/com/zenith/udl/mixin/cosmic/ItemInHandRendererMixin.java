package com.zenith.udl.mixin.cosmic;

import com.zenith.udl.init.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

    @Shadow private ItemStack mainHandItem;
    @Shadow private float mainHandHeight;
    @Shadow private float oMainHandHeight;

    @Inject(method = "tick", at = @At("TAIL"))
    public void onTickTail(CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        // メイン手で「使用中（右クリック長押し中）」かつ「使用中のアイテムがこの剣である」場合のみ実行
        if (player.isUsingItem() && player.getUsedItemHand() == InteractionHand.MAIN_HAND) {
            ItemStack usingItem = player.getUseItem();

            if (usingItem.getItem() == ModItems.UDL_SWORD.get()) {
                // ガード中だけアニメーションによる落ち込みを防止（1.0Fに保持）
                this.mainHandHeight = 1.0F;
                this.oMainHandHeight = 1.0F;
            }
        }
        // 使用していない時や、別のアイテムに切り替えたときは何もしない（バニラの通常アニメーション・アイテム切り替えが実行される）
    }
}