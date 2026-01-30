package pilchh.evensplit.containers.adapters;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.advancements.criterion.PlayerInteractTrigger;
import net.minecraft.server.level.ServerPlayer;
import pilchh.evensplit.containers.ContainerAdapter;

public class ChestAdapter implements ContainerAdapter {
    private final Storage<ItemVariant> storage;

    public ChestAdapter(Storage<ItemVariant> storage) {
        this.storage = storage;
    }

    @Override
    public boolean accepts(ItemVariant item) {
        // Chests can accept all items
        return true;
    }

    @Override
    public long freeSpace(ItemVariant item) {
        long freeSpace = 0;
        try (Transaction transaction = Transaction.openOuter()) {
            freeSpace = storage.insert(item, Long.MAX_VALUE, transaction);
        }

        return freeSpace;
    }

    @Override
    public long insert(ItemVariant item, long amount, Transaction transaction) {
        return storage.insert(item, amount, transaction);
    }

    @Override
    public long insertFromPlayer(ServerPlayer player, ItemVariant item, long amount) {
        Storage<ItemVariant> playerStorage = PlayerInventoryStorage.of(player);

        try (Transaction transaction = Transaction.openOuter()) {
            long extracted = playerStorage.extract(item, amount, transaction);

            if (extracted == 0) {
                transaction.abort();
                return 0;
            }

            long inserted = this.insert(item, extracted, transaction);

            if (inserted < extracted) {
                long leftover = extracted - inserted;
                playerStorage.insert(item, leftover, transaction);
            }

            transaction.commit();
            return inserted;
        }
    }
}
