package net.minheur.betterosc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class ConfigHandler {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("orbital_strike.json");

    private static ConfigData config;

    public static void load() {
        try {
            if (!Files.exists(CONFIG_PATH))
                createDefaultConfig();
            else {
                try (Reader reader = new FileReader(CONFIG_PATH.toFile())) {
                    config = GSON.fromJson(reader, ConfigData.class);
                    if (config == null)
                        createDefaultConfig();
                }
            }
        } catch (Exception e) {
            loadDefault();
        }
    }

    public static void save() {
        try (Writer writer = new FileWriter(CONFIG_PATH.toFile())) {
            GSON.toJson(config, writer);
        } catch (Exception ignored) {}
    }

    private static void loadDefault() {
        config = new ConfigData();
    }
    private static void createDefaultConfig() {
        loadDefault();
        save();
    }

    public static ConfigData getConfig() {
        return config;
    }

    public static class ConfigData {
        // illegal tank
        public List<String> illegalTanksItems = List.of(
                "minecraft:bedrock", "minecraft:barrier", "minecraft:command_block", "minecraft:structure_block",
                "minecraft:chain_command_block", "minecraft:repeat_command_block"
        );

        public double rodCastDelay = 0.75f;

        // railgun
        public double railgunDelay = 4f;
        public double railgunArrowVelocity = 1500f;
        public double railgunSpreadRadius = 1.0f;
        // wolf
        public int defaultWolfAmount = 150;
        // darkness
        public int darknessRadius = 25;
        public int darknessDuration = 5;
        // nuke
        public int nukeRingAmount = 10;
        // crash pig
        public int pigAmount = 5000;
    }
}
