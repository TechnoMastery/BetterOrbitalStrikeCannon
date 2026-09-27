package net.minheur.betterosc.executors;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public final class TotemStasis {
    public static final String TYPE_FIXED = "totem_fixed";
    public static final String TYPE_MOBILE = "totem_mobile";

    public static @NonNull ItemStack createFixed(@Nullable UUID target, int x, int y, int z) {
        ItemStack stack = new ItemStack(Items.TOTEM_OF_UNDYING);
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Totem Stasis"));

        NbtCompound nbt = new NbtCompound();
        nbt.putString("uuid", UUID.randomUUID().toString());
        nbt.putString("oc_type", target == null ? TYPE_MOBILE : TYPE_FIXED);
        nbt.putInt("x", x);
        nbt.putInt("y", y);
        nbt.putInt("z", z);
        if (target != null) nbt.putString("target", target.toString());

        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        return stack;
    }
    public static @NonNull ItemStack createMobile(int x, int y, int z) {
        return createFixed(null, x, y, z);
    }

}
