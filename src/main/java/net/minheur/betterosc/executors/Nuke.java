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

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static net.minheur.betterosc.executors.ExecutorsHelpers.*;

public final class Nuke {
    public static final String TYPE_FIXED = "nuke_fixed";
    public static final String TYPE_MOBILE = "nuke_mobile";

    @Contract("_, null, !null, _ -> fail; _, !null, null, _ -> fail; _, null, null, !null -> fail; _, !null, !null, null -> fail")
    public static @NonNull ItemStack create(int ringAmount, @Nullable Integer x, @Nullable Integer z, @Nullable RegistryKey<World> world) {
        // check null states
        checkPositionedArgumentsNullStates(x, z, world);
        ItemStack stack = mkDefaultRod();
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Nuke shot"));

        NbtCompound nbt = nullableXZNbt(x, z, world, TYPE_MOBILE, TYPE_FIXED);
        nbt.putInt("ring_amount", ringAmount);

        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        return stack;
    }
    public static @NonNull ItemStack create(int ringAmount) {
        return create(ringAmount, null, null, null);
    }

    public static ActionResult handle(ServerPlayerEntity player, UUID itemUUID, NbtCompound nbt, Hand hand, ItemStack stack) {
        if (!UsedItemsHandler.getCastedFishingRods().contains(itemUUID)) {
            UsedItemsHandler.getCastedFishingRods().add(itemUUID);

            if (UsedItemsHandler.getUsedItems().contains(itemUUID)) return ActionResult.PASS;
            UsedItemsHandler.getUsedItems().add(itemUUID);

            ServerWorld world = player.getEntityWorld();

            ResultXZDim resultXZDim = getXZDim(nbt, world, player);
            if (resultXZDim.returnsPass()) return ActionResult.PASS;
            int x = resultXZDim.x(), z = resultXZDim.z();
            ServerWorld targetWorld = resultXZDim.targetWorld();

            Integer ringAmount = nbt.getInt("ring_amount").orElse(null);
            if (ringAmount == null) return ActionResult.PASS;

            long delay = (long) (ConfigHandler.getConfig().rodCastDelay * 1000.0f);

            world.getServer().execute(() -> new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    world.getServer().execute(() -> spawn(targetWorld, x, z, ringAmount));
                }
            }, delay));

            return ActionResult.PASS;

        } else {
            UsedItemsHandler.getCastedFishingRods().remove(itemUUID);
            Betterosc.breakOrbitalCallItem(player, hand, stack);
            return ActionResult.SUCCESS;
        }
    }

    public static void spawn(World world, int centerX, int centerZ, int ringAmount) {
        // SETUP
        int fuse = 80;
        double gravity = -0.03;
        double velocityMultiplier = 1.4;
        int[] baseRadii = new int[]{12, 22, 32, 42, 52, 62, 72, 82, 92, 102};
        int[] radii = ringAmount <= baseRadii.length ? Arrays.copyOf(baseRadii, ringAmount) : new int[ringAmount];
        if (ringAmount > baseRadii.length) {
            System.arraycopy(baseRadii, 0, radii, 0, baseRadii.length);

            for(int i = baseRadii.length; i < ringAmount; ++i) {
                radii[i] = 98 + (i - baseRadii.length + 1) * 10;
            }
        }

        // FIND y VALUE
        int centerY = world.getTopY(Heightmap.Type.WORLD_SURFACE, centerX, centerZ) +71; // get top block & tempo

        // GET CENTER TNT
        TntEntity center = new TntEntity(world, (double) centerX +.5, centerY, (double) centerZ +.5, null);
        center.setFuse(fuse);
        center.setVelocity(0.0f, gravity, 0.0f);
        world.spawnEntity(center); // SPAWN

        // SPAWN RINGS
        for (int ringIndex = 0; ringIndex < radii.length; ringIndex++) {
            int radius = radii[ringIndex];
            // tnt amount getter...
            int tntsPerRing;
            if (ringIndex == 0) tntsPerRing = 84;
            else if (ringIndex == 1) tntsPerRing = 50;
            else if (ringIndex <= 5) tntsPerRing = 70;
            else if (ringIndex == 6) tntsPerRing = 90;
            else tntsPerRing = Math.min(100, Math.round((float)(radius * 2) - (float)ringIndex * 1.2F));

            // SPAWN TNT OF RING
            for (int i = 0; i < tntsPerRing; i++) {
                double angle = Math.random() * (double) 2.0F * Math.PI;
                double dx = Math.cos(angle);
                double dz = Math.sin(angle);
                double vx = dx * ((double) radius / (double) fuse) * velocityMultiplier;
                double vz = dz * ((double) radius / (double) fuse) * velocityMultiplier;
                TntEntity tnt = new TntEntity(world, (double)centerX + (double)0.5F, centerY, (double)centerZ + (double)0.5F, null);
                tnt.setFuse(fuse);
                tnt.setVelocity(vx, gravity, vz);
                world.spawnEntity(tnt);
            }
        }

    }
}
