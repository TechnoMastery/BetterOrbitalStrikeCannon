package net.minheur.betterosc.recipes;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class RecipesHandler {
    private static final List<Recipe> recipes = new ArrayList<>();

    public static void loadRecipes() {
        recipes.clear();
        try {
            recipes.addAll(RecipeLoader.loadAll());
        } catch (Exception ignored) {}
    }

    public static @Nullable Recipe getFromId(String recipeId) {
        for (Recipe recipe : recipes)
            if (recipe.recipeId().equals(recipeId)) return recipe;
        return null;
    }

    @Contract(" -> new")
    public static @NonNull Set<Identifier> mapToIdentifiers() {
        return new HashSet<>(recipes.stream().map(
                recipe -> Identifier.of(recipe.recipeId())
        ).toList());
    }

}
