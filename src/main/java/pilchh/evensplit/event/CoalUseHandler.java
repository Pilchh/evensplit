package pilchh.evensplit.event;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class CoalUseHandler {

    public static final Logger LOGGER = LoggerFactory.getLogger("evensplit");

    public static void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            LOGGER.info("here");
            if (world.isClientSide()) {
                return InteractionResult.PASS;
            }

            if (!player.isCrouching()) {
                return InteractionResult.PASS;
            }

            ItemStack itemStack = player.getItemInHand(hand);
            if (!itemStack.is(Items.COAL)) {
                return InteractionResult.PASS;
            }

            double reach = 100.0;
            HitResult hit = player.pick(reach, 0.0F, false);

            if (hit.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHitResult = (BlockHitResult) hit;
                BlockPos blockPosition = blockHitResult.getBlockPos();
                BlockState blockState = world.getBlockState(blockPosition);

                player.displayClientMessage(
                        Component.literal("Looking at: " + blockState.getBlock() + " at: " + blockPosition), false);
            }

            return InteractionResult.PASS;
        });
    }
}
