package net.minheur.betterosc.plugin;

import com.google.gson.JsonElement;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minheur.betterosc.Betterosc;
import net.minheur.betterosc.ConfigHandler;
import net.minheur.betterosc.executors.*;
import net.minheur.commandcrafter.misc.Registry;
import net.minheur.commandcrafter.recipes.Recipe;
import org.jetbrains.annotations.Contract;

public class CommandCrafterPlugin {

    public CommandCrafterPlugin() { // instantiate to call plugin-only code - if dep not present + instantiate = crash
        Registry.registerExecutor(Betterosc.MODID, this::mkFromResultContainer); // register pi creator
    }

    @Contract("null, _ -> fail; _, null -> fail")
    public ItemStack mkFromResultContainer(Recipe.ResultContainer result, ServerPlayerEntity crafter) {
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
            case BlueSkull.TYPE_FIXED -> {
                JsonElement skullAmountEle = result.getExtra().get("skullAmount");
                JsonElement maxSkullAngleEle = result.getExtra().get("skullAngle");
                Integer skullAmount = skullAmountEle == null ? null : skullAmountEle.getAsInt();
                Double maxSkullAngle = maxSkullAngleEle == null ? null : maxSkullAngleEle.getAsDouble();

                int x = crafter.getBlockX();
                int z = crafter.getBlockZ();
                yield BlueSkull.create(x, z, crafter.getEntityWorld().getRegistryKey(),
                        skullAmount, maxSkullAngle);
            }
            case BlueSkull.TYPE_MOBILE -> {
                JsonElement skullAmountEle = result.getExtra().get("skullAmount");
                JsonElement maxSkullAngleEle = result.getExtra().get("skullAngle");
                Integer skullAmount = skullAmountEle == null ? null : skullAmountEle.getAsInt();
                Double maxSkullAngle = maxSkullAngleEle == null ? null : maxSkullAngleEle.getAsDouble();

                if (skullAmount == null && maxSkullAngle == null) yield BlueSkull.create();
                yield BlueSkull.create(null, null, null,
                        skullAmount, maxSkullAngle);
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
            case Wolves.TYPE_FIXED ->  {
                JsonElement amount = result.getExtra().get("amount");
                if (amount == null) yield Wolves.createFixed(crafter.getUuid());
                yield Wolves.createFixed(crafter.getUuid(), amount.getAsInt());
            }
            default -> throw new IllegalStateException("Did not found custom item type for " + result.getId());
        };
    }

}
