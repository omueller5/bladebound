package net.owen.bladebound;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BladeboundConfig {
    private BladeboundConfig() {}

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "bladebound.json";

    public static BladeboundConfigData DATA = new BladeboundConfigData();

    public static void load(Path configDir) {
        try {
            Path file = configDir.resolve(FILE_NAME);

            if (!Files.exists(file)) {
                save(configDir); // write defaults
                return;
            }

            String json = Files.readString(file);
            BladeboundConfigData loaded = GSON.fromJson(json, BladeboundConfigData.class);
            if (loaded != null) DATA = loaded;

        } catch (Exception e) {
            // If config is broken, keep defaults (don't crash the game)
            e.printStackTrace();
            DATA = new BladeboundConfigData();
        }
    }

    public static void save(Path configDir) {
        try {
            Files.createDirectories(configDir);
            Path file = configDir.resolve(FILE_NAME);
            Files.writeString(file, GSON.toJson(DATA));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // All your toggles live here
    public static final class BladeboundConfigData {
        // HUD
        public boolean hudEnabled = true;
        public int hudYOffset = -58;        // relative to bottom of screen (negative = up)
        public int hudWidth = 81;
        public int hudHeight = 8;
        public boolean hudShowText = true;

        // Perfect Form visuals
        public boolean perfectFormEffects = true;

        // Enchant rules
        public boolean enforceAllowedEnchantments = true;

        // Durability
        public boolean durabilityEnabled = true;
        public int durabilityPerHit = 1; // how much durability to consume per hit
    }
}
