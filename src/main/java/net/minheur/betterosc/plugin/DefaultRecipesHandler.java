package net.minheur.betterosc.plugin;

import net.minheur.betterosc.Betterosc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class DefaultRecipesHandler {
    private static final Path RECIPE_FOLDER =
            net.minheur.commandcrafter.crafts.DefaultRecipesHandler
                    .RECIPE_FOLDER.resolve("betterOSC");
    private static final Logger LOGGER = LoggerFactory.getLogger(Betterosc.MODID + "/crafter");

    public static final List<String> DEFAULT_RECIPES = List.of(
            "README.md",
            "concentrated/fixed.json", "concentrated/mobile.json",
            "nuke/fixed.json", "nuke/mobile.json",
            "stab/fixed.json", "stab/mobile.json",
            "stasis/fixed.json", "stasis/mobile.json",
            "totem_stasis/fixed.json", "totem_stasis/mobile.json",
            "wither/fixed.json", "wither/mobile.json",
            "wolves/fixed.json", "wolves/mobile.json",
            "crashpig.json",
            "darkness.json",
            "railgun.json"
    );

    public static void checkAndCreate() {
        if (Files.exists(RECIPE_FOLDER)) return; // if recipe folder exists, pass

        try {
            Files.createDirectories(RECIPE_FOLDER);

            DEFAULT_RECIPES.forEach(fileName -> {
                Path output = RECIPE_FOLDER.resolve(fileName);

                try {
                    Files.createDirectories(output.getParent());
                } catch (IOException e) {
                    LOGGER.error("Failed to create recipe folder", e);
                    return;
                }

                try (InputStream source = DefaultRecipesHandler.class.getClassLoader()
                        .getResourceAsStream("files/recipes/" + fileName)) {
                    if (source == null) {
                        throw new IOException("Missing bundled recipe resource: " + fileName);
                    }
                    Files.copy(source, output);
                } catch (IOException e) {
                    LOGGER.error("Failed to copy files", e);
                }
            });

        } catch (IOException e) {
            LOGGER.error("A file writing error happened", e);
        }
    }

}
