package pilchh.evensplit.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.UUID;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

public class PlayerInteractionHandler {
    private static final Set<UUID> activeUsers = new HashSet<>();
    private static Map<UUID, List<BlockEntity>> interactedBlocks = new HashMap<UUID, List<BlockEntity>>();

    public static void startTracking(Player player, ItemVariant item) {
        UUID playerUUID = player.getUUID();

        // User already tracked
        if (isTracking(player)) {
            return;
        }

        if (player.isCrouching() && player.isHolding(item.getItem())) {
            interactedBlocks.put(playerUUID, new ArrayList<>());
            activeUsers.add(playerUUID);
        }
    }

    public static void stopTracking(Player player) {
        UUID playerUUID = player.getUUID();

        // User is not being tracked
        if (!isTracking(player)) {
            return;
        }

        interactedBlocks.remove(playerUUID);
        activeUsers.remove(playerUUID);
    }

    public static boolean isTracking(Player player) {
        return activeUsers.contains(player.getUUID());
    }

    public static void addBlock(Player player, BlockEntity block) {
        UUID playerUUID = player.getUUID();

        if (!interactedBlocks.get(playerUUID).contains(block)) {
            interactedBlocks.get(playerUUID).add(block);
        }
    }

    public static List<BlockEntity> getBlocks(Player player) {
        return interactedBlocks.get(player.getUUID());
    }
}
