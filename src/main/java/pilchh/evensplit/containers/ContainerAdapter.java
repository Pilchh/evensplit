package pilchh.evensplit.containers;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.server.level.ServerPlayer;

public interface ContainerAdapter {
    /**
     * Returns a boolean for if the provided item can be held inside container.
     *
     * @param item the item to check if the container can accept
     * @return the boolean value for if the container can accept the item.
     */
    boolean accepts(ItemVariant item);

    /**
     * Returns a value for how many free spaces for the provided item are
     * available inside the container.
     *
     * @param item the item to check against
     * @return how many spaces are available
     */
    long freeSpace(ItemVariant item);

    /**
     * Insert an item into the container and returns the output.
     *
     * @param item        the item to insert
     * @param amount      the number of items to insert
     * @param transaction the transaction to handle the item movement
     * @return the output of the transaction
     */
    long insert(ItemVariant item, long amount, Transaction transaction);

    /**
     * Insert an item into the container and removes it from a players inventory.
     *
     * @param player    the player to take the items from
     * @param container the container to put the items into
     * @param item      the item to insert
     * @param amount    the number of items to insert
     * @return the output of the transaction
     */
    long insertFromPlayer(ServerPlayer player, ItemVariant item, long amount);
}
