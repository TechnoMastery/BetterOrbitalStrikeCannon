package net.minheur.betterosc.tank;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.UUID;
import java.util.function.Consumer;

public final class TankOperationHandler {

    public static OperationResults incrementTank(ItemStack tank, long amount) {
        if (!TankHandler.isTank(tank)) throw new IllegalArgumentException("Incrementing on non-tank item!");

        UUID uuid = TankHandler.getTankUUID(tank);
        TankContent actualContent = TankHandler.getTanks().get(uuid);
        if (actualContent == null) throw new IllegalStateException("Tank content is null!");

        if (actualContent.amount() > Long.MAX_VALUE - amount) return OperationResults.TOO_MUCH;

        TankContent newContent = new TankContent(
                actualContent.item(),
                actualContent.amount() + amount
        );
        TankHandler.getTanks().put(uuid, newContent);

        return OperationResults.SUCCESS;
    }
    public static boolean safeIncrement(ItemStack tank, ItemStack target, Consumer<ItemStack> replaceTarget) {
        if (!TankOperationHandler.canItemGoInTank(tank, target)) return true;

        OperationResults result = TankOperationHandler.incrementTank(tank, target.getCount());
        if (result == OperationResults.SUCCESS) {
            replaceTarget.accept(ItemStack.EMPTY);
            return true;
        }

        long toStore = Long.MAX_VALUE - TankOperationHandler.getAmountStored(tank);
        long left = target.getCount() - toStore;

        if (toStore == 0) return false;

        OperationResults secondResult = TankOperationHandler.incrementTank(tank, toStore);
        if (secondResult == OperationResults.TOO_MUCH) return false;

        target.setCount((int) left);
        return true;
    }

    public static OperationResults decrementTank(ItemStack tank, long amount) {
        if (!TankHandler.isTank(tank)) throw new IllegalArgumentException("Incrementing on non-tank item!");

        UUID uuid = TankHandler.getTankUUID(tank);
        TankContent actualContent = TankHandler.getTanks().get(uuid);
        if (actualContent == null) throw new IllegalStateException("Tank content is null!");

        if (actualContent.amount() < amount) return OperationResults.NOT_ENOUGH;
        if (actualContent.amount() == amount) {
            TankHandler.getTanks().remove(uuid);
            return OperationResults.EMPTIED;
        }

        TankContent newContent = new TankContent(
                actualContent.item(),
                actualContent.amount() - amount
        );
        TankHandler.getTanks().put(uuid, newContent);

        return OperationResults.SUCCESS;
    }

    public static ItemStack getTankContent(ItemStack tank) {
        if (!TankHandler.isTank(tank)) throw new IllegalArgumentException("Getting content on non-tank item!");

        UUID uuid = TankHandler.getTankUUID(tank);
        TankContent content = TankHandler.getTanks().get(uuid);
        if (content == null) throw new IllegalStateException("Tank content is null!");

        return content.item();
    }
    public static long getAmountStored(ItemStack tank) {
        if (!TankHandler.isTank(tank)) throw new IllegalArgumentException("Incrementing on non-tank item!");

        UUID uuid = TankHandler.getTankUUID(tank);
        TankContent content = TankHandler.getTanks().get(uuid);
        if (content == null) throw new IllegalStateException("Tank content is null!");

        return content.amount();
    }

    public static boolean getIsSameItem(ItemStack ref, ItemStack target) {
        if (ref.isEmpty() || target.isEmpty()) return false;
        if (ref.getMaxCount() != target.getMaxCount()) return false;

        ItemStack refCopy = ref.copy();
        ItemStack targetCopy = target.copy();

        refCopy.setCount(1);
        targetCopy.setCount(1);

        return ItemStack.areEqual(refCopy, targetCopy);
    }
    public static boolean canItemGoInTank(ItemStack tank, ItemStack target) {
        ItemStack tankContent = getTankContent(tank);
        return getIsSameItem(tankContent, target);
    }

}
