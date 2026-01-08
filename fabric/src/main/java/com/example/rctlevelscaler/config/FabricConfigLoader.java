package com.example.rctlevelscaler.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FabricConfigLoader {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "rctlevelscaler.json";

    private FabricConfigLoader() {}

    public static ScalerConfig loadOrCreateDefault() {
        try {
            Path configDir = FabricLoader.getInstance().getConfigDir();
            Path file = configDir.resolve(FILE_NAME);

            if (!Files.exists(file)) {
                ScalerConfig def = ScalerConfig.defaults().sanitize();
                write(file, def);
                return def;
            }

            try (Reader r = Files.newBufferedReader(file)) {
                ScalerConfig cfg = GSON.fromJson(r, ScalerConfig.class);
                if (cfg == null) cfg = ScalerConfig.defaults();
                cfg = cfg.sanitize();
                write(file, cfg); // auto-normaliza/rellena campos
                return cfg;
            }
        } catch (Throwable t) {
            System.out.println("[RCT-Level-Scaler] Config load failed, using defaults: " + t);
            return ScalerConfig.defaults().sanitize();
        }
    }

    private static void write(Path file, ScalerConfig cfg) throws Exception {
        Files.createDirectories(file.getParent());
        try (Writer w = Files.newBufferedWriter(file)) {
            GSON.toJson(cfg, w);
        }
    }
}
