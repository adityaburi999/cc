package com.adityaburi999.cc.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoCrystalConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("cc.json");
    private static final Logger LOGGER = LoggerFactory.getLogger("CrystalClicker");

    public boolean enabled = true;
    public double maxCps = 12.0;
    public double minCpsFactor = 0.65;
    public double misclickChance = 0.05;

    public static AutoCrystalConfig load() {
        AutoCrystalConfig config = new AutoCrystalConfig();
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                AutoCrystalConfig loaded = GSON.fromJson(reader, AutoCrystalConfig.class);
                if (loaded != null) {
                    config = loaded;
                }
            } catch (IOException ignored) {
                LOGGER.warn("Failed to load config, using defaults.", ignored);
                config = new AutoCrystalConfig();
            }
        }
        config.normalize();
        return config;
    }

    public void save() {
        normalize();
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException ignored) {
            LOGGER.warn("Failed to save config.", ignored);
        }
    }

    public double minCps() {
        return Math.max(1.0, maxCps * minCpsFactor);
    }

    private void normalize() {
        if (maxCps < 1.0) {
            maxCps = 1.0;
        }
        if (minCpsFactor < 0.1) {
            minCpsFactor = 0.1;
        } else if (minCpsFactor > 1.0) {
            minCpsFactor = 1.0;
        }
        if (misclickChance < 0.0) {
            misclickChance = 0.0;
        } else if (misclickChance > 0.5) {
            misclickChance = 0.5;
        }
    }
}
