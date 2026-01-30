package pilchh.evensplit.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class BlockUtils {
    public static BlockEntity getLookedAtBlock(ServerPlayer player) {
        double reach = 5.0;
        HitResult hit = player.pick(reach, 0.0F, false);

        if (hit.getType() != HitResult.Type.BLOCK) {
            return null;
        }

        BlockHitResult blockHitResult = (BlockHitResult) hit;
        BlockPos blockPosition = blockHitResult.getBlockPos();
        BlockEntity blockEntity = player.level().getBlockEntity(blockPosition);

        return blockEntity;
    }
}
