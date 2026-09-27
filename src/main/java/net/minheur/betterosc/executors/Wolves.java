package net.minheur.betterosc.executors;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
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
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public final class Wolves {
    public static final String TYPE_FIXED = "wolf_fixed";
    public static final String TYPE_MOBILE = "wolf_mobile";

    public static @NonNull ItemStack createFixed(@Nullable UUID target, int wolfAmount) {
        ItemStack stack = new ItemStack(Items.FISHING_ROD);
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Orbital Wolf"));
        stack.set(DataComponentTypes.DAMAGE, stack.getMaxDamage() -1);
        NbtCompound nbt = new NbtCompound();
        nbt.putString("uuid", UUID.randomUUID().toString());
        nbt.putInt("wolf_amount", wolfAmount);

        nbt.putString("oc_type", target == null ? TYPE_MOBILE : TYPE_FIXED);
        if (target != null) nbt.putString("target", target.toString());

        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        return stack;
    }
    public static @NonNull ItemStack createFixed(UUID target) {
        return createFixed(target, ConfigHandler.getConfig().defaultWolfAmount);
    }

    public static @NonNull ItemStack createMobile(int wolfAmount) {
        return createFixed(null, wolfAmount);
    }
    public static @NonNull ItemStack createMobile() {
        return createFixed(null);
    }

    public static ActionResult handle(ServerPlayerEntity player, UUID itemUUID, NbtCompound nbt, Hand hand, ItemStack stack, boolean mobile) {
        if (!UsedItemsHandler.getCastedFishingRods().contains(itemUUID)) {
            UsedItemsHandler.getCastedFishingRods().add(itemUUID);

            if (UsedItemsHandler.getUsedItems().contains(itemUUID)) return ActionResult.PASS;
            UsedItemsHandler.getUsedItems().add(itemUUID);

            int amount = nbt.getInt("wolf_amount").orElse(ConfigHandler.getConfig().defaultWolfAmount);
            if (amount <= 0) amount = ConfigHandler.getConfig().defaultWolfAmount;
            final int finalAmount = amount;

            ServerWorld world = player.getEntityWorld();
            long delay = (long) (ConfigHandler.getConfig().rodCastDelay * 1000.0f);

            ServerPlayerEntity target;
            if (mobile) target = player;
            else {
                UUID targetUUID = UUID.fromString(nbt.getString("target").orElse(player.getUuidAsString()));
                target = world.getServer().getPlayerManager().getPlayer(targetUUID);
            }
            final ServerPlayerEntity finalTarget = target == null ? player : target;

            world.getServer().execute(() -> (new Timer()).schedule(new TimerTask() {
                @Override
                public void run() {
                    world.getServer().execute(() -> spawn(finalTarget, finalAmount));
                }
            }, delay));
            return ActionResult.PASS;
        } else {
            UsedItemsHandler.getCastedFishingRods().remove(itemUUID);
            Betterosc.breakFishingRod(player, hand, stack);
            return ActionResult.SUCCESS;
        }
    }

    public static void spawn(@NonNull ServerPlayerEntity player, int amount) {
        ServerWorld world = player.getEntityWorld();
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();

        // spawn wolves
        for (int i = 0; i < amount; i++) {
            WolfEntity wolf = new WolfEntity(EntityType.WOLF, world);
            wolf.setPosition(x, y, z);
            wolf.setOwner(player);
            wolf.setTamed(true, true);
            ItemStack armor = new ItemStack(Items.WOLF_ARMOR);
            wolf.equipStack(EquipmentSlot.BODY, armor);
            wolf.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 3600, 1, false, true));
            wolf.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 3600, 1, false, true));
            wolf.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 9600, 0, false, true));
            double angle = Math.random() * (double)2.0F * Math.PI;
            double pitch = (Math.random() - (double)0.5F) * Math.PI * (double)0.5F;
            double horizontalSpeed = 0.8;
            double vx = Math.cos(angle) * Math.cos(pitch) * horizontalSpeed;
            double vy = Math.sin(pitch) * 0.6 + 0.3;
            double vz = Math.sin(angle) * Math.cos(pitch) * horizontalSpeed;
            wolf.setVelocity(vx, vy, vz);
            world.spawnEntity(wolf);
        }
    }
}
