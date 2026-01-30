package pilchh.evensplit.containers;

import java.util.List;
import java.util.Objects;

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
                .map(block -> ContainerAdapterFactory.fromBlockEntity(block, world))
                .filter(Objects::nonNull)
                .toList();

        if (containers.isEmpty()) {
            return;
        }

        long itemsRemaining = InventoryUtils.countInInventory(player, item);
        int containersSize = containers.size();

        for (int i = 0; i < containers.size(); i++) {
            long fairShare = itemsRemaining / (containersSize - i);
            long capacity = containers.get(i).freeSpace(item);
            long target = Math.min(fairShare, capacity);

            long inserted = containers.get(i).insertFromPlayer(player, item, target);
            itemsRemaining -= inserted;
        }
    }
}
