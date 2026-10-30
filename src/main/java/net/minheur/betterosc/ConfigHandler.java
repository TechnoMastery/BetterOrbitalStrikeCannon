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
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("betterosc").resolve("orbital_strike.json");

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
            e.printStackTrace();
            loadDefault();
        }
    }

    public static void save() {
        try (Writer writer = new FileWriter(CONFIG_PATH.toFile())) {
            GSON.toJson(config, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        public final double rodCastDelay = 0.75f;

        // railgun
        public final double railgunDelay = 4f;
        public final double railgunArrowVelocity = 1500f;
        public final double railgunSpreadRadius = 1.0f;
        // wolf
        public final int defaultWolfAmount = 150;
        // darkness
        public final int darknessRadius = 25;
        public final int darknessDuration = 5;
        // nuke
        public final int nukeRingAmount = 10;
        // crash pig
        public final int pigAmount = 5000;
        // concentrated
        public final int concentratedTnts = 50;
        // wither
        public final int skullAmount = 1000;
        public final double maximumSkullAngle = 20.0;
        private final String skullAngleDesc = "The angle defines where will look the highest skulls. 0 = horizontal, higher values mean lower, so a more concentrated shot on where the origin is. 0 min, 90 max (not recommended). This value is descriptive and not meant to be changed.";
        public final float skullRandomOffset = 2.0f;
        private final String skullRandomDesc = "Higher = more random. 0 = off. Value not meant to be changed (informative)";
    }
}
