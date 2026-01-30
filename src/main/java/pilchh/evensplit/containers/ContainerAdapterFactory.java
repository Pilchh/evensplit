package pilchh.evensplit.containers;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import pilchh.evensplit.containers.adapters.ChestAdapter;
import pilchh.evensplit.containers.adapters.GenericAdapter;

public class ContainerAdapterFactory {
    public static ContainerAdapter fromBlockEntity(BlockEntity blockEntity, ServerLevel world) {
        Storage<ItemVariant> storage = ItemStorage.SIDED.find(world, blockEntity.getBlockPos(), null);

        if (storage == null)
            return null;

        if (blockEntity instanceof ChestBlockEntity)
            return new ChestAdapter(storage);

        return new GenericAdapter(storage);
    }
}
