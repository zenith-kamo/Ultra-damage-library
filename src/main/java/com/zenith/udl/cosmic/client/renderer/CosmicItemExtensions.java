package com.zenith.udl.cosmic.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public class CosmicItemExtensions implements IClientItemExtensions {
    public static final CosmicItemExtensions INSTANCE = new CosmicItemExtensions();

    @Override
    public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack itemInHand, float partialTick, float equipProgress, float swingProcess) {
        // 右クリック使用中、かつガード(UseAnim.BLOCK)またはこの剣を構える状態の場合
        if (player.isUsingItem() && player.getUseItem() == itemInHand) {
            int horizontal = arm == HumanoidArm.RIGHT ? 1 : -1;

            // --- 1. 基本位置調整（手元から画面中央前寄りに平行移動） ---
            poseStack.translate((float)horizontal * 0.56F, -0.52F + equipProgress * -0.6F, -0.72F);
            poseStack.translate((float)horizontal * -0.14142136F, 0.08F, 0.14142136F);

            // --- 2. 剣を傾けてガードの姿勢にする回転 ---
            poseStack.mulPose(Axis.XP.rotationDegrees(-102.25F));
            poseStack.mulPose(Axis.YP.rotationDegrees((float)horizontal * 13.365F));
            poseStack.mulPose(Axis.ZP.rotationDegrees((float)horizontal * 78.05F));

            // --- 3. 振った際のアニメーション補正（スイング時の挙動） ---
//            if (swingProcess > 0.0F) {
//                float f1 = (float)Math.sin(Math.sqrt((double)swingProcess) * Math.PI);
//                poseStack.mulPose(Axis.YP.rotationDegrees((float)Math.sin((double)(swingProcess * swingProcess) * Math.PI) * -20.0F));
//                poseStack.mulPose(Axis.ZP.rotationDegrees(f1 * -20.0F));
//                poseStack.mulPose(Axis.XP.rotationDegrees(f1 * -80.0F));
//            }

            return true; // バニラ側のデフォルト手の位置調整をキャンセル
        }
        return false;
    }
}