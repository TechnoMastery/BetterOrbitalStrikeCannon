package net.minheur.betterosc.executors;

import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.TntEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minheur.betterosc.Betterosc;
import net.minheur.betterosc.ConfigHandler;
import net.minheur.betterosc.UsedItemsHandler;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public final class Stab {
    public static final String TYPE_FIXED = "stab_fixed";
    public static final String TYPE_MOBILE = "stab_mobile";

    @Contract("null, !null -> fail; !null, null -> fail")
    public static @NonNull ItemStack create(@Nullable Integer x, @Nullable Integer z) {
        // check null states
        if (x == null && z != null) throw new IllegalArgumentException("Both x and z should be either null or non-null !");
        if (x != null && z == null) throw new IllegalArgumentException("Both x and z should be either null or non-null !");

        ItemStack stack = new ItemStack(Items.FISHING_ROD);
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Stab Shot"));
        stack.set(DataComponentTypes.DAMAGE, stack.getMaxDamage() -1);

        NbtCompound nbt = new NbtCompound();
        nbt.putString("uuid", UUID.randomUUID().toString());

        if (x == null)
            nbt.putString("oc_type", TYPE_MOBILE);
        else {
            nbt.putString("oc_type", TYPE_FIXED);
            nbt.putInt("x", x);
            nbt.putInt("z", z);
        }

        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        return stack;
    }
    public static @NonNull ItemStack create() {
        return create(null, null);
    }

    public static ActionResult handle(ServerPlayerEntity player, UUID itemUUID, NbtCompound nbt, Hand hand, ItemStack stack) {
        if (!UsedItemsHandler.getCastedFishingRods().contains(itemUUID)) {
            UsedItemsHandler.getCastedFishingRods().add(itemUUID);

            if (UsedItemsHandler.getUsedItems().contains(itemUUID)) return ActionResult.PASS;
            UsedItemsHandler.getUsedItems().add(itemUUID);

            int x;
            int z;

            if (nbt.getInt("x").isPresent()) {
                x = nbt.getInt("x").orElseThrow();
                z = nbt.getInt("z").orElseThrow();
            } else {
                HitResult hitResult = player.raycast(500.0f, 0.0f, false);
                if (hitResult.getType() != HitResult.Type.BLOCK) return ActionResult.PASS;
                BlockPos hitBlock = ((BlockHitResult) hitResult).getBlockPos();

                x = hitBlock.getX();
                z = hitBlock.getZ();
            }

            ServerWorld world = player.getEntityWorld();
            long delay = (long) (ConfigHandler.getConfig().rodCastDelay * 1000.0f);

            world.getServer().execute(() -> new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    world.getServer().execute(() -> spawn(world, x, z));
                }
            }, delay));

            return ActionResult.PASS;

        } else {
            UsedItemsHandler.getCastedFishingRods().remove(itemUUID);
            Betterosc.breakFishingRod(player, hand, stack);
            return ActionResult.SUCCESS;
        }
    }

    public static void spawn(@NonNull World world, int centerX, int centerZ) {
        Integer minY = null;
        for (int y = world.getHeight() -1; y >= world.getBottomY(); y--) {
            BlockPos pos = new BlockPos(centerX, y, centerZ);
            BlockState state = world.getBlockState(pos);

            if (state.getBlock().getBlastResistance() >= 1200.0f) {
                minY = y +1;
                break;
            }
        }
        if (minY == null) minY = world.getBottomY();
        int maxY = world.getHeight() -1;

        // SPAWN
        for (int y = maxY; y >= minY; y -=2) {
            BlockPos pos = new BlockPos(centerX, y, centerZ);
            int amount = world.getBlockState(pos).isAir() ? 1 : 2;
            for(int i = 0; i < amount; ++i) {
                TntEntity tnt = new TntEntity(world, (double)centerX + (double)0.5F, y, (double)centerZ + (double)0.5F, null);
                tnt.setFuse(0);
                world.spawnEntity(tnt);
            }
        }
    }
}
