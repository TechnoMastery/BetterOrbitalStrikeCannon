package net.minheur.betterosc;


import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class UsedItemsHandler {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path SAVE_PATH = FabricLoader.getInstance().getConfigDir().resolve("used_ocs.json");
    private static final Set<UUID> usedItems = new HashSet<>();

    private static final Set<UUID> castedFishingRods = new HashSet<>();

    public static Set<UUID> getUsedItems() {
        return usedItems;
    }
    public static Set<UUID> getCastedFishingRods() {
        return castedFishingRods;
    }

    public static void load() {
        usedItems.clear();
        if (!Files.exists(SAVE_PATH)) return;
        try (Reader reader = new FileReader(SAVE_PATH.toFile())) {
            Type type = new TypeToken<Set<UUID>>(){}.getType();
            usedItems.addAll(GSON.fromJson(reader, type));
        } catch (Exception ignored) {}
    }
    public static void save() {
        try (Writer writer = new FileWriter(SAVE_PATH.toFile())) {
            GSON.toJson(usedItems, writer);
        } catch (Exception ignored) {}
    }

}
