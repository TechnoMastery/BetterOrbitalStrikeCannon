package net.minheur.betterosc.executors;

import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
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
    static void checkPositionedArgumentsNullStates(@Nullable Integer x, @Nullable Integer z, @Nullable RegistryKey<World> world) {
        if (x == null && (z != null || world != null)) throw new IllegalArgumentException("Both x, z and world should be either null or non-null !");
        if (z == null && x != null) throw new IllegalArgumentException("Both x, z and world should be either null or non-null !");
        if (world == null && x != null) throw new IllegalArgumentException("Both x, z and world should be either null or non-null !");

        new ItemStack(Items.FISHING_ROD);
    }
    static @NonNull ItemStack mkDefaultRod() {
        ItemStack stack = new ItemStack(Items.FISHING_ROD);
        stack.set(DataComponentTypes.DAMAGE, stack.getMaxDamage() -1);
        return stack;
    }

    @Contract("_,_,_,_,_ -> new")
    static @NonNull NbtCompound nullableXZNbt(Integer x, Integer z, RegistryKey<World> world, String typeMobile, String typeFixed) {
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

    static @NonNull NbtCompound worldXYZNbt(int x, int y, int z, @NonNull RegistryKey<World> world, @NonNull String type) {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("uuid", UUID.randomUUID().toString());
        nbt.putString("oc_type", type);
        nbt.putInt("x", x);
        nbt.putInt("y", y);
        nbt.putInt("z", z);
        nbt.putString("dim", world.getValue().toString());
        return nbt;
    }

    static int getHighestNonExplosive(@NonNull World world, int x, int z) {
        for (int block = world.getHeight() -1; block >= world.getBottomY(); block--) {
            BlockPos pos = new BlockPos(x, block, z);
            BlockState state = world.getBlockState(pos);

            if (state.getBlock().getBlastResistance() >= 1200.0f)
                return block +1;
        }
        return world.getBottomY();
    }

    @Contract("_, _, _ -> new")
    static @NonNull ResultXZDim getXZDim(@NonNull NbtCompound nbt, ServerWorld world, ServerPlayerEntity player) {
        int x, z;
        ServerWorld targetWorld;

        if (nbt.getInt("x").isPresent()) {
            x = nbt.getInt("x").orElseThrow();
            z = nbt.getInt("z").orElseThrow();

            String targetWorldKeyString = nbt.getString("dim", null);
            RegistryKey<World> key = RegistryKey.of(
                    RegistryKeys.WORLD,
                    Identifier.of(targetWorldKeyString)
            );
            MinecraftServer server = world.getServer();
            if (server == null) return ResultXZDim.getPass();
            targetWorld = server.getWorld(key);
        } else {
            HitResult hitResult = player.raycast(500.0f, 0.0f, false);
            if (hitResult.getType() != HitResult.Type.BLOCK) return ResultXZDim.getPass();
            BlockPos hitBlock = ((BlockHitResult) hitResult).getBlockPos();

            x = hitBlock.getX();
            z = hitBlock.getZ();
            targetWorld = world;
        }

        return new ResultXZDim(x, z, targetWorld, false);
    }

    public record ResultXZDim(int x, int z, ServerWorld targetWorld, boolean returnsPass) {
        @Contract(" -> new")
        public static @NonNull ResultXZDim getPass() {
            return new ResultXZDim(0, 0, null, true);
        }
    }

}
