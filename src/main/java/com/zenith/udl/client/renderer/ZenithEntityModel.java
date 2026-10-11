
package com.zenith.udl.client.renderer;

import com.zenith.udl.entity.ZenithEntity;
import net.minecraft.client.model.PlayerModel;

public class ZenithEntityModel extends PlayerModel<ZenithEntity> {

    public ZenithEntityModel(
            net.minecraft.client.model.geom.ModelPart root
    ) {
        super(root, false);
    }

    @Override
    public void setupAnim(
            ZenithEntity entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        // アニメーションなし
    }
}
