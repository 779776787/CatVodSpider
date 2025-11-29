package com.github.catvod.binrunner.config;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

/**
 * Main configuration bean for envs.json.
 * Contains all settings and alias mappings for BinRunner.
 */
public class EnvsConfig {

    @SerializedName("settings")
    private SettingsConfig settings;

    @SerializedName("alias")
    private Map<String, String> alias;

    public EnvsConfig() {
        this.settings = new SettingsConfig();
        this.alias = new HashMap<>();
    }

    /**
     * Parse configuration from JSON string.
     *
     * @param json JSON configuration string
     * @return parsed EnvsConfig or default config if parsing fails
     */
    public static EnvsConfig fromJson(String json) {
        try {
            EnvsConfig config = new Gson().fromJson(json, EnvsConfig.class);
            return config != null ? config : new EnvsConfig();
        } catch (Exception e) {
            return new EnvsConfig();
        }
    }

    public SettingsConfig getSettings() {
        return settings != null ? settings : new SettingsConfig();
    }

    public void setSettings(SettingsConfig settings) {
        this.settings = settings;
    }

    public Map<String, String> getAlias() {
        return alias != null ? alias : new HashMap<>();
    }

    public void setAlias(Map<String, String> alias) {
        this.alias = alias;
    }

    /**
     * Resolve an alias to its actual command.
     *
     * @param name command name or alias
     * @return resolved command name
     */
    public String resolveAlias(String name) {
        if (alias != null && alias.containsKey(name)) {
            return alias.get(name);
        }
        return name;
    }
}
