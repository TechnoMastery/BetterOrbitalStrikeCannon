package net.minheur.betterosc;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.Vec2ArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minheur.betterosc.executors.*;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class CommandRegister {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {

            dispatcher.register(
                    literal("orbitalCannon")

                            /*
                             * SPAWN BLOCK - instant usage
                             */

                            .then(literal("spawn").requires(CommandRegister::isOpped)

                                    // DARKNESS

                                    .then(literal("darkness")
                                            .executes(source -> { // DARKNESS
                                                if (!(source.getSource().getEntity() instanceof ServerPlayerEntity user)) return 0;
                                                int duration = ConfigHandler.getConfig().darknessDuration;
                                                int radius = ConfigHandler.getConfig().darknessRadius;
                                                Darkness.apply(user, radius, duration);
                                                source.getSource().sendFeedback(() ->
                                                        Text.literal("Darkness applied for " + duration + " in " + radius + " blocks.").formatted(Formatting.DARK_GRAY),
                                                        true);
                                                return 1;
                                            })
                                            .then(argument("duration", IntegerArgumentType.integer())
                                                    .executes(source -> { // DARKNESS - duration
                                                        if (!(source.getSource().getEntity() instanceof ServerPlayerEntity user)) return 0;
                                                        int duration = IntegerArgumentType.getInteger(source, "duration");
                                                        int radius = ConfigHandler.getConfig().darknessRadius;
                                                        Darkness.apply(user, radius, duration);
                                                        source.getSource().sendFeedback(() ->
                                                                        Text.literal("Darkness applied for " + duration + " in " + radius + " blocks.").formatted(Formatting.DARK_GRAY),
                                                                true);
                                                        return 1;
                                                    })
                                                    .then(argument("radius", IntegerArgumentType.integer())
                                                            .executes(source -> { // DARKNESS - duration & radius
                                                                if (!(source.getSource().getEntity() instanceof ServerPlayerEntity user)) return 0;
                                                                int duration = IntegerArgumentType.getInteger(source, "duration");
                                                                int radius = IntegerArgumentType.getInteger(source, "radius");
                                                                Darkness.apply(user, radius, duration);
                                                                source.getSource().sendFeedback(() ->
                                                                                Text.literal("Darkness applied for " + duration + " in " + radius + " blocks.").formatted(Formatting.DARK_GRAY),
                                                                        true);
                                                                return 1;
                                                            }))
                                            )
                                    )

                                    // RAILGUN

                                    .then(literal("railgun").then(literal("straight")
                                            .executes(source -> { // RAILGUN
                                                if (!(source.getSource().getEntity() instanceof ServerPlayerEntity user)) return 0;
                                                double spreadRadius = ConfigHandler.getConfig().railgunSpreadRadius;
                                                Railgun.fireStraight(user, user.getEyePos(), user.getRotationVec(1.0f), spreadRadius);
                                                source.getSource().sendFeedback(() -> Text.literal("Railgun incoming...").formatted(Formatting.YELLOW), true);
                                                return 1;
                                            })
                                            .then(argument("radius", DoubleArgumentType.doubleArg())
                                                    .executes(source -> { // RAILGUN - spread radius
                                                        if (!(source.getSource().getEntity() instanceof ServerPlayerEntity user)) return 0;
                                                        double spreadRadius = DoubleArgumentType.getDouble(source, "radius");
                                                        Railgun.fireStraight(user, user.getEyePos(), user.getRotationVec(1.0f), spreadRadius);
                                                        source.getSource().sendFeedback(() -> Text.literal("Railgun incoming...").formatted(Formatting.YELLOW), true);
                                                        return 1;
                                                    })
                                            )
                                    ))

                                    // NUKE

                                    .then(literal("nuke")
                                            .executes(source -> { // NUKE
                                                if (!(source.getSource().getEntity() instanceof ServerPlayerEntity user)) return 0;
                                                int x = user.getBlockX();
                                                int z = user.getBlockZ();
                                                int ringAmount = ConfigHandler.getConfig().nukeRingAmount;
                                                Nuke.spawn(source.getSource().getWorld(), x, z, ringAmount);
                                                source.getSource().sendFeedback(
                                                        () -> Text.literal("<!> Nuke Incoming <!> x " + x + " z " + z).formatted(Formatting.DARK_RED),
                                                        true);
                                                return 1;
                                            })
                                            .then(argument("target", Vec2ArgumentType.vec2())
                                                    .executes(source -> { // NUKE - x z
                                                        if (!(source.getSource().getEntity() instanceof ServerPlayerEntity)) return 0;
                                                        Vec2f target = Vec2ArgumentType.getVec2(source, "target");
                                                        int ringAmount = ConfigHandler.getConfig().nukeRingAmount;
                                                        Nuke.spawn(source.getSource().getWorld(), (int) target.x, (int) target.y, ringAmount);
                                                        source.getSource().sendFeedback(
                                                                () -> Text.literal("<!> Nuke Incoming <!> x " + target.x + " z " + target.y).formatted(Formatting.DARK_RED),
                                                                true);
                                                        return 1;
                                                    })
                                                    .then(argument("ringAmount", IntegerArgumentType.integer())
                                                            .executes(source -> { // NUKE - x z & ring amount
                                                                if (!(source.getSource().getEntity() instanceof ServerPlayerEntity)) return 0;
                                                                Vec2f target = Vec2ArgumentType.getVec2(source, "target");
                                                                int ringAmount = IntegerArgumentType.getInteger(source, "ringAmount");
                                                                Nuke.spawn(source.getSource().getWorld(), (int) target.x, (int) target.y, ringAmount);
                                                                source.getSource().sendFeedback(
                                                                        () -> Text.literal("<!> Nuke Incoming <!> x " + target.x + " z " + target.y).formatted(Formatting.DARK_RED),
                                                                        true);
                                                                return 1;
                                                            })
                                                    )
                                            )
                                    )

                                    // STAB

                                    .then(literal("stab")
                                            .executes(source -> { // STAB
                                                if (!(source.getSource().getEntity() instanceof ServerPlayerEntity user)) return 0;
                                                int x = user.getBlockX();
                                                int z = user.getBlockZ();
                                                Stab.spawn(source.getSource().getWorld(), x, z);
                                                source.getSource().sendFeedback(
                                                        () -> Text.literal("<!> Stab Incoming <!> x " + x + " z " + z).formatted(Formatting.RED),
                                                        true);
                                                return 1;
                                            })
                                            .then(argument("target", Vec2ArgumentType.vec2())
                                                    .executes(source -> { // STAB - x z
                                                        if (!(source.getSource().getEntity() instanceof ServerPlayerEntity)) return 0;
                                                        Vec2f target = Vec2ArgumentType.getVec2(source, "target");
                                                        Stab.spawn(source.getSource().getWorld(), (int) target.x, (int) target.y);
                                                        source.getSource().sendFeedback(
                                                                () -> Text.literal("<!> Stab Incoming <!> x " + target.x + " z " + target.y).formatted(Formatting.RED),
                                                                true);
                                                        return 1;
                                                    })
                                            )
                                    )

                                    // WOLVES

                                    .then(literal("wolves")
                                            .executes(source -> { // WOLVES
                                                if (!(source.getSource().getEntity() instanceof ServerPlayerEntity user)) return 0;
                                                int amount = ConfigHandler.getConfig().defaultWolfAmount;
                                                Wolves.spawn(user, amount);
                                                source.getSource().sendFeedback(() -> Text.literal("A bone army of " + amount + " is coming").formatted(Formatting.AQUA), true);
                                                return 1;
                                            })
                                            .then(argument("amount", IntegerArgumentType.integer())
                                                    .executes(source -> { // WOLVES - amount
                                                        if (!(source.getSource().getEntity() instanceof ServerPlayerEntity user)) return 0;
                                                        int amount = IntegerArgumentType.getInteger(source, "amount");
                                                        Wolves.spawn(user, amount);
                                                        source.getSource().sendFeedback(() -> Text.literal("A bone army of " + amount + " is coming").formatted(Formatting.AQUA), true);
                                                        return 1;
                                                    })
                                            )
                                    )

                                    // CRASH PIG

                                    .then(literal("crashPig").then(argument("target", Vec3ArgumentType.vec3())
                                            .executes(source -> {
                                                if (!(source.getSource().getEntity() instanceof ServerPlayerEntity user)) return 0;
                                                Vec3d target = Vec3ArgumentType.getVec3(source, "target");
                                                int pigAmount = ConfigHandler.getConfig().pigAmount;
                                                CrashPig.spawn(user, (int) target.x, (int) target.y, (int) target.z, pigAmount);
                                                source.getSource().sendFeedback(() -> Text.literal("A pig army of " + pigAmount + " will crash your enemies...").formatted(Formatting.DARK_AQUA), true);
                                                return 1;
                                            })
                                            .then(argument("pigAmount", IntegerArgumentType.integer())
                                                    .executes(source -> {
                                                        if (!(source.getSource().getEntity() instanceof ServerPlayerEntity user)) return 0;
                                                        Vec3d target = Vec3ArgumentType.getVec3(source, "target");
                                                        int pigAmount = IntegerArgumentType.getInteger(source, "pigAmount");
                                                        CrashPig.spawn(user, (int) target.x, (int) target.y, (int) target.z, pigAmount);
                                                        source.getSource().sendFeedback(() -> Text.literal("A pig army of " + pigAmount + " will crash your enemies...").formatted(Formatting.DARK_AQUA), true);
                                                        return 1;
                                                    })
                                            )
                                    ))
                            )

                            /*
                             * END BLOCK SPAWN
                             */

                            .then(literal("give").requires(CommandRegister::isOpped)

                                    // DARKNESS

                                    .then(literal("darkness")
                                            .executes(source -> { // DARKNESS
                                                if (!(source.getSource().getEntity() instanceof ServerPlayerEntity user)) return 0;
                                                int duration = ConfigHandler.getConfig().darknessDuration;
                                                int radius = ConfigHandler.getConfig().darknessRadius;
                                                ItemStack giveStack = Darkness.create(radius, duration);
                                                if (!user.giveItemStack(giveStack)) user.dropItem(giveStack, true);
                                                source.getSource().sendFeedback(() -> Text.empty()
                                                        .append(Text.literal("Gave you ").formatted(Formatting.DARK_GRAY))
                                                        .append(Text.literal("1").formatted(Formatting.DARK_GRAY, Formatting.UNDERLINE))
                                                        .append(Text.literal(" Darkness rod !").formatted(Formatting.DARK_GRAY)),
                                                        true
                                                );
                                                return 1;
                                            })
                                            .then(argument("amount", IntegerArgumentType.integer())
                                                    .executes(source -> { // DARKNESS - amount
                                                        if (!(source.getSource().getEntity() instanceof ServerPlayerEntity user)) return 0;
                                                        int duration = ConfigHandler.getConfig().darknessDuration;
                                                        int radius = ConfigHandler.getConfig().darknessRadius;
                                                        int amount = IntegerArgumentType.getInteger(source, "amount");
                                                        if (amount <= 0) {
                                                            source.getSource().sendError(Text.literal("Need at least 1 rod!"));
                                                            return 0;
                                                        }
                                                        for (int i = 0; i < amount; i++) {
                                                            ItemStack toGive = Darkness.create(radius, duration);
                                                            if (!user.giveItemStack(toGive)) user.dropItem(toGive, true);
                                                        }
                                                        source.getSource().sendFeedback(() -> Text.empty()
                                                                        .append(Text.literal("Gave you ").formatted(Formatting.DARK_GRAY))
                                                                        .append(Text.literal(String.valueOf(amount)).formatted(Formatting.DARK_GRAY, Formatting.UNDERLINE))
                                                                        .append(Text.literal(" Darkness rod !").formatted(Formatting.DARK_GRAY)),
                                                                true
                                                        );
                                                        return 1;
                                                    })
                                                    .then(argument("duration", IntegerArgumentType.integer())
                                                            .executes(source -> { // DARKNESS - amount & duration
                                                                if (!(source.getSource().getEntity() instanceof ServerPlayerEntity user)) return 0;
                                                                int duration = IntegerArgumentType.getInteger(source, "duration");
                                                                int radius = ConfigHandler.getConfig().darknessRadius;
                                                                int amount = IntegerArgumentType.getInteger(source, "amount");
                                                                if (amount <= 0) {
                                                                    source.getSource().sendError(Text.literal("Need at least 1 rod!"));
                                                                    return 0;
                                                                }
                                                                for (int i = 0; i < amount; i++) {
                                                                    ItemStack toGive = Darkness.create(radius, duration);
                                                                    if (!user.giveItemStack(toGive)) user.dropItem(toGive, true);
                                                                }
                                                                source.getSource().sendFeedback(() -> Text.empty()
                                                                                .append(Text.literal("Gave you ").formatted(Formatting.DARK_GRAY))
                                                                                .append(Text.literal(String.valueOf(amount)).formatted(Formatting.DARK_GRAY, Formatting.UNDERLINE))
                                                                                .append(Text.literal(" Darkness rod !").formatted(Formatting.DARK_GRAY)),
                                                                        true
                                                                );
                                                                return 1;
                                                            })
                                                            .then(argument("radius", IntegerArgumentType.integer())
                                                                    .executes(source -> { // DARKNESS - amount & duration & radius
                                                                        if (!(source.getSource().getEntity() instanceof ServerPlayerEntity user)) return 0;
                                                                        int duration = IntegerArgumentType.getInteger(source, "duration");
                                                                        int radius = IntegerArgumentType.getInteger(source, "radius");
                                                                        int amount = IntegerArgumentType.getInteger(source, "amount");
                                                                        if (amount <= 0) {
                                                                            source.getSource().sendError(Text.literal("Need at least 1 rod!"));
                                                                            return 0;
                                                                        }
                                                                        for (int i = 0; i < amount; i++) {
                                                                            ItemStack toGive = Darkness.create(radius, duration);
                                                                            if (!user.giveItemStack(toGive)) user.dropItem(toGive, true);
                                                                        }
                                                                        source.getSource().sendFeedback(() -> Text.empty()
                                                                                        .append(Text.literal("Gave you ").formatted(Formatting.DARK_GRAY))
                                                                                        .append(Text.literal(String.valueOf(amount)).formatted(Formatting.DARK_GRAY, Formatting.UNDERLINE))
                                                                                        .append(Text.literal(" Darkness rod !").formatted(Formatting.DARK_GRAY)),
                                                                                true
                                                                        );
                                                                        return 1;
                                                                    })
                                                            )
                                                    )
                                            )
                                    )

                                    // RAILGUN

                                    .then(literal("railgun").then(literal("straight")
                                            .executes(source -> { // RAILGUN
                                                if (!(source.getSource().getEntity() instanceof ServerPlayerEntity user)) return 0;
                                                double radius = ConfigHandler.getConfig().railgunSpreadRadius;
                                                ItemStack toGive = Railgun.createStraight(radius);
                                                if (!user.giveItemStack(toGive)) user.dropItem(toGive, true);
                                                source.getSource().sendFeedback(() -> Text.empty()
                                                                .append(Text.literal("Gave you ").formatted(Formatting.YELLOW))
                                                                .append(Text.literal("1").formatted(Formatting.YELLOW, Formatting.UNDERLINE))
                                                                .append(Text.literal(" Railgun !").formatted(Formatting.YELLOW)),
                                                        true
                                                );
                                                return 1;
                                            })
                                            .then(argument("amount", IntegerArgumentType.integer())
                                                    .executes(source -> { // RAILGUN - amount
                                                        if (!(source.getSource().getEntity() instanceof ServerPlayerEntity user)) return 0;
                                                        double radius = ConfigHandler.getConfig().railgunSpreadRadius;
                                                        int amount = IntegerArgumentType.getInteger(source, "amount");
                                                        if (amount <= 0) {
                                                            source.getSource().sendError(Text.literal("Need at least 1 railfun!"));
                                                            return 0;
                                                        }
                                                        for (int i = 0; i < amount; i++) {
                                                            ItemStack toGive = Railgun.createStraight(radius);
                                                            if (!user.giveItemStack(toGive)) user.dropItem(toGive, true);
                                                        }
                                                        source.getSource().sendFeedback(() -> Text.empty()
                                                                        .append(Text.literal("Gave you ").formatted(Formatting.YELLOW))
                                                                        .append(Text.literal(String.valueOf(amount)).formatted(Formatting.YELLOW, Formatting.UNDERLINE))
                                                                        .append(Text.literal(" Railgun !").formatted(Formatting.YELLOW)),
                                                                true
                                                        );
                                                        return 1;
                                                    })
                                                    .then(argument("radius", DoubleArgumentType.doubleArg())
                                                            .executes(source -> { // RAILGUN - amount & radius
                                                                if (!(source.getSource().getEntity() instanceof ServerPlayerEntity user)) return 0;
                                                                double radius = DoubleArgumentType.getDouble(source, "radius");
                                                                int amount = IntegerArgumentType.getInteger(source, "amount");
                                                                if (amount <= 0) {
                                                                    source.getSource().sendError(Text.literal("Need at least 1 railgun!"));
                                                                    return 0;
                                                                }
                                                                for (int i = 0; i < amount; i++) {
                                                                    ItemStack toGive = Railgun.createStraight(radius);
                                                                    if (!user.giveItemStack(toGive)) user.dropItem(toGive, true);
                                                                }
                                                                source.getSource().sendFeedback(() -> Text.empty()
                                                                                .append(Text.literal("Gave you ").formatted(Formatting.YELLOW))
                                                                                .append(Text.literal(String.valueOf(amount)).formatted(Formatting.YELLOW, Formatting.UNDERLINE))
                                                                                .append(Text.literal(" Railgun !").formatted(Formatting.YELLOW)),
                                                                        true
                                                                );
                                                                return 1;
                                                            })
                                                    )
                                            )
                                    ))

                                    // NUKE

                                    .then(literal("nuke")
                                            .executes(source -> { // NUKE
                                                if (!(source.getSource().getEntity() instanceof ServerPlayerEntity user)) return 0;
                                                ItemStack toGive = Nuke.create(ConfigHandler.getConfig().nukeRingAmount);
                                                if (!user.giveItemStack(toGive)) user.dropItem(toGive, true);
                                                source.getSource().sendFeedback(() -> Text.empty()
                                                                .append(Text.literal("Gave you ").formatted(Formatting.DARK_RED))
                                                                .append(Text.literal("1").formatted(Formatting.DARK_RED, Formatting.UNDERLINE))
                                                                .append(Text.literal(" Nuke shot !").formatted(Formatting.DARK_RED)),
                                                        true
                                                );
                                                return 1;
                                            })
                                            .then(argument("amount", IntegerArgumentType.integer())
                                                    .executes(source -> { // NUKE - amount
                                                        if (!(source.getSource().getEntity() instanceof ServerPlayerEntity user)) return 0;
                                                        int amount = IntegerArgumentType.getInteger(source, "amount");
                                                        if (amount <= 0) {
                                                            source.getSource().sendError(Text.literal("Need at least 1 rod!"));
                                                            return 0;
                                                        }
                                                        for (int i = 0; i < amount; i++) {
                                                            ItemStack toGive = Nuke.create(ConfigHandler.getConfig().nukeRingAmount);
                                                            if (!user.giveItemStack(toGive)) user.dropItem(toGive, true);
                                                        }
                                                        source.getSource().sendFeedback(() -> Text.empty()
                                                                        .append(Text.literal("Gave you ").formatted(Formatting.DARK_RED))
                                                                        .append(Text.literal(String.valueOf(amount)).formatted(Formatting.DARK_RED, Formatting.UNDERLINE))
                                                                        .append(Text.literal(" Nuke shot !").formatted(Formatting.DARK_RED)),
                                                                true
                                                        );
                                                        return 1;
                                                    })
                                                    .then(argument("rings", IntegerArgumentType.integer())
                                                            .executes(source -> { // NUKE - amount & rings
                                                                if (!(source.getSource().getEntity() instanceof ServerPlayerEntity user)) return 0;
                                                                int ringAmount = IntegerArgumentType.getInteger(source, "rings");
                                                                int amount = IntegerArgumentType.getInteger(source, "amount");
                                                                if (amount <= 0) {
                                                                    source.getSource().sendError(Text.literal("Need at least 1 rod!"));
                                                                    return 0;
                                                                }
                                                                for (int i = 0; i < amount; i++) {
                                                                    ItemStack toGive = Nuke.create(ConfigHandler.getConfig().nukeRingAmount);
                                                                    if (!user.giveItemStack(toGive)) user.dropItem(toGive, true);
                                                                }
                                                                source.getSource().sendFeedback(() -> Text.empty()
                                                                                .append(Text.literal("Gave you ").formatted(Formatting.DARK_RED))
                                                                                .append(Text.literal(String.valueOf(amount)).formatted(Formatting.DARK_RED, Formatting.UNDERLINE))
                                                                                .append(Text.literal(" Nuke shot !").formatted(Formatting.DARK_RED)),
                                                                        true
                                                                );
                                                                return 1;
                                                            })
                                                            .then(argument("target", Vec2ArgumentType.vec2())
                                                                    .executes(source -> { // NUKE - amount & ring & target
                                                                        if (!(source.getSource().getEntity() instanceof ServerPlayerEntity user)) return 0;
                                                                        int ringAmount = IntegerArgumentType.getInteger(source, "rings");
                                                                        int amount = IntegerArgumentType.getInteger(source, "amount");
                                                                        if (amount <= 0) {
                                                                            source.getSource().sendError(Text.literal("Need at least 1 rod!"));
                                                                            return 0;
                                                                        }
                                                                        Vec2f target = Vec2ArgumentType.getVec2(source, "target");
                                                                        for (int i = 0; i < amount; i++) {
                                                                            ItemStack toGive = Nuke.create(ConfigHandler.getConfig().nukeRingAmount);
                                                                            if (!user.giveItemStack(toGive)) user.dropItem(toGive, true);
                                                                        }
                                                                        source.getSource().sendFeedback(() -> Text.empty()
                                                                                        .append(Text.literal("Gave you ").formatted(Formatting.DARK_RED))
                                                                                        .append(Text.literal(String.valueOf(amount)).formatted(Formatting.DARK_RED, Formatting.UNDERLINE))
                                                                                        .append(Text.literal(" Nuke shot !").formatted(Formatting.DARK_RED)),
                                                                                true
                                                                        );
                                                                        return 1;
                                                                    })
                                                            )
                                                    )
                                            )
                                    )

                                    // STAB

                                    .then(literal("stab")
                                            .executes(source -> { // STAB
                                                if (!(source.getSource().getEntity() instanceof ServerPlayerEntity user)) return 0;
                                                ItemStack toGive = Stab.create();
                                                if (!user.giveItemStack(toGive)) user.dropItem(toGive, true);
                                                user.giveItemStack(Stab.create());
                                                source.getSource().sendFeedback(() -> Text.empty()
                                                                .append(Text.literal("Gave you ").formatted(Formatting.RED))
                                                                .append(Text.literal("1").formatted(Formatting.RED, Formatting.UNDERLINE))
                                                                .append(Text.literal(" Stab shot !").formatted(Formatting.RED)),
                                                        true
                                                );
                                                return 1;
                                            })
                                            .then(argument("amount", IntegerArgumentType.integer())
                                                    .executes(source -> { // STAB - amount
                                                        if (!(source.getSource().getEntity() instanceof ServerPlayerEntity user)) return 0;
                                                        int amount = IntegerArgumentType.getInteger(source, "amount");
                                                        if (amount <= 0) {
                                                            source.getSource().sendError(Text.literal("Need at least 1 rod!"));
                                                            return 0;
                                                        }
                                                        for (int i = 0; i < amount; i++) {
                                                            ItemStack toGive = Stab.create();
                                                            if (!user.giveItemStack(toGive)) user.dropItem(toGive, true);
                                                        }
                                                        source.getSource().sendFeedback(() -> Text.empty()
                                                                        .append(Text.literal("Gave you ").formatted(Formatting.RED))
                                                                        .append(Text.literal(String.valueOf(amount)).formatted(Formatting.RED, Formatting.UNDERLINE))
                                                                        .append(Text.literal(" Stab shot !").formatted(Formatting.RED)),
                                                                true
                                                        );
                                                        return 1;
                                                    })
                                                    .then(argument("target", Vec2ArgumentType.vec2())
                                                            .executes(source -> { // STAB - amount & target
                                                                if (!(source.getSource().getEntity() instanceof ServerPlayerEntity user)) return 0;
                                                                int amount = IntegerArgumentType.getInteger(source, "amount");
                                                                if (amount <= 0) {
                                                                    source.getSource().sendError(Text.literal("Need at least 1 rod!"));
                                                                    return 0;
                                                                }
                                                                Vec2f target = Vec2ArgumentType.getVec2(source, "target");
                                                                for (int i = 0; i < amount; i++) {
                                                                    ItemStack toGive = Stab.create((int) target.x, (int) target.y);
                                                                    if (!user.giveItemStack(toGive)) user.dropItem(toGive, true);
                                                                }
                                                                source.getSource().sendFeedback(() -> Text.empty()
                                                                                .append(Text.literal("Gave you ").formatted(Formatting.RED))
                                                                                .append(Text.literal(String.valueOf(amount)).formatted(Formatting.RED, Formatting.UNDERLINE))
                                                                                .append(Text.literal(" Stab shot !").formatted(Formatting.RED)),
                                                                        true
                                                                );
                                                                return 1;
                                                            })
                                                    )
                                            )
                                    )

                                    // TOTEM STASIS

                                    .then(literal("totemStasis")
                                            .then(argument("pos", Vec3ArgumentType.vec3())
                                                    .executes(source -> { // TOTEM
                                                        if (!(source.getSource().getEntity() instanceof ServerPlayerEntity user)) return 0;
                                                        Vec3d pos = Vec3ArgumentType.getVec3(source, "pos");
                                                        ItemStack toGive = TotemStasis.createMobile((int) pos.x, (int) pos.y, (int) pos.z);
                                                        if (!user.giveItemStack(toGive)) user.dropItem(toGive, true);
                                                        source.getSource().sendFeedback(() -> Text.empty()
                                                                        .append(Text.literal("Gave you ").formatted(Formatting.GOLD))
                                                                        .append(Text.literal("1").formatted(Formatting.GOLD, Formatting.UNDERLINE))
                                                                        .append(Text.literal(" Totem stasis !").formatted(Formatting.GOLD)),
                                                                true
                                                        );
                                                        return 1;
                                                    })
                                                    .then(argument("amount", IntegerArgumentType.integer())
                                                            .executes(source -> { // TOTEM - amount
                                                                if (!(source.getSource().getEntity() instanceof ServerPlayerEntity user)) return 0;
                                                                Vec3d pos = Vec3ArgumentType.getVec3(source, "pos");
                                                                int amount = IntegerArgumentType.getInteger(source, "amount");
                                                                if (amount <= 0) {
                                                                    source.getSource().sendError(Text.literal("Need at least 1 rod!"));
                                                                    return 0;
                                                                }
                                                                for (int i = 0; i < amount; i++) {
                                                                    ItemStack toGive = TotemStasis.createMobile((int) pos.x, (int) pos.y, (int) pos.z);
                                                                    if (!user.giveItemStack(toGive)) user.dropItem(toGive, true);
                                                                }
                                                                source.getSource().sendFeedback(() -> Text.empty()
                                                                                .append(Text.literal("Gave you ").formatted(Formatting.GOLD))
                                                                                .append(Text.literal(String.valueOf(amount)).formatted(Formatting.GOLD, Formatting.UNDERLINE))
                                                                                .append(Text.literal(" Totem stasis !").formatted(Formatting.GOLD)),
                                                                        true
                                                                );
                                                                return 1;
                                                            })
                                                            .then(argument("target", EntityArgumentType.player())
                                                                    .executes(source -> { // TOTEM - amount & fixed
                                                                        if (!(source.getSource().getEntity() instanceof ServerPlayerEntity user)) return 0;
                                                                        Vec3d pos = Vec3ArgumentType.getVec3(source, "pos");
                                                                        int amount = IntegerArgumentType.getInteger(source, "amount");
                                                                        if (amount <= 0) {
                                                                            source.getSource().sendError(Text.literal("Need at least 1 rod!"));
                                                                            return 0;
                                                                        }
                                                                        ServerPlayerEntity target = EntityArgumentType.getPlayer(source, "target");
                                                                        for (int i = 0; i < amount; i++) {
                                                                            ItemStack toGive = TotemStasis.createFixed(target.getUuid(), (int) pos.x, (int) pos.y, (int) pos.z);
                                                                            if (!user.giveItemStack(toGive)) user.dropItem(toGive, true);
                                                                        }
                                                                        source.getSource().sendFeedback(() -> Text.empty()
                                                                                        .append(Text.literal("Gave you ").formatted(Formatting.GOLD))
                                                                                        .append(Text.literal(String.valueOf(amount)).formatted(Formatting.GOLD, Formatting.UNDERLINE))
                                                                                        .append(Text.literal(" Totem stasis !").formatted(Formatting.GOLD)),
                                                                                true
                                                                        );
                                                                        return 1;
                                                                    })
                                                            )
                                                    )
                                            )
                                    )

                                    // TP STASIS

                                    .then(literal("stasis")
                                            .then(argument("pos", Vec3ArgumentType.vec3())
                                                    .executes(source -> { // STASIS
                                                        if (!(source.getSource().getEntity() instanceof ServerPlayerEntity user)) return 0;
                                                        Vec3d pos = Vec3ArgumentType.getVec3(source, "pos");
                                                        ItemStack toGive = TpStasis.createMobile((int) pos.x, (int) pos.y, (int) pos.z);
                                                        if (!user.giveItemStack(toGive)) user.dropItem(toGive, true);
                                                        source.getSource().sendFeedback(() -> Text.empty()
                                                                        .append(Text.literal("Gave you ").formatted(Formatting.GRAY))
                                                                        .append(Text.literal("1").formatted(Formatting.GRAY, Formatting.UNDERLINE))
                                                                        .append(Text.literal(" Stasis !").formatted(Formatting.GRAY)),
                                                                true
                                                        );
                                                        return 1;
                                                    })
                                                    .then(argument("amount", IntegerArgumentType.integer())
                                                            .executes(source -> { // STASIS - amount
                                                                if (!(source.getSource().getEntity() instanceof ServerPlayerEntity user)) return 0;
                                                                Vec3d pos = Vec3ArgumentType.getVec3(source, "pos");
                                                                int amount = IntegerArgumentType.getInteger(source, "amount");
                                                                if (amount <= 0) {
                                                                    source.getSource().sendError(Text.literal("Need at least 1 rod!"));
                                                                    return 0;
                                                                }
                                                                for (int i = 0; i < amount; i++) {
                                                                    ItemStack toGive = TpStasis.createMobile((int) pos.x, (int) pos.y, (int) pos.z);
                                                                    if (!user.giveItemStack(toGive)) user.dropItem(toGive, true);
                                                                }
                                                                source.getSource().sendFeedback(() -> Text.empty()
                                                                                .append(Text.literal("Gave you ").formatted(Formatting.GRAY))
                                                                                .append(Text.literal(String.valueOf(amount)).formatted(Formatting.GRAY, Formatting.UNDERLINE))
                                                                                .append(Text.literal(" Stasis !").formatted(Formatting.GRAY)),
                                                                        true
                                                                );
                                                                return 1;
                                                            })
                                                            .then(argument("target", EntityArgumentType.player())
                                                                    .executes(source -> { // STASIS - amount & target
                                                                        if (!(source.getSource().getEntity() instanceof ServerPlayerEntity user)) return 0;
                                                                        Vec3d pos = Vec3ArgumentType.getVec3(source, "pos");
                                                                        int amount = IntegerArgumentType.getInteger(source, "amount");
                                                                        if (amount <= 0) {
                                                                            source.getSource().sendError(Text.literal("Need at least 1 rod!"));
                                                                            return 0;
                                                                        }
                                                                        ServerPlayerEntity target = EntityArgumentType.getPlayer(source, "target");
                                                                        for (int i = 0; i < amount; i++) {
                                                                            ItemStack toGive = TpStasis.createFixed(target.getUuid(), (int) pos.x, (int) pos.y, (int) pos.z);
                                                                            if (!user.giveItemStack(toGive)) user.dropItem(toGive, true);
                                                                        }
                                                                        source.getSource().sendFeedback(() -> Text.empty()
                                                                                        .append(Text.literal("Gave you ").formatted(Formatting.GRAY))
                                                                                        .append(Text.literal(String.valueOf(amount)).formatted(Formatting.GRAY, Formatting.UNDERLINE))
                                                                                        .append(Text.literal(" Stasis !").formatted(Formatting.GRAY)),
                                                                                true
                                                                        );
                                                                        return 1;
                                                                    })
                                                            )
                                                    )
                                            )
                                    )

                                    // WOLVES

                                    .then(literal("wolves")
                                            .executes(source -> { // WOLVES
                                                if (!(source.getSource().getEntity() instanceof ServerPlayerEntity user)) return 0;
                                                ItemStack toGive = Wolves.create(ConfigHandler.getConfig().defaultWolfAmount);
                                                if (!user.giveItemStack(toGive)) user.dropItem(toGive, true);
                                                source.getSource().sendFeedback(() -> Text.empty()
                                                                .append(Text.literal("Gave you ").formatted(Formatting.GREEN))
                                                                .append(Text.literal("1").formatted(Formatting.GREEN, Formatting.UNDERLINE))
                                                                .append(Text.literal(" Wolves rod !").formatted(Formatting.GREEN)),
                                                        true
                                                );
                                                return 1;
                                            })
                                            .then(argument("amount", IntegerArgumentType.integer())
                                                    .executes(source -> { // WOLVES - amount
                                                        if (!(source.getSource().getEntity() instanceof ServerPlayerEntity user)) return 0;
                                                        int amount = IntegerArgumentType.getInteger(source, "amount");
                                                        if (amount <= 0) {
                                                            source.getSource().sendError(Text.literal("Need at least 1 rod!"));
                                                            return 0;
                                                        }
                                                        for (int i = 0; i < amount; i++) {
                                                            ItemStack toGive = Wolves.create(ConfigHandler.getConfig().defaultWolfAmount);
                                                            if (!user.giveItemStack(toGive)) user.dropItem(toGive, true);
                                                        }
                                                        source.getSource().sendFeedback(() -> Text.empty()
                                                                        .append(Text.literal("Gave you ").formatted(Formatting.GREEN))
                                                                        .append(Text.literal(String.valueOf(amount)).formatted(Formatting.GREEN, Formatting.UNDERLINE))
                                                                        .append(Text.literal(" Wolves !").formatted(Formatting.GREEN)),
                                                                true
                                                        );
                                                        return 1;
                                                    })
                                                    .then(argument("wolvesAmount", IntegerArgumentType.integer())
                                                            .executes(source -> {
                                                                if (!(source.getSource().getEntity() instanceof ServerPlayerEntity user)) return 0;
                                                                int amount = IntegerArgumentType.getInteger(source, "amount");
                                                                if (amount <= 0) {
                                                                    source.getSource().sendError(Text.literal("Need at least 1 rod!"));
                                                                    return 0;
                                                                }
                                                                int wolvesAmount = IntegerArgumentType.getInteger(source, "wolvesAmount");
                                                                for (int i = 0; i < amount; i++) {
                                                                    ItemStack toGive = Wolves.create(wolvesAmount);
                                                                    if (!user.giveItemStack(toGive)) user.dropItem(toGive, true);
                                                                }
                                                                source.getSource().sendFeedback(() -> Text.empty()
                                                                                .append(Text.literal("Gave you ").formatted(Formatting.GREEN))
                                                                                .append(Text.literal(String.valueOf(amount)).formatted(Formatting.GREEN, Formatting.UNDERLINE))
                                                                                .append(Text.literal(" Wolves !").formatted(Formatting.GREEN)),
                                                                        true
                                                                );
                                                                return 1;
                                                            })
                                                    )
                                            )
                                    )

                                    // CRASH PIG

                                    .then(literal("crashPig").then(argument("target", Vec3ArgumentType.vec3())
                                            .executes(source -> {
                                                if (!(source.getSource().getEntity() instanceof ServerPlayerEntity user)) return 0;
                                                Vec3d target = Vec3ArgumentType.getVec3(source, "target");
                                                ItemStack toGive = CrashPig.create((int) target.x, (int) target.y, (int) target.z);
                                                if (!user.giveItemStack(toGive)) user.dropItem(toGive, true);
                                                source.getSource().sendFeedback(() -> Text.empty()
                                                                .append(Text.literal("Gave you ").formatted(Formatting.DARK_AQUA))
                                                                .append(Text.literal("1").formatted(Formatting.DARK_AQUA, Formatting.UNDERLINE))
                                                                .append(Text.literal(" CrashPig !").formatted(Formatting.DARK_AQUA)),
                                                        true
                                                );
                                                return 1;
                                            })
                                            .then(argument("amount", IntegerArgumentType.integer())
                                                    .executes(source -> {
                                                        if (!(source.getSource().getEntity() instanceof ServerPlayerEntity user)) return 0;
                                                        Vec3d target = Vec3ArgumentType.getVec3(source, "target");
                                                        int amount = IntegerArgumentType.getInteger(source, "integer");
                                                        if (amount <= 0) {
                                                            source.getSource().sendError(Text.literal("Need at least 1 rod!"));
                                                            return 0;
                                                        }
                                                        for (int i = 0; i < amount; i++) {
                                                            ItemStack toGive = CrashPig.create((int) target.x, (int) target.y, (int) target.z);
                                                            if (!user.giveItemStack(toGive)) user.dropItem(toGive, true);
                                                        }
                                                        source.getSource().sendFeedback(() -> Text.empty()
                                                                        .append(Text.literal("Gave you ").formatted(Formatting.DARK_AQUA))
                                                                        .append(Text.literal(String.valueOf(amount)).formatted(Formatting.DARK_AQUA, Formatting.UNDERLINE))
                                                                        .append(Text.literal(" CrashPig !").formatted(Formatting.DARK_AQUA)),
                                                                true
                                                        );
                                                        return 1;
                                                    })
                                                    .then(argument("pigAmount", IntegerArgumentType.integer())
                                                            .executes(source -> {
                                                                if (!(source.getSource().getEntity() instanceof ServerPlayerEntity user)) return 0;
                                                                Vec3d target = Vec3ArgumentType.getVec3(source, "target");
                                                                int amount = IntegerArgumentType.getInteger(source, "integer");
                                                                if (amount <= 0) {
                                                                    source.getSource().sendError(Text.literal("Need at least 1 rod!"));
                                                                    return 0;
                                                                }
                                                                int pigAmount = IntegerArgumentType.getInteger(source, "pigAmount");
                                                                for (int i = 0; i < amount; i++) {
                                                                    ItemStack toGive = CrashPig.create((int) target.x, (int) target.y, (int) target.z, pigAmount);
                                                                    if (!user.giveItemStack(toGive)) user.dropItem(toGive, true);
                                                                }
                                                                source.getSource().sendFeedback(() -> Text.empty()
                                                                                .append(Text.literal("Gave you ").formatted(Formatting.DARK_AQUA))
                                                                                .append(Text.literal(String.valueOf(amount)).formatted(Formatting.DARK_AQUA, Formatting.UNDERLINE))
                                                                                .append(Text.literal(" CrashPig !").formatted(Formatting.DARK_AQUA)),
                                                                        true
                                                                );
                                                                return 1;
                                                            })
                                                    )
                                            )
                                    ))

                            )
            );

        });
    }

    private static boolean isOpped(ServerCommandSource source) {
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) return false;
        return source.getServer().getPlayerManager().isOperator(player.getPlayerConfigEntry());
    }
}
