package pilchh.evensplit.utils;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class InventoryUtils {
    public static int countInInventory(ServerPlayer player, ItemVariant item) {
        Inventory inventory = player.getInventory();
        int total = 0;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);

            if (ItemVariant.of(stack).equals(item)) {
                total += stack.getCount();
            }
        }

        return total;
    }
}
