package pilchh.evensplit.containers;

import java.util.List;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import pilchh.evensplit.event.InteractionManager;
import pilchh.evensplit.utils.InventoryUtils;

public class SplittingLogic {
    public static void spread(ServerPlayer player, ItemVariant item) {
        List<BlockEntity> blocks = InteractionManager.TRACKER.getBlocks(player);
        ServerLevel world = player.level();

        if (blocks.isEmpty()) {
            return;
        }

        List<ContainerAdapter> containers = blocks.stream()
                .filter(block -> (block instanceof ContainerAdapter))
                .map(block -> ContainerAdapterFactory.fromBlockEntity(block, world))
                .toList();

        long itemCount = InventoryUtils.countInInventory(player, item);
        long splitAmount = Math.floorDiv(itemCount, containers.size());
        long overflow = itemCount - splitAmount * containers.size();

        for (int i = 0; i < containers.size(); i++) {
            long amount = splitAmount;

            // Handle overflow items
            if (i - 1 == containers.size()) {
                amount += overflow;
            }

            containers.get(i).insertFromPlayer(player, item, amount);
        }
    }
}
