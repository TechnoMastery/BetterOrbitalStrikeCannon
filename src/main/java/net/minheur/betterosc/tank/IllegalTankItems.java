package net.minheur.betterosc.tank;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;

import java.util.ArrayList;
import java.util.List;

public final class IllegalTankItems {
    private static final List<Item> illegalItems = new ArrayList<>();

    static {
        illegalItems.add(Items.BEDROCK);
        illegalItems.add(Items.BARRIER);
    }

    public static boolean isItemAllowed(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (TankHandler.isTank(stack)) return false;
        if (illegalItems.contains(stack.getItem())) return false;

        NbtComponent nbtComponent = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (nbtComponent == null) return true;
        NbtCompound nbt = nbtComponent.copyNbt();

        String type = nbt.getString("oc_type").orElse(null);
        return type == null;
    }

}
