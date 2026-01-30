package pilchh.evensplit.containers.adapters;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.server.level.ServerPlayer;
import pilchh.evensplit.containers.ContainerAdapter;

public class GenericAdapter implements ContainerAdapter {

    private final Storage<ItemVariant> storage;

    public GenericAdapter(Storage<ItemVariant> storage) {
        this.storage = storage;
    }

    @Override
    public boolean accepts(ItemVariant item) {
        try (Transaction tx = Transaction.openOuter()) {
            long inserted = storage.insert(item, 1, tx);
            tx.abort();
            return inserted > 0;
        }
    }

    @Override
    public long freeSpace(ItemVariant item) {
        try (Transaction tx = Transaction.openOuter()) {
            long maxInsert = storage.insert(item, Long.MAX_VALUE, tx);
            tx.abort();
            return maxInsert;
        }
    }

    @Override
    public long insert(ItemVariant item, long amount, Transaction tx) {
        return storage.insert(item, amount, tx);
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
