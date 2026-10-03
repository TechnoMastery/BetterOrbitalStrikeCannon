package net.minheur.betterosc.crafts;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class DefaultRecipesHandler {
    private static final Path RECIPE_FOLDER = FabricLoader.getInstance().getConfigDir().resolve("betterosc/recipes");

    public static final List<String> DEFAULT_RECIPES = List.of(
            "fixed_stab.json"
    );

    public static void checkAndCreate() {
        if (Files.exists(RECIPE_FOLDER)) return; // if recipe folder exists, pass

        try {
            Files.createDirectories(RECIPE_FOLDER);

            DEFAULT_RECIPES.forEach(fileName -> {
                Path output = RECIPE_FOLDER.resolve(fileName);

                try (InputStream source = DefaultRecipesHandler.class.getClassLoader()
                        .getResourceAsStream("files/recipes/" + fileName)) {
                    if (source == null) {
                        throw new IOException("Missing bundled recipe resource: " + fileName);
                    }
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
