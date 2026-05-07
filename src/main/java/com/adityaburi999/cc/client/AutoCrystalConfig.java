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

    public static final double MAX_CPS_MIN = 2.0;
    public static final double MAX_CPS_MAX = 20.0;
    public static final double MIN_FACTOR_MIN = 0.3;
    public static final double MIN_FACTOR_MAX = 1.0;
    public static final double MISCLICK_MIN = 0.0;
    public static final double MISCLICK_MAX = 0.3;

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
            } catch (IOException e) {
                LOGGER.warn("Failed to load config, using defaults.", e);
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
        } catch (IOException e) {
            LOGGER.warn("Failed to save config.", e);
        }
    }

    public double minCps() {
        return Math.max(1.0, maxCps * minCpsFactor);
    }

    private void normalize() {
        if (maxCps < MAX_CPS_MIN) {
            maxCps = MAX_CPS_MIN;
        } else if (maxCps > MAX_CPS_MAX) {
            maxCps = MAX_CPS_MAX;
        }
        if (minCpsFactor < MIN_FACTOR_MIN) {
            minCpsFactor = MIN_FACTOR_MIN;
        } else if (minCpsFactor > MIN_FACTOR_MAX) {
            minCpsFactor = MIN_FACTOR_MAX;
        }
        if (misclickChance < MISCLICK_MIN) {
            misclickChance = MISCLICK_MIN;
        } else if (misclickChance > MISCLICK_MAX) {
            misclickChance = MISCLICK_MAX;
        }
    }
}
