package net.minheur.betterosc.executors;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.TntEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.Heightmap;
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

import static net.minheur.betterosc.executors.ExecutorsHelpers.*;

public class ConcentratedShot {
    public static final String TYPE_FIXED = "concentrated_fixed";
    public static final String TYPE_MOBILE = "concentrated_mobile";

    @Contract("null, !null, _, _ -> fail; !null, null, _, _ -> fail; null, null, _, !null -> fail; !null, !null, _, null -> fail")
    public static @NonNull ItemStack create(@Nullable Integer x, @Nullable Integer z, @Nullable Integer amount, @Nullable RegistryKey<World> world) {
        // check null states
        checkPositionedArgumentsNullStates(x, z, world);
        ItemStack stack = mkDefaultRod();
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Concentrated Shot"));

        NbtCompound nbt = ExecutorsHelpers.nullableXZNbt(x, z, world, TYPE_MOBILE, TYPE_FIXED);
        if (amount != null) nbt.putInt("amount", amount);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        return stack;
    }
    public static @NonNull ItemStack create(@Nullable Integer amount) {
        return create(null, null, amount, null);
    }

    public static ActionResult handle(ServerPlayerEntity player, UUID itemUUID, NbtCompound nbt, Hand hand, ItemStack stack) {
        if (!UsedItemsHandler.getCastedFishingRods().contains(itemUUID)) {
            UsedItemsHandler.getCastedFishingRods().add(itemUUID);

            if (UsedItemsHandler.getUsedItems().contains(itemUUID)) return ActionResult.PASS;
            UsedItemsHandler.getUsedItems().add(itemUUID);

            ServerWorld world = player.getEntityWorld();
            if (world.getServer() == null) return ActionResult.PASS;

            ResultXZDim resultXZDim = getXZDim(nbt, world, player);
            if (resultXZDim.returnsPass()) return ActionResult.PASS;
            int x = resultXZDim.x(), z = resultXZDim.z();
            ServerWorld targetWorld = resultXZDim.targetWorld();
            if (targetWorld == null) return ActionResult.PASS;

            int tntAmount = nbt.getInt("amount", ConfigHandler.getConfig().concentratedTnts);

            int y = targetWorld.getTopY(Heightmap.Type.WORLD_SURFACE, x, z) +1;

            long delay = (long) (ConfigHandler.getConfig().rodCastDelay * 1000.0f);

            world.getServer().execute(() -> new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    assert world.getServer() != null;
                    world.getServer().execute(() -> spawn(targetWorld, x, y, z, tntAmount));
                }
            }, delay));

            return ActionResult.PASS;

        } else {
            UsedItemsHandler.getCastedFishingRods().remove(itemUUID);
            Betterosc.breakOrbitalCallItem(player, hand, stack);
            return ActionResult.SUCCESS;
        }
    }

    public static void spawn(@NonNull World world, int x, int y, int z, int amount) {
        for (int i = 0; i < amount; i++) {
            TntEntity tnt = new TntEntity(world, x, y, z, null);
            tnt.setFuse(10);
            world.spawnEntity(tnt);
        }
    }

}
