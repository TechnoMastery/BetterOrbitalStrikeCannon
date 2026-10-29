package net.minheur.betterosc.executors;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minheur.betterosc.Betterosc;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;

import static net.minheur.betterosc.executors.ExecutorsHelpers.*;

public final class BlueSkull {
    public static final String TYPE_FIXED = "wither_fixed";
    public static final String TYPE_MOBILE = "wither_mobile";

    @Contract("null, !null, _ -> fail; !null, null, _ -> fail; null, null, !null -> fail; !null, !null, null -> fail")
    public static @NonNull ItemStack create(@Nullable Integer x, @Nullable Integer z, @Nullable RegistryKey<World> world) {
        checkPositionedArgumentsNullStates(x, z, world);
        ItemStack stack = mkDefaultRod();
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Blue wither skull shot"));

        NbtCompound nbt = nullableXZNbt(x, z, world, TYPE_MOBILE, TYPE_FIXED);

        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        return stack;
    }
    public static @NonNull ItemStack create() {
        return create(null, null, null);
    }

    public static void spawn(@NonNull World world, int centerX, int centerZ) {
        // setup - tweaking these values to make it work
        float[] pitchOffsets = new float[]{2f,4f,6f,8f,10f,12f,14f,16f,18f,20f,22f,24f,26f,28f,30f,32f,34f,36f,38f,40f,42f,44f,46f,48f,50f};
        float yawStep = 2f;

        // FIND y VALUE
        int centerY = world.getTopY(Heightmap.Type.WORLD_SURFACE, centerX, centerZ) +35; // get top block & tempo

        // GET CENTER TNT
        WitherSkullEntity center = mk(world, centerX, centerY, centerZ); // no direction tweaking
        applyVelocity(center);
        world.spawnEntity(center); // SPAWN

        // first go through yaws - creating rings from looking down to up
        for (float pitchOffset : pitchOffsets) // then go for pitches - add the offset until we did 360°
            for (float yawOffset = 0f; yawOffset < 360.0f; yawOffset += yawStep) {
                Betterosc.LOGGER.info("pitch {} yaw {}", pitchOffset, yawOffset);
                // create skull
                WitherSkullEntity skull = mk(world, centerX, centerY, centerZ);
                applyOffsets(skull, pitchOffset, yawOffset);
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
    private static void applyOffsets(@NonNull WitherSkullEntity skull, float pitchOffset, float yawOffset) {
        float currentPitch = skull.getPitch();
        float currentYaw = skull.getYaw();

        float finalPitch = MathHelper.clamp(currentPitch - pitchOffset, // offset from the downward base angle
                -90.0F, 90.0F); // clamp to max up - max down
        float finalYaw = ((currentYaw + yawOffset) // add
                % 360.0F + 360.0F) % 360.0F; // normalize : -360° if > 360° (370° --> 10°)

        // apply
        skull.setYaw(finalYaw);
        skull.setPitch(finalPitch);
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
