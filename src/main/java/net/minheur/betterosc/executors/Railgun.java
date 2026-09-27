package net.minheur.betterosc.executors;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ChargedProjectilesComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.projectile.ArrowEntity;
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
import net.minecraft.util.math.Vec3d;
import net.minheur.betterosc.ConfigHandler;
import net.minheur.betterosc.UsedItemsHandler;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public final class Railgun {
    public static final String TYPE_STRAIGHT = "railgun_straight";
    private static final Random random = new Random();

    public static @NonNull ItemStack createStraight(@Nullable Double radius) {
        ItemStack stack = new ItemStack(Items.CROSSBOW);
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Railgun"));
        stack.setDamage(stack.getMaxDamage() -1);

        ItemStack arrow = new ItemStack(Items.ARROW);
        stack.set(DataComponentTypes.CHARGED_PROJECTILES, ChargedProjectilesComponent.of(arrow));

        NbtCompound nbt = new NbtCompound();
        nbt.putString("uuid", UUID.randomUUID().toString());
        nbt.putString("oc_type", TYPE_STRAIGHT);
        nbt.putDouble("radius", radius == null ? ConfigHandler.getConfig().railgunSpreadRadius : radius);

        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        return stack;
    }
    public static @NonNull ItemStack createStraight() {
        return createStraight(null);
    }

    public static ActionResult handle(ServerPlayerEntity player, UUID uuid, NbtCompound nbt, @NonNull ItemStack stack, Hand hand) {
        UsedItemsHandler.getUsedItems().add(uuid);

        ChargedProjectilesComponent charged = stack.get(DataComponentTypes.CHARGED_PROJECTILES);
        if (charged == null || charged.isEmpty()) return ActionResult.PASS;

        double radius = nbt.getDouble("railgun_radius").orElse((double) 1.0F);
        if (radius <= 0.0F)
            radius = 1.0F;
        final double finalRadius = radius;

        HitResult result = player.raycast(10000.0F, 0.0F, false);
        if (result.getType() != HitResult.Type.BLOCK) return ActionResult.PASS;

        ServerWorld serverWorld = player.getEntityWorld();

        Vec3d eyeCopy = player.getEyePos();
        Vec3d directionCopy = player.getRotationVec(1.0f);

        long delay = (long) (ConfigHandler.getConfig().railgunDelay * 1000.0f);
        serverWorld.getServer().execute(() -> new Timer().schedule(new TimerTask() {
            public void run() {
                serverWorld.getServer().execute(() -> fireStraight(player, eyeCopy, directionCopy, finalRadius));
            }
        }, delay));

        stack.set(DataComponentTypes.CHARGED_PROJECTILES, ChargedProjectilesComponent.DEFAULT);
        player.sendEquipmentBreakStatus(stack.getItem(), hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);

        if (hand == Hand.MAIN_HAND) player.getInventory().setStack(player.getInventory().getSlotWithStack(stack), ItemStack.EMPTY);
        else player.getInventory().setStack(player.getInventory().size() - 1, ItemStack.EMPTY);

        return ActionResult.CONSUME;
    }

    public static void fireStraight(@NonNull ServerPlayerEntity player, Vec3d playerEye, Vec3d direction, double spreadRadius) {
        ServerWorld world = player.getEntityWorld();
        double velocity = ConfigHandler.getConfig().railgunArrowVelocity;

        for (int i = 0; i < 45; i++) {
            double offsetX = (random.nextDouble() - 0.5f) * spreadRadius * 2.0f;
            double offsetY = (random.nextDouble() - 0.5f) * spreadRadius * 2.0f;
            double offsetZ = (random.nextDouble() - 0.5f) * spreadRadius * 2.0f;
            Vec3d spawnPos = playerEye.add(direction.multiply(3.0f));
            spawnPos = spawnPos.add(offsetX, offsetY, offsetZ);
            ArrowEntity arrow = new ArrowEntity(world, spawnPos.x, spawnPos.y, spawnPos.z, player.getActiveItem(), null);
            arrow.setVelocity(direction.multiply(velocity));
            arrow.setOwner(player);
            arrow.setCritical(true);
            arrow.setDamage(1500.0f);
            arrow.setOnFireFor(Integer.MAX_VALUE);
            world.spawnEntity(arrow);
        }
    }
}
