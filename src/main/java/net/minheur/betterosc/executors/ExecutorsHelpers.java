package net.minheur.betterosc.executors;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public final class ExecutorsHelpers {

    @Contract("null, !null, _ -> fail; null, null, !null -> fail; !null, null, _ -> fail; !null, !null, null -> fail")
    static @NonNull ItemStack checkPositionedArgumentsNullStates(@Nullable Integer x, @Nullable Integer z, @Nullable RegistryKey<World> world) {
        if (x == null && (z != null || world != null)) throw new IllegalArgumentException("Both x, z and world should be either null or non-null !");
        if (z == null && x != null) throw new IllegalArgumentException("Both x, z and world should be either null or non-null !");
        if (world == null && x != null) throw new IllegalArgumentException("Both x, z and world should be either null or non-null !");

        return new ItemStack(Items.FISHING_ROD);
    }
    static @NonNull ItemStack mkDefaultRod() {
        ItemStack stack = new ItemStack(Items.FISHING_ROD);
        stack.set(DataComponentTypes.DAMAGE, stack.getMaxDamage() -1);
        return stack;
    }

    @Contract("_,_,_,_,_ -> new")
    static @NonNull NbtCompound mkPositionedNbt(Integer x, Integer z, RegistryKey<World> world, String typeMobile, String typeFixed) {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("uuid", UUID.randomUUID().toString());

        if (x == null)
            nbt.putString("oc_type", typeMobile);
        else {
            nbt.putString("oc_type", typeFixed);
            nbt.putInt("x", x);
            nbt.putInt("z", z);
            nbt.putString("dim", world.getValue().toString());
        }

        return nbt;
    }

}
