package net.minheur.betterosc.tank;

import net.minecraft.item.ItemStack;

import java.util.UUID;

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

}
