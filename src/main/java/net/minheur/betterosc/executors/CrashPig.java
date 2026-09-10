package net.minheur.betterosc.executors;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minheur.betterosc.Betterosc;
import net.minheur.betterosc.ConfigHandler;
import net.minheur.betterosc.UsedItemsHandler;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public final class CrashPig {
    public static final String TYPE = "crash_pig";

    public static ItemStack create(int x, int y, int z, Integer amount) {
        ItemStack stack = new ItemStack(Items.FISHING_ROD);
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Orbital CrashPig Cannon"));
        stack.set(DataComponentTypes.DAMAGE, stack.getMaxDamage() -1);
        NbtCompound nbt = new NbtCompound();
        nbt.putString("uuid", UUID.randomUUID().toString());
        if (amount != null) nbt.putInt("pig_amount", amount);
        nbt.putInt("x", x);
        nbt.putInt("y", y);
        nbt.putInt("z", z);
        nbt.putString("oc_type", TYPE);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        return stack;
    }
    public static ItemStack create(int x, int y, int z) {
        return create(x, y, z, null);
    }

    public static ActionResult handle(ServerPlayerEntity player, UUID itemUUID, NbtCompound nbt, Hand hand, ItemStack stack) {
        if (!UsedItemsHandler.getCastedFishingRods().contains(itemUUID)) {
            UsedItemsHandler.getCastedFishingRods().add(itemUUID);

            if (UsedItemsHandler.getUsedItems().contains(itemUUID)) return ActionResult.PASS;
            UsedItemsHandler.getUsedItems().add(itemUUID);

            int amount = nbt.getInt("pig_amount").orElse(ConfigHandler.getConfig().pigAmount);
            if (amount <= 0) amount = ConfigHandler.getConfig().pigAmount;
            final int finalAmount = amount;

            int x = nbt.getInt("x").orElse(player.getBlockX());
            int y = nbt.getInt("y").orElse(player.getBlockY());
            int z = nbt.getInt("z").orElse(player.getBlockZ());

            ServerWorld world = player.getEntityWorld();
            long delay = (long) (ConfigHandler.getConfig().rodCastDelay * 1000.0f);

            world.getServer().execute(() -> (new Timer()).schedule(new TimerTask() {
                @Override
                public void run() {
                    world.getServer().execute(() -> spawn(player, x, y, z, finalAmount));
                }
            }, delay));
            return ActionResult.PASS;
        } else {
            UsedItemsHandler.getCastedFishingRods().remove(itemUUID);
            Betterosc.breakFishingRod(player, hand, stack);
            return ActionResult.SUCCESS;
        }
    }

    public static void spawn(ServerPlayerEntity player, int x, int y, int z, int amount) {
        ServerWorld world = player.getEntityWorld();

        // spawn wolves
        for (int i = 0; i < amount; i++) {
            PigEntity pig = new PigEntity(EntityType.PIG, world);
            pig.setPosition(x, y, z);
            double angle = Math.random() * (double)2.0F * Math.PI;
            double pitch = (Math.random() - (double)0.5F) * Math.PI * (double)0.5F;
            double horizontalSpeed = 0.8;
            double vx = Math.cos(angle) * Math.cos(pitch) * horizontalSpeed;
            double vy = Math.sin(pitch) * 0.6 + 0.3;
            double vz = Math.sin(angle) * Math.cos(pitch) * horizontalSpeed;
            pig.setVelocity(vx, vy, vz);
            world.spawnEntity(pig);
        }
    }

}
