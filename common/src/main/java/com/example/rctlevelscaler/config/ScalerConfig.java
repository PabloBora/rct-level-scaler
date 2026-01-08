package com.example.rctlevelscaler.config;

public class ScalerConfig {

    public boolean enableLogs = true;

    // base level mode: "max" o "avg2"
    public String baseMode = "max";

    // rango
    public int minus = 3;
    public int plus = 2;

    // seed mode: "none" o "real_day"
    public String seedMode = "real_day";

    public static ScalerConfig defaults() {
        return new ScalerConfig();
    }

    public ScalerConfig sanitize() {
        baseMode = (baseMode == null) ? "max" : baseMode.trim().toLowerCase();
        seedMode = (seedMode == null) ? "real_day" : seedMode.trim().toLowerCase();

        if (!baseMode.equals("max") && !baseMode.equals("avg2")) baseMode = "max";
        if (!seedMode.equals("none") && !seedMode.equals("real_day")) seedMode = "real_day";

        if (minus < 0) minus = 0;
        if (plus < 0) plus = 0;

        if (minus > 50) minus = 50;
        if (plus > 50) plus = 50;

        return this;
    }


    @Override
    public String toString() {
        return "ScalerConfig{" +
                "enableLogs=" + enableLogs +
                ", baseMode='" + baseMode + '\'' +
                ", minus=" + minus +
                ", plus=" + plus +
                ", seedMode='" + seedMode + '\'' +
                '}';
    }
}
