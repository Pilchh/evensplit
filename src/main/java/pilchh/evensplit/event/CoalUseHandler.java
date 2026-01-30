package pilchh.evensplit.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CoalUseHandler {

    public static final Logger LOGGER = LoggerFactory.getLogger("evensplit");
    private static final Item ITEM = Items.COAL;
    private static final Set<UUID> activeUsers = new HashSet<>();
    private static Map<UUID, List<BlockEntity>> interactedBlocks = new HashMap<UUID, List<BlockEntity>>();

    private static int countItemInInventory(Player player, Item item) {
        int total = 0;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);

            if (stack.is(item)) {
                total += stack.getCount();
            }
        }

        return total;
    }

    private static int countFullFreeStacks(ChestBlockEntity chestEntity) {
        int chestSize = chestEntity.getContainerSize();
        int freeSpaces = 0;

        for (int i = 0; i < chestSize; i++) {
            ItemStack stack = chestEntity.getItem(i);

            if (stack.isEmpty()) {
                freeSpaces++;
            }
        }

        return freeSpaces;
    }

    private static Map<Integer, ItemStack> getStacksOfItem(ChestBlockEntity chestEntity, Item item) {
        int chestSize = chestEntity.getContainerSize();

        Map<Integer, ItemStack> items = new HashMap<>();
        for (int i = 0; i < chestSize; i++) {
            ItemStack itemStack = chestEntity.getItem(i);
            if (itemStack.getItem() == item) {
                items.put(i, itemStack);
            }
        }

        return items;
    }

    private static int countPartialFreeStacks(ChestBlockEntity chestEntity, Item item) {
        Map<Integer, ItemStack> items = getStacksOfItem(chestEntity, item);

        int freeSpaces = 0;
        for (ItemStack itemStack : items.values()) {
            freeSpaces += 64 - itemStack.getCount();
        }

        return freeSpaces;
    }

    private static int calculateTotalAvailableSpace(ChestBlockEntity chestEntity, Item item) {
        int emptyFullSlotCount = countFullFreeStacks(chestEntity);
        int emptyPartialSlotCount = countPartialFreeStacks(chestEntity, item);

        return emptyFullSlotCount * 64 + emptyPartialSlotCount;
    }

    private static BlockEntity getBlockInteraction(ServerPlayer player) {
        double reach = 5.0;
        HitResult hit = player.pick(reach, 0.0F, false);

        if (hit.getType() != HitResult.Type.BLOCK) {
            return null;
        }

        BlockHitResult blockHitResult = (BlockHitResult) hit;
        BlockPos blockPosition = blockHitResult.getBlockPos();
        ServerLevel world = player.level();
        BlockEntity blockEntity = world.getBlockEntity(blockPosition);

        return blockEntity;
    }

    private static void spreadItemsAcrossContainers(ServerPlayer player) {
        List<BlockEntity> blocks = interactedBlocks.get(player.getUUID());
        if (blocks == null || blocks.isEmpty()) {
            return;
        }

        List<ChestBlockEntity> chests = interactedBlocks.get(player.getUUID()).stream()
                .filter(block -> (block instanceof ChestBlockEntity))
                .map(block -> (ChestBlockEntity) block).toList();

        if (chests.isEmpty()) {
            return;
        }

        int chestCount = chests.size();

        int totalFreeSpace = chests.stream()
                .mapToInt(chest -> calculateTotalAvailableSpace(chest, ITEM))
                .sum();

        int totalItems = countItemInInventory(player, ITEM);

        if (totalItems > totalFreeSpace) {
            return;
        }

        int splitAmount = Math.floorDiv(totalItems, chestCount);
        int overflow = totalItems - splitAmount * chestCount;

        for (int i = 0; i < chestCount; i++) {
            int count = splitAmount;
            if (i == chestCount - 1) {
                count += overflow;
            }

            transferToContainer(chests.get(i), player, count);
        }
    }

    private static void transferToContainer(BlockEntity block, ServerPlayer player, int count) {
        Storage<ItemVariant> chestStorage = ItemStorage.SIDED.find(player.level(),
                block.getBlockPos(), null);
        Storage<ItemVariant> playerStorage = PlayerInventoryStorage.of(player);

        if (chestStorage != null && playerStorage != null) {
            try (Transaction transaction = Transaction.openOuter()) {
                long items = playerStorage.extract(ItemVariant.of(ITEM), count,
                        transaction);
                chestStorage.insert(ItemVariant.of(ITEM), items, transaction);
                transaction.commit();
            }
        }
    }

    public static void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClientSide()) {
                return InteractionResult.PASS;
            }

            if (!player.isCrouching()) {
                return InteractionResult.PASS;
            }

            ItemStack itemStack = player.getItemInHand(hand);
            if (!itemStack.is(ITEM)) {
                return InteractionResult.PASS;
            }

            UUID uuid = player.getUUID();
            activeUsers.add(uuid);
            interactedBlocks.computeIfAbsent(uuid, u -> new ArrayList<>());

            return InteractionResult.PASS;
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                UUID uuid = player.getUUID();

                if (!activeUsers.contains(uuid)) {
                    continue;
                }

                if (!player.isCrouching()) {
                    spreadItemsAcrossContainers(player);
                    activeUsers.remove(uuid);
                    interactedBlocks.remove(uuid);
                    continue;
                }

                BlockEntity interactedBlock = getBlockInteraction(player);

                if (interactedBlock instanceof ChestBlockEntity) {
                    List<BlockEntity> chests = interactedBlocks.get(uuid);

                    if (!chests.contains(interactedBlock)) {
                        interactedBlocks.get(uuid).add(interactedBlock);
                    }
                }
            }
        });
    }
}
