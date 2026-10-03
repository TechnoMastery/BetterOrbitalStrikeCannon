package net.minheur.betterosc.tank;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minheur.betterosc.ConfigHandler;
import org.jspecify.annotations.NonNull;

public final class IllegalTankItems {

    public static boolean isItemAllowed(@NonNull ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (TankHandler.isTank(stack)) return false;
        String id = Registries.ITEM.getId(stack.getItem()).toString();
        if (ConfigHandler.getConfig().illegalTanksItems.contains(id)) return false;

        NbtComponent nbtComponent = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (nbtComponent == null) return true;
        NbtCompound nbt = nbtComponent.copyNbt();

        String type = nbt.getString("oc_type").orElse(null);
        return type == null;
    }

}
