package net.minheur.betterosc.tank;

import net.minecraft.item.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.UUID;
import java.util.function.Consumer;

public final class TankOperationHandler {

    public static OperationResults incrementTank(ItemStack tank, long amount) {
        if (!TankHandler.isTank(tank)) throw new IllegalArgumentException("Incrementing on non-tank item!");

        UUID uuid = TankHandler.getTankUUID(tank);
        TankContent actualContent = TankHandler.getTankState().getTanks().get(uuid);
        if (actualContent == null) throw new IllegalStateException("Tank content is null!");

        if (actualContent.amount() > Long.MAX_VALUE - amount) return OperationResults.TOO_MUCH;

        TankContent newContent = new TankContent(
                actualContent.item(),
                actualContent.amount() + amount
        );
        TankHandler.getTankState().setTank(uuid, newContent);
        TankHandler.updateTankDisplay(tank);

        return OperationResults.SUCCESS;
    }
    public static boolean safeIncrement(ItemStack tank, ItemStack target, Consumer<ItemStack> replaceTarget, int[] maxToAdd) {
        if (!TankOperationHandler.canItemGoInTank(tank, target)) return true;

        int adding = maxToAdd[0] == -1 ? target.getCount() : Math.min(target.getCount(), maxToAdd[0]);

        OperationResults result = TankOperationHandler.incrementTank(tank, adding);
        if (result == OperationResults.SUCCESS) {
            if (adding == target.getCount()) replaceTarget.accept(ItemStack.EMPTY);
            else target.setCount(target.getCount() - maxToAdd[0]);
            maxToAdd[0] -= adding;
            return true;
        }

        long toStore = Long.MAX_VALUE - TankOperationHandler.getAmountStored(tank);
        long left = adding - toStore;

        if (toStore == 0) return false;

        OperationResults secondResult = TankOperationHandler.incrementTank(tank, toStore);
        if (secondResult == OperationResults.TOO_MUCH) return false;

        target.setCount((int) left);
        return true;
    }
    public static boolean safeIncrement(ItemStack tank, ItemStack target, Consumer<ItemStack> replaceTarget) {
        return safeIncrement(tank, target, replaceTarget, new int[]{-1});
    }

    public static OperationResults decrementTank(ItemStack tank, long amount) {
        if (!TankHandler.isTank(tank)) throw new IllegalArgumentException("Incrementing on non-tank item!");

        UUID uuid = TankHandler.getTankUUID(tank);
        TankContent actualContent = TankHandler.getTankState().getTanks().get(uuid);
        if (actualContent == null) throw new IllegalStateException("Tank content is null!");

        if (actualContent.amount() < amount) return OperationResults.NOT_ENOUGH;
        if (actualContent.amount() == amount) {
            TankHandler.getTankState().removeTank(uuid);
            return OperationResults.EMPTIED;
        }

        TankContent newContent = new TankContent(
                actualContent.item(),
                actualContent.amount() - amount
        );
        TankHandler.getTankState().setTank(uuid, newContent);
        TankHandler.updateTankDisplay(tank);

        return OperationResults.SUCCESS;
    }

    public static ItemStack getTankContent(ItemStack tank) {
        if (!TankHandler.isTank(tank)) throw new IllegalArgumentException("Getting content on non-tank item!");

        UUID uuid = TankHandler.getTankUUID(tank);
        TankContent content = TankHandler.getTankState().getTanks().get(uuid);
        if (content == null) throw new IllegalStateException("Tank content is null!");

        return content.item();
    }
    public static long getAmountStored(ItemStack tank) {
        if (!TankHandler.isTank(tank)) throw new IllegalArgumentException("Incrementing on non-tank item!");

        UUID uuid = TankHandler.getTankUUID(tank);
        TankContent content = TankHandler.getTankState().getTanks().get(uuid);
        if (content == null) throw new IllegalStateException("Tank content is null!");

        return content.amount();
    }

    public static boolean getIsSameItem(@NonNull ItemStack ref, ItemStack target) {
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
