package net.minheur.betterosc.recipes;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public final class RecipeLoader {
    public static final Gson GSON = new Gson();
    private static final Path RECIPE_FOLDER = FabricLoader.getInstance().getConfigDir().resolve("betterosc").resolve("recipes");

    private RecipeLoader() {}

    public static @NonNull List<Recipe> loadAll() {
        try {

            if (!Files.exists(RECIPE_FOLDER)) {
                Files.createDirectories(RECIPE_FOLDER);
                return Collections.emptyList();
            }

            List<Recipe> recipes = new ArrayList<>();
            try (Stream<Path> paths = Files.walk(RECIPE_FOLDER)) {
                paths.filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().endsWith(".json"))
                        .forEach(path -> {
                            try {
                                recipes.add(loadFile(path));
                            } catch (Exception ignored) {}
                        });
            }

            return recipes;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load recipes from " + RECIPE_FOLDER, e);
        }
    }

    public static @NonNull Recipe loadFile(Path path) {
        Objects.requireNonNull(path, "path");

        try {
            String content = Files.readString(path);
            return decode(content);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read recipe file: " + path, e);
        }
    }

    public static Recipe decode(String content) {
        if (content == null || content.isBlank())
            throw new IllegalArgumentException("Recipe content cannot be empty");

        JsonObject json;
        try {
            json = GSON.fromJson(content, JsonObject.class);
        } catch (JsonParseException e) {
            throw new IllegalArgumentException("Invalid recipe JSON", e);
        }

        if (json == null || json.isJsonNull())
            throw new IllegalArgumentException("Recipe JSON cannot be null");

        return Recipe.fromJson(json);
    }
}
