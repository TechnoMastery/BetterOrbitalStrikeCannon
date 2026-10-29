package net.minheur.betterosc.executors;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
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

public final class BlueSkull {
    public static final String TYPE_FIXED = "wither_fixed";
    public static final String TYPE_MOBILE = "wither_mobile";

    @Contract("null, !null, _, _ -> fail; !null, null, _, _ -> fail; null, null, !null, _ -> fail; !null, !null, null, _ -> fail")
    public static @NonNull ItemStack create(@Nullable Integer x, @Nullable Integer z, @Nullable RegistryKey<World> world, @Nullable Integer skullAmount) {
        checkPositionedArgumentsNullStates(x, z, world);
        ItemStack stack = mkDefaultRod();
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Blue wither skull shot"));

        NbtCompound nbt = nullableXZNbt(x, z, world, TYPE_MOBILE, TYPE_FIXED);
        if (skullAmount != null) nbt.putInt("skullAmount", skullAmount);

        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        return stack;
    }
    public static @NonNull ItemStack create() {
        return create(null, null, null, null);
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

            Integer skullAmount = nbt.getInt("skullAmount").orElse(ConfigHandler.getConfig().skullAmount);
            long delay = (long) (ConfigHandler.getConfig().rodCastDelay * 1000.0f);

            MinecraftServer server = world.getServer();
            if (server == null) return ActionResult.PASS;

            server.execute(() -> new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    server.execute(() -> spawn(targetWorld, x, z, skullAmount));
                }
            }, delay));
            return ActionResult.PASS;
        } else {
            UsedItemsHandler.getCastedFishingRods().remove(itemUUID);
            Betterosc.breakOrbitalCallItem(player, hand, stack);
            return ActionResult.SUCCESS;
        }
    }

    public static void spawn(@NonNull World world, int centerX, int centerZ, int skullCount) {
        // FIND y VALUE
        int centerY = world.getTopY(Heightmap.Type.WORLD_SURFACE, centerX, centerZ) +35; // get top block & tempo

        double goldenAngle = Math.PI * (3.0 - Math.sqrt(5.0));

        for (int i = 0; i < skullCount; i++) {
            WitherSkullEntity skull = mk(world, centerX, centerY, centerZ);

            // y varie de presque 0 à presque -1 : hémisphère inférieur
            double y = -(i + 0.5) / skullCount;
            double r = Math.sqrt(1.0 - y * y);
            double phi = i * goldenAngle;

            double x = r * Math.cos(phi);
            double z = r * Math.sin(phi);

            // Orientation Minecraft correspondant au vecteur (x, y, z)
            float yaw = (float) Math.toDegrees(Math.atan2(-x, z));
            float pitch = (float) Math.toDegrees(
                    Math.atan2(-y, Math.sqrt(x * x + z * z))
            );

            skull.setYaw(yaw);
            skull.setPitch(pitch);
            applyVelocity(skull);
            world.spawnEntity(skull);
        }

    }

    @Contract("_,_,_,_ -> new")
    private static @NonNull WitherSkullEntity mk(World world, double centerX, double centerY, double centerZ) {
        WitherSkullEntity w = new WitherSkullEntity(
                EntityType.WITHER_SKULL,
                world
        );
        w.setCharged(true);
        w.setPos(centerX, centerY, centerZ);
        w.setAngles(180.0F, 90.0F);
        return w;
    }
    private static void applyVelocity(@NonNull WitherSkullEntity skull) {
        float yaw = skull.getYaw();
        float pitch = skull.getPitch();
        Vec3d direction = Vec3d.fromPolar(pitch, yaw).normalize();

        skull.setVelocity(
                direction.x, direction.y, direction.z,
                0.1f, 0.0f
        );
    }

}
