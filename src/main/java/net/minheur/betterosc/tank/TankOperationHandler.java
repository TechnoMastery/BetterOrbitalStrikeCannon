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

    public enum OperationResults {
        SUCCESS,
        TOO_MUCH,
        NOT_ENOUGH,
        EMPTIED
    }

}
