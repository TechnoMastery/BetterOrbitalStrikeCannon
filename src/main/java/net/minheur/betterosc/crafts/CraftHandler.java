package net.minheur.betterosc.crafts;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minheur.betterosc.ExecutorHandler;
import net.minheur.betterosc.recipes.Recipe;
import net.minheur.betterosc.recipes.RecipesHandler;
import net.minheur.betterosc.tank.TankHandler;
import net.minheur.betterosc.tank.TankOperationHandler;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public final class CraftHandler {

    public static int handleCraft(@NonNull CommandContext<ServerCommandSource> source) {
        if (!(source.getSource().getEntity() instanceof ServerPlayerEntity crafter)) return 1;

        Identifier recipeId = IdentifierArgumentType.getIdentifier(source, "recipe");
        Recipe recipe = RecipesHandler.getFromId(recipeId.toString());

        if (recipe == null) {
            source.getSource().sendError(Text.literal("Did not found this recipe!"));
            return 0;
        }

        List<ItemStack> allStacksInInv = getAvailableStacks(crafter);
        boolean canCraft = recipe.matches(allStacksInInv);

        if (!canCraft) {
            source.getSource().sendError(Text.literal("You don't have enough resources to craft this!"));
            source.getSource().sendError(Text.literal("Check ingredients with /commandCrafter see <recipe> first!"));
            return 0;
        }

        craftItem(recipe, crafter);
        source.getSource().sendMessage(Text.literal("Here goes your craft!"));
        return 1;
    }

    public static int seeRecipe(CommandContext<ServerCommandSource> source) {
        return 1;
    }

    public static void craftItem(@NonNull Recipe recipe, ServerPlayerEntity crafter) {
        for (Recipe.IngredientMatcher ingredient : recipe.ingredients()) {
            if (checkFoundAndConsume(ingredient, crafter.getStackInHand(Hand.MAIN_HAND))) continue;
            if (checkFoundAndConsume(ingredient, crafter.getStackInHand(Hand.OFF_HAND))) continue;

            for (int i = 0; i < crafter.getInventory().size(); i++)
                if (checkFoundAndConsume(ingredient, crafter.getInventory().getStack(i))) break;
        }

        ItemStack result = recipe.result().getIsByStack() ? recipe.result().getAsStack() :
                ExecutorHandler.mkFromResultContainer(recipe.result(), crafter);

        if (!crafter.giveItemStack(result)) crafter.dropItem(result, true);
    }

    public static boolean checkFoundAndConsume(Recipe.IngredientMatcher ingredient, ItemStack candidate) {
        return false; // todo
    }

    @Contract(pure = true)
    private static @NonNull List<ItemStack> getAvailableStacks(@NonNull ServerPlayerEntity crafter) {
        List<ItemStack> returnValue = new ArrayList<>();

        returnValue.add(crafter.getStackInHand(Hand.MAIN_HAND).copy());
        returnValue.add(crafter.getStackInHand(Hand.OFF_HAND).copy());

        for (int i = 0; i < crafter.getInventory().size(); i++) {
            ItemStack stack = crafter.getInventory().getStack(i).copy();
            if (!TankHandler.isTank(stack)) returnValue.add(stack); // normal behavior
            // TANK BEHAVIOR
            long amount = TankOperationHandler.getAmountStored(stack);
            ItemStack tankContent = TankOperationHandler.getTankContent(stack);
            int contentMaxStackSize = tankContent.getMaxCount();

            long fullStacks = amount / contentMaxStackSize;
            int amountLeft = Math.toIntExact(amount % contentMaxStackSize);

            ItemStack templateStack = tankContent.copy();
            templateStack.setCount(contentMaxStackSize);

            for (int j = 0; j < fullStacks; j++) returnValue.add(templateStack.copy());
            templateStack.setCount(amountLeft);
            returnValue.add(templateStack.copy());
        }

        // CLEAR all empty stacks / slots from available
        returnValue.removeIf(stack -> stack == null || stack.isEmpty() || stack.isOf(Items.AIR));

        return returnValue;
    }

}
