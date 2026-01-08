package com.example.rctlevelscaler.config;

public final class ConfigState {

    private static volatile ScalerConfig CURRENT = ScalerConfig.defaults();

    private ConfigState() {}

    public static ScalerConfig get() {
        return CURRENT;
    }

    public static void set(ScalerConfig cfg) {
        CURRENT = (cfg == null) ? ScalerConfig.defaults() : cfg;
    }

}
