package net.minheur.betterosc.recipes;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

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

}
