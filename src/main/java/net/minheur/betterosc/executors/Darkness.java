package net.minheur.betterosc.executors;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minheur.betterosc.Betterosc;
import net.minheur.betterosc.ConfigHandler;
import net.minheur.betterosc.UsedItemsHandler;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public final class Darkness {
    public static final String TYPE = "darkness";

    public static ItemStack create(int radius, int timeSeconds) {
        ItemStack stack = new ItemStack(Items.FISHING_ROD);
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Darkness rod"));
        stack.set(DataComponentTypes.DAMAGE, stack.getMaxDamage() -1);

        NbtCompound nbt = new NbtCompound();
        nbt.putString("uuid", UUID.randomUUID().toString());
        nbt.putString("oc_type", TYPE);

        nbt.putInt("radius", radius);
        nbt.putInt("duration", timeSeconds);

        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        return stack;
    }

    public static ActionResult handle(ServerPlayerEntity player, UUID itemUUID, NbtCompound nbt, Hand hand, ItemStack stack) {
        if (!UsedItemsHandler.getCastedFishingRods().contains(itemUUID)) {
            UsedItemsHandler.getCastedFishingRods().add(itemUUID);

            if (UsedItemsHandler.getUsedItems().contains(itemUUID)) return ActionResult.PASS;
            UsedItemsHandler.getUsedItems().add(itemUUID);

            Integer radius = nbt.getInt("radius").orElse(null);
            Integer duration = nbt.getInt("duration").orElse(null);

            if (radius == null || duration == null) return ActionResult.PASS;

            long delay = (long) (ConfigHandler.getConfig().rodCastDelay * 1000.0f);
            player.getEntityWorld().getServer().execute(() -> new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    player.getEntityWorld().getServer().execute(() -> apply(player, radius, duration));
                }
            }, delay));

            return ActionResult.PASS;
        } else {
            UsedItemsHandler.getCastedFishingRods().remove(itemUUID);
            Betterosc.breakFishingRod(player, hand, stack);
            return ActionResult.SUCCESS;
        }
    }

    public static void apply(ServerPlayerEntity user, int radius, int duration) {
        for (ServerPlayerEntity player : user.getEntityWorld().getPlayers()) {
            if (player == user) continue;

            if (player.squaredDistanceTo(user) <= radius * radius)
                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.DARKNESS,
                        duration * 20,
                        0
                ));
        }
    }

}
