package net.minheur.betterosc.tank;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minheur.betterosc.CommandRegister;

import java.util.UUID;
import java.util.function.Supplier;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class TankCommands {

    private static final Supplier<SuggestionProvider<ServerCommandSource>> TANKS_SUGGESTIONS = () ->
            (context, builder) -> {
        TankHandler.getTankState().getTanks().keySet().stream()
                .map(UUID::toString)
                .filter(id -> id.startsWith(builder.getRemaining()))
                .forEach(builder::suggest);
        return builder.buildFuture();
    };

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    literal("tank")

                            .then(literal("create").executes(source -> {
                                if (!(source.getSource().getEntity() instanceof ServerPlayerEntity user)) return 0;
                                ItemStack mainHand = user.getMainHandStack();

                                if (!IllegalTankItems.isItemAllowed(mainHand)) {
                                    source.getSource().sendError(Text.literal("Can't store this item in a tank!"));
                                    source.getSource().sendError(Text.literal("Try holding a correct item in your main hand!"));
                                    return 0;
                                }

                                ItemStack tank = TankHandler.createTank(mainHand);
                                if (tank == null) {
                                    source.getSource().sendError(Text.literal("Couldn't create tank!"));
                                    return 0;
                                }

                                user.setStackInHand(Hand.MAIN_HAND, tank);

                                for (int i = 0; i < user.getInventory().size(); i++) {
                                    ItemStack target = user.getInventory().getStack(i);
                                    final int index = i;
                                    if (!TankOperationHandler.safeIncrement(tank, target, (stack -> user.getInventory().setStack(index, stack))))
                                        break;
                                }

                                ItemStack offHand = user.getOffHandStack();
                                TankOperationHandler.safeIncrement(tank, offHand, (stack) -> user.setStackInHand(Hand.OFF_HAND, stack));

                                return 1;
                            }))

                            .then(literal("add").then(literal("all").executes(source -> {
                                        if (!(source.getSource().getEntity() instanceof ServerPlayerEntity user)) return 0;

                                        ItemStack tank = user.getMainHandStack();
                                        if (!TankHandler.isTank(tank)) {
                                            source.getSource().sendError(Text.literal("Please hold a tank in your main hand!"));
                                            return 0;
                                        }

                                        for (int i = 0; i < user.getInventory().size(); i++) {
                                            ItemStack target = user.getInventory().getStack(i);
                                            final int index = i;
                                            if (!TankOperationHandler.safeIncrement(tank, target, (stack -> user.getInventory().setStack(index, stack))))
                                                break;
                                        }

                                        ItemStack offHand = user.getOffHandStack();
                                        TankOperationHandler.safeIncrement(tank, offHand, (stack) -> user.setStackInHand(Hand.OFF_HAND, stack));

                                        return 1;
                                    }))
                                    .then(argument("amount", IntegerArgumentType.integer())
                                            .executes(source -> {
                                                if (!(source.getSource().getEntity() instanceof ServerPlayerEntity user))
                                                    return 0;

                                                ItemStack tank = user.getMainHandStack();
                                                if (!TankHandler.isTank(tank)) {
                                                    source.getSource().sendError(Text.literal("Please hold a tank in your main hand!"));
                                                    return 0;
                                                }

                                                int[] leftToAdd = {IntegerArgumentType.getInteger(source, "amount")};

                                                for (int i = 0; i < user.getInventory().size(); i++) {
                                                    ItemStack target = user.getInventory().getStack(i);
                                                    final int index = i;
                                                    if (!TankOperationHandler.safeIncrement(tank, target, (stack -> user.getInventory().setStack(index, stack)), leftToAdd))
                                                        break;
                                                    if (leftToAdd[0] <= 0) break;
                                                }

                                                if (leftToAdd[0] <= 0) return 1;

                                                ItemStack offHand = user.getOffHandStack();
                                                TankOperationHandler.safeIncrement(tank, offHand, (stack -> user.setStackInHand(Hand.OFF_HAND, stack)), leftToAdd);
                                                return 1;
                                            })))

                            .then(literal("withdraw")
                                    .then(literal("all").executes(source -> {
                                        if (!(source.getSource().getEntity() instanceof ServerPlayerEntity user))
                                            return 0;

                                        ItemStack tank = user.getMainHandStack();
                                        if (!TankHandler.isTank(tank)) {
                                            source.getSource().sendError(Text.literal("Please hold a tank in your main hand!"));
                                            return 0;
                                        }

                                        ItemStack contentRef = TankOperationHandler.getTankContent(tank);
                                        long amountStoredLeft = TankOperationHandler.getAmountStored(tank);
                                        int maxStackSize = contentRef.getMaxCount();

                                        while (amountStoredLeft > 0) {
                                            ItemStack giving = contentRef.copy();

                                            if (amountStoredLeft <= maxStackSize) {
                                                long withdrawn = amountStoredLeft;
                                                giving.setCount((int) withdrawn);

                                                if (!user.giveItemStack(giving)) user.dropItem(giving, true);
                                                amountStoredLeft = 0;
                                                OperationResults result = TankOperationHandler.decrementTank(tank, withdrawn);
                                                if (result == OperationResults.EMPTIED) {
                                                    user.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
                                                    break;
                                                }
                                            } else {

                                                giving.setCount(maxStackSize);

                                                if (!user.giveItemStack(giving)) user.dropItem(giving, true);
                                                amountStoredLeft -= maxStackSize;
                                                OperationResults result = TankOperationHandler.decrementTank(tank, maxStackSize);
                                                if (result == OperationResults.EMPTIED)
                                                    user.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);

                                            }
                                        }

                                        user.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);

                                        return 1;
                                    }))
                                    .then(argument("amount", LongArgumentType.longArg()).executes(source -> {
                                        if (!(source.getSource().getEntity() instanceof ServerPlayerEntity user))
                                            return 0;

                                        ItemStack tank = user.getMainHandStack();
                                        if (!TankHandler.isTank(tank)) {
                                            source.getSource().sendError(Text.literal("Please hold a tank in your main hand!"));
                                            return 0;
                                        }

                                        long amountToWithdraw = LongArgumentType.getLong(source, "amount");
                                        long amountStored = TankOperationHandler.getAmountStored(tank);

                                        if (amountStored < amountToWithdraw) amountToWithdraw = amountStored;

                                        ItemStack contentRef = TankOperationHandler.getTankContent(tank);
                                        int maxStackSize = contentRef.getMaxCount();

                                        while (amountToWithdraw > 0) {
                                            ItemStack giving = contentRef.copy();
                                            long withdrawn = Math.min(amountToWithdraw, maxStackSize);
                                            giving.setCount((int) withdrawn);

                                            if (!user.giveItemStack(giving)) user.dropItem(giving, true);
                                            amountToWithdraw -= withdrawn;

                                            OperationResults result = TankOperationHandler.decrementTank(tank, withdrawn);
                                            if (result == OperationResults.EMPTIED) {
                                                user.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
                                                return 1;
                                            }
                                        }

                                        if (TankHandler.isTank(tank) && TankOperationHandler.getAmountStored(tank) <= 0)
                                            user.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
                                        return 1;
                                    }))
                            )

                            .then(literal("admin").requires(CommandRegister::isOpped)
                                    .then(literal("adminGiveTank").then(argument("uuid", UuidArgumentType.uuid())
                                            .suggests(TANKS_SUGGESTIONS.get()).executes(source -> {
                                                if (!(source.getSource().getEntity() instanceof ServerPlayerEntity user))
                                                    return 0;
                                                UUID targetTank = UuidArgumentType.getUuid(source, "uuid");

                                                if (!TankHandler.getTankState().getTanks().containsKey(targetTank)) {
                                                    source.getSource().sendError(Text.literal("This tank doesn't exists!"));
                                                    return 0;
                                                }

                                                ItemStack tank = TankHandler.buildTankFromUUID(targetTank);
                                                if (!user.giveItemStack(tank)) user.dropItem(tank, true);
                                                return 1;
                                            })
                                    ))
                            )
            );

            dispatcher.register(literal("t").redirect(dispatcher.getRoot().getChild("tank")));
            dispatcher.register(literal("tc").redirect(dispatcher.getRoot().getChild("tank").getChild("create")));
            dispatcher.register(literal("ta").redirect(dispatcher.getRoot().getChild("tank").getChild("add")));
            dispatcher.register(literal("tw").redirect(dispatcher.getRoot().getChild("tank").getChild("withdraw")));

        });
    }

}
