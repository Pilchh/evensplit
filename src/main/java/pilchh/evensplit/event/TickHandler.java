package pilchh.evensplit.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.entity.BlockEntity;
import pilchh.evensplit.containers.SplittingLogic;
import pilchh.evensplit.utils.BlockUtils;

public class TickHandler {
    public static void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            // Don't bother checking a tracked player
            if (InteractionManager.TRACKER.isTracking(player)) {
                return InteractionResult.PASS;
            }

            // Start tracking the player
            System.out.println("Starting track");
            ItemVariant heldItem = ItemVariant.of(player.getItemInHand(hand));
            InteractionManager.TRACKER.startTracking(player, heldItem);

            return InteractionResult.PASS;
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (!InteractionManager.TRACKER.isTracking(player)) {
                    continue;
                }

                BlockEntity block = BlockUtils.getLookedAtBlock(player);
                if (block != null) {
                    InteractionManager.TRACKER.addBlock(player, block);
                }

                if (!player.isCrouching()) {
                    ItemVariant handItem = ItemVariant.of(player.getMainHandItem());
                    SplittingLogic.spread(player, handItem);
                    InteractionManager.TRACKER.stopTracking(player);
                    System.out.println("Stopping track");
                }
            }
        });
    }
}
