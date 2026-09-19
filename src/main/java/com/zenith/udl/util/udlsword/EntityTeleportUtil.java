package com.zenith.udl.util.udlsword;

import com.zenith.udl.manager.TargetManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;

public class EntityTeleportUtil {
    public static void EntityTeleport(Entity entity) {
        entity.setPos(9999999, -9999999, 9999999);
        entity.setPosRaw(9999999, -9999999, 9999999);
        entity.teleportTo(9999999, -9999999, 9999999);
        entity.moveTo(9999999, -9999999, 9999999);
        entity.blockPosition = new BlockPos(9999999, -9999999, 9999999);
        entity.feetBlockState = null;
        entity.xo = 9999999;
        entity.yo = -9999999;
        entity.zo = 9999999;
        entity.xOld = 9999999;
        entity.yOld = -9999999;
        entity.zOld = 9999999;
        entity.reapplyPosition();
        TargetManager.addTpTarget(entity);
    }
}
