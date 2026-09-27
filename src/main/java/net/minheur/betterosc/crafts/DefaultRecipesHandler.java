package net.minheur.betterosc.crafts;

import net.fabricmc.loader.api.FabricLoader;
import net.minheur.betterosc.Betterosc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class DefaultRecipesHandler {
    private static final Path RECIPE_FOLDER = FabricLoader.getInstance().getConfigDir().resolve("betterosc/recipes");
    public static final Path SOURCE_RECIPES_FOLDER = Betterosc.resourceRoot.resolve("files/recipes");

    public static final List<String> DEFAULT_RECIPES = List.of(
            // TODO: fill
    );

    public static void checkAndCreate() {
        if (Files.exists(RECIPE_FOLDER)) return; // if recipe folder exists, pass

        try {
            Files.createDirectories(RECIPE_FOLDER);

            DEFAULT_RECIPES.forEach(fileName -> {
                Path source = SOURCE_RECIPES_FOLDER.resolve(fileName);
                if (!Files.exists(source)) return;

                Path output = RECIPE_FOLDER.resolve(fileName);

                try {
                    Files.copy(source, output);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
