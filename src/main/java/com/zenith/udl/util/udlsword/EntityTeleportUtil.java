package com.zenith.udl.util.udlsword;

import com.zenith.udl.manager.TargetManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;

public class EntityTeleportUtil {
    public static void EntityTeleport(Entity entity) {
        entity.setPos(Float.MAX_VALUE, -Float.MIN_VALUE, Float.MAX_VALUE);
        entity.setPosRaw(Float.MAX_VALUE, -Float.MIN_VALUE, Float.MAX_VALUE);
        entity.teleportTo(Float.MAX_VALUE, -Float.MIN_VALUE, Float.MAX_VALUE);
        entity.moveTo(Float.MAX_VALUE, -Float.MIN_VALUE, Float.MAX_VALUE);
        entity.blockPosition = new BlockPos(Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
        entity.feetBlockState = null;
        entity.xo = Float.MAX_VALUE;
        entity.yo = -Float.MIN_VALUE;
        entity.zo = Float.MAX_VALUE;
        entity.xOld = Float.MAX_VALUE;
        entity.yOld = -Float.MIN_VALUE;
        entity.zOld = Float.MAX_VALUE;
        entity.reapplyPosition();
        TargetManager.addTpTarget(entity);
    }
}
