package com.example.rctlevelscaler;

import com.example.rctlevelscaler.config.ConfigState;
import com.example.rctlevelscaler.config.FabricConfigLoader;
import com.example.rctlevelscaler.config.ScalerConfig;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RctLevelScaler implements ModInitializer {
    public static final String MOD_ID = "rctlevelscaler";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // Cargar config y publicarla para el resto del mod
        ScalerConfig cfg = FabricConfigLoader.loadOrCreateDefault();
        ConfigState.set(cfg);

        // Logs solo si está habilitado en config
        if (cfg.enableLogs) {
            LOGGER.info("Loaded config: {}", cfg);
        }
    }
}
