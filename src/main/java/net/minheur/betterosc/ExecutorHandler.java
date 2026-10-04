package net.minheur.betterosc;

import com.google.gson.JsonElement;
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
import net.minheur.betterosc.recipes.Recipe;
import org.jetbrains.annotations.Contract;

import java.util.UUID;

public final class ExecutorHandler {

    @Contract("null, _ -> fail; _, null -> fail")
    public static ItemStack mkFromResultContainer(Recipe.ResultContainer result, ServerPlayerEntity crafter) {
        // handle null and default stack
        if (result == null) throw new IllegalArgumentException("Can't create result for null result container!");
        if (result.getIsByStack()) return result.getAsStack();

        if (crafter == null) throw new IllegalArgumentException("Can't have a null crafter player!");

        return switch (result.getId()) {
            case ConcentratedShot.TYPE_FIXED -> {
                int x = crafter.getBlockX();
                int z = crafter.getBlockZ();
                if (!result.getExtra().has("tntAmount"))
                    yield ConcentratedShot.create(x, z, null, crafter.getEntityWorld().getRegistryKey());
                int amount = result.getExtra().get("tntAmount").getAsInt();
                yield ConcentratedShot.create(x, z, amount, crafter.getEntityWorld().getRegistryKey());
            }
            case ConcentratedShot.TYPE_MOBILE -> {
                if (!result.getExtra().has("tntAmount")) yield ConcentratedShot.create(null);
                int amount = result.getExtra().get("tntAmount").getAsInt();
                yield ConcentratedShot.create(amount);
            }
            case CrashPig.TYPE -> {
                int x = crafter.getBlockX();
                int y = crafter.getBlockY();
                int z = crafter.getBlockZ();

                if (!result.getExtra().has("pigAmount"))
                    yield CrashPig.create(x, y, z, crafter.getEntityWorld().getRegistryKey());

                int amount = result.getExtra().get("pigAmount").getAsInt();
                yield CrashPig.create(x, y, z, crafter.getEntityWorld().getRegistryKey(), amount);
            }
            case Darkness.TYPE -> {
                JsonElement radius = result.getExtra().get("radius");
                JsonElement duration = result.getExtra().get("duration");
                if (radius == null && duration == null) yield Darkness.create();

                int intRadius = radius == null ? ConfigHandler.getConfig().darknessRadius : radius.getAsInt();
                int intDuration = duration == null ? ConfigHandler.getConfig().darknessDuration : duration.getAsInt();
                yield Darkness.create(intRadius, intDuration);
            }
            case Nuke.TYPE_FIXED -> {
                int ringAmount = result.getExtra().get("ringAmount").getAsInt();
                int x = crafter.getBlockX();
                int z = crafter.getBlockZ();

                yield Nuke.create(ringAmount, x, z, crafter.getEntityWorld().getRegistryKey());
            }
            case Nuke.TYPE_MOBILE -> {
                int ringAmount = result.getExtra().get("ringAmount").getAsInt();
                yield Nuke.create(ringAmount);
            }
            case Railgun.TYPE_STRAIGHT -> {
                JsonElement radius = result.getExtra().get("radius");
                if (radius == null) yield Railgun.createStraight();

                double intRadius = radius.getAsDouble();
                yield Railgun.createStraight(intRadius);
            }
            case Stab.TYPE_FIXED -> {
                int x = crafter.getBlockX();
                int z = crafter.getBlockZ();
                yield Stab.create(x, z, crafter.getEntityWorld().getRegistryKey());
            }
            case Stab.TYPE_MOBILE -> Stab.create();
            case TotemStasis.TYPE_FIXED -> {
                int x = crafter.getBlockX();
                int y = crafter.getBlockY();
                int z = crafter.getBlockZ();
                yield TotemStasis.createFixed(crafter.getUuid(), x, y, z, crafter.getEntityWorld().getRegistryKey());
            }
            case TotemStasis.TYPE_MOBILE -> {
                int x = crafter.getBlockX();
                int y = crafter.getBlockY();
                int z = crafter.getBlockZ();
                yield TotemStasis.createMobile(x, y, z, crafter.getEntityWorld().getRegistryKey());
            }
            case TpStasis.TYPE_FIXED -> {
                int x = crafter.getBlockX();
                int y = crafter.getBlockY();
                int z = crafter.getBlockZ();
                yield TpStasis.createFixed(crafter.getUuid(), x, y, z, crafter.getEntityWorld().getRegistryKey());
            }
            case TpStasis.TYPE_MOBILE -> {
                int x = crafter.getBlockX();
                int y = crafter.getBlockY();
                int z = crafter.getBlockZ();
                yield TpStasis.createMobile(x, y, z, crafter.getEntityWorld().getRegistryKey());
            }
            case Wolves.TYPE_MOBILE -> {
                JsonElement amount = result.getExtra().get("amount");
                if (amount == null) yield Wolves.createMobile();
                yield Wolves.createMobile(amount.getAsInt());
            }
            default -> throw new IllegalStateException("Did not found custom item type for " + result.getId());
        };
    }

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
                    case ConcentratedShot.TYPE_FIXED, ConcentratedShot.TYPE_MOBILE ->
                            ConcentratedShot.handle(serverPlayer, uuid, nbt, hand, stack);
                    case Nuke.TYPE_FIXED, Nuke.TYPE_MOBILE -> Nuke.handle(serverPlayer, uuid, nbt, hand, stack);
                    case Stab.TYPE_FIXED, Stab.TYPE_MOBILE -> Stab.handle(serverPlayer, uuid, nbt, hand, stack);
                    case TpStasis.TYPE_FIXED, TpStasis.TYPE_MOBILE ->
                            TpStasis.handle(serverPlayer, uuid, nbt, hand, stack);
                    case Wolves.TYPE_MOBILE -> Wolves.handle(serverPlayer, uuid, nbt, hand, stack, true);
                    case Wolves.TYPE_FIXED -> Wolves.handle(serverPlayer, uuid, nbt, hand, stack, false);
                    case Darkness.TYPE -> Darkness.handle(serverPlayer, uuid, nbt, hand, stack);
                    case CrashPig.TYPE -> CrashPig.handle(serverPlayer, uuid, nbt, hand, stack);
                    default -> ActionResult.PASS;
                };

            return ActionResult.PASS;
        });
    }

}
