package net.minheur.betterosc;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ChargedProjectilesComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minheur.betterosc.executors.*;

import java.util.UUID;

public final class ExecutorHandler {

    public static void registerUsage() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClient() || !(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;
            ItemStack stack = player.getStackInHand(hand);

            NbtComponent nbtCompo = stack.get(DataComponentTypes.CUSTOM_DATA);
            if (nbtCompo == null) return ActionResult.PASS;
            NbtCompound nbt = nbtCompo.copyNbt();

            String uuidString = nbt.getString("uuid").orElse(null);
            if (uuidString == null) return ActionResult.PASS;

            UUID uuid;
            try {
                uuid = UUID.fromString(uuidString);
            } catch (Exception e) {
                return ActionResult.PASS;
            }

            String type = nbt.getString("oc_type").orElse(null);
            if (type == null) return ActionResult.PASS;

            // CROSSBOW
            if (stack.getItem() == Items.CROSSBOW) {
                ChargedProjectilesComponent charged = stack.get(DataComponentTypes.CHARGED_PROJECTILES);
                if (charged == null || charged.isEmpty()) return ActionResult.PASS;

                switch (type) {
                    case Railgun.TYPE_STRAIGHT -> {
                        return Railgun.handle(serverPlayer, uuid, nbt, stack, hand);
                    }
                    default -> {
                        return ActionResult.PASS;
                    }
                }
            }

            // ROD
            if (stack.getItem() == Items.FISHING_ROD)
                return switch (type) {
                    case Nuke.TYPE_FIXED, Nuke.TYPE_MOBILE -> Nuke.handle(serverPlayer, uuid, nbt, hand, stack);
                    case Stab.TYPE_FIXED, Stab.TYPE_MOBILE -> Stab.handle(serverPlayer, uuid, nbt, hand, stack);
                    case TpStasis.TYPE_FIXED, TpStasis.TYPE_MOBILE ->
                            TpStasis.handle(serverPlayer, uuid, nbt, hand, stack);
                    case Wolves.TYPE_MOBILE /* , Wolves.TYPE_FIXED */ ->
                            Wolves.handle(serverPlayer, uuid, nbt, hand, stack);
                    case Darkness.TYPE -> Darkness.handle(serverPlayer, uuid, nbt, hand, stack);
                    case CrashPig.TYPE -> CrashPig.handle(serverPlayer, uuid, nbt, hand, stack);
                    default -> ActionResult.PASS;
                };

            return ActionResult.PASS;
        });
    }

}
