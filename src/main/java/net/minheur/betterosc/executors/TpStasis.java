package net.minheur.betterosc.executors;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minheur.betterosc.Betterosc;
import net.minheur.betterosc.ConfigHandler;
import net.minheur.betterosc.UsedItemsHandler;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static net.minheur.betterosc.executors.ExecutorsHelpers.worldXYZNbt;

public final class TpStasis {
    public static final String TYPE_FIXED = "stasis_fixed";
    public static final String TYPE_MOBILE = "stasis_mobile";

    public static @NonNull ItemStack createFixed(@Nullable UUID target, int x, int y, int z, @NonNull RegistryKey<World> world) {
        ItemStack stack = new ItemStack(Items.FISHING_ROD);
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Stasis"));
        stack.set(DataComponentTypes.DAMAGE, stack.getMaxDamage() -1);

        NbtCompound nbt = worldXYZNbt(x, y, z, world, target == null ? TYPE_MOBILE : TYPE_FIXED);
        if (target != null) nbt.putString("target", target.toString());

        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        return stack;
    }
    public static @NonNull ItemStack createMobile(int x, int y, int z, @NonNull RegistryKey<World> world) {
        return createFixed(null, x, y, z, world);
    }

    public static ActionResult handle(ServerPlayerEntity player, UUID itemUUID, NbtCompound nbt, Hand hand, ItemStack stack) {
        if (!UsedItemsHandler.getCastedFishingRods().contains(itemUUID)) {
            UsedItemsHandler.getCastedFishingRods().add(itemUUID);

            if (UsedItemsHandler.getUsedItems().contains(itemUUID)) return ActionResult.PASS;
            UsedItemsHandler.getUsedItems().add(itemUUID);

            ServerWorld world = player.getEntityWorld();
            MinecraftServer server = world.getServer();

            Integer x = nbt.getInt("x").orElse(null);
            Integer y = nbt.getInt("y").orElse(null);
            Integer z = nbt.getInt("z").orElse(null);

            String worldKey = nbt.getString("dim", null);
            if (worldKey == null) return ActionResult.PASS;
            ServerWorld targetWorld = server.getWorld(RegistryKey.of(
                    RegistryKeys.WORLD,
                    Identifier.of(worldKey)
            ));

            String type = nbt.getString("oc_type").orElse("");

            ServerPlayerEntity tpTarget = switch (type) {
                case TYPE_FIXED -> {
                    String targetUUID = nbt.getString("target").orElse(null);
                    if (targetUUID == null) yield player;
                    yield server.getPlayerManager().getPlayer(UUID.fromString(targetUUID));
                }
                case TYPE_MOBILE -> player;
                default -> null;
            };

            if (tpTarget == null || targetWorld == null) return ActionResult.PASS;

            HitResult hitResults = player.raycast(256.0f, 0.0f, false);
            if (hitResults.getType() == HitResult.Type.BLOCK) {
                BlockPos tpPos = ((BlockHitResult) hitResults).getBlockPos();
                if (x == null) x = tpPos.getX();
                if (y == null) y = tpPos.getY();
                if (z == null) z = tpPos.getZ();
            } else if (x == null || y == null || z == null) return ActionResult.PASS;

            Integer finalX = x, finalY = y, finalZ = z;

            long delay = (long) (ConfigHandler.getConfig().rodCastDelay * 1000.0f);
            server.execute(() -> new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    server.execute(() -> tp(targetWorld, tpTarget, finalX, finalY, finalZ));
                }
            }, delay));

            return ActionResult.PASS;
        } else {
            UsedItemsHandler.getCastedFishingRods().remove(itemUUID);
            Betterosc.breakOrbitalCallItem(player, hand, stack);
            return ActionResult.SUCCESS;
        }
    }

    public static void tp(@NonNull ServerWorld targetWorld, @NonNull ServerPlayerEntity player, int x, int y, int z) {
        float yaw = player.getYaw();
        float pitch = player.getPitch();
        Vec3d currentPos = new Vec3d(player.getX(), player.getY(), player.getZ());
        player.getEntityWorld().playSound(null, currentPos.x, currentPos.y, currentPos.z, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
        player.getEntityWorld().spawnParticles(ParticleTypes.PORTAL, currentPos.x, currentPos.y + (double) 1.0F, currentPos.z, 50, 0.5F, 0.5F, 0.5F, 0.5F);
        player.teleport(targetWorld, (double)x + (double)0.5F, y, (double)z + (double)0.5F, Set.of(), yaw, pitch, true);
        targetWorld.playSound(null, (double)x + (double)0.5F, y, (double)z + (double)0.5F, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
        targetWorld.spawnParticles(ParticleTypes.PORTAL, (double)x + (double)0.5F, y + 1, (double)z + (double)0.5F, 50, 0.5F, 0.5F, 0.5F, 0.5F);
    }
}
