package com.creeperapocalypse.config;

import com.creeperapocalypse.CreeperApocalypse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Configuration class for Creeper Apocalypse Escalation
 * Handles all configurable settings including spawn rates, escalation, and features
 */
public class CreeperConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE = "creeper-apocalypse.json";

    // ==================== GENERAL SETTINGS ====================

    /** Whether the challenge is enabled */
    private boolean enabled = true;

    /** Show welcome message on player join */
    private boolean showWelcomeMessage = true;

    // ==================== ESCALATION SETTINGS ====================

    /** Base spawn multiplier (Day 1) */
    private float baseSpawnMultiplier = 1.0f;

    /** Maximum spawn multiplier cap */
    private float maxSpawnMultiplier = 10.0f;

    /** Multiplier increase per day */
    private float escalationRate = 1.0f;

    /** Escalation speed: 0.5x (slow), 1.0x (normal), 2.0x (fast) */
    private float escalationSpeed = 1.0f;

    // ==================== MOB REPLACEMENT SETTINGS ====================

    /** Replace hostile mobs with creepers */
    private boolean replaceHostileMobs = true;

    /** Replace passive mobs with creepers */
    private boolean replacePassiveMobs = true;

    /** Replace neutral mobs with creepers */
    private boolean replaceNeutralMobs = true;

    /** Keep Blazes (exception) */
    private boolean keepBlazes = true;

    /** Keep Villagers (exception) */
    private boolean keepVillagers = true;

    /** Keep Ender Dragon (exception) */
    private boolean keepEnderDragon = true;

    /** Keep Iron Golems for village defense */
    private boolean keepIronGolems = true;

    // ==================== MILESTONE SETTINGS ====================

    /** Enable milestone events */
    private boolean milestonesEnabled = true;

    /** Day 5: Blood Moon - all creepers glow red */
    private boolean bloodMoonEnabled = true;

    /** Day 7: Charged Day - 25% spawn as charged */
    private boolean chargedDayEnabled = true;
    private float chargedDayChance = 0.25f;

    /** Day 10: The Swarm - special announcement/effect */
    private boolean swarmDayEnabled = true;

    // ==================== SPECIAL CREEPER VARIANTS ====================

    /** Enable special creeper variants */
    private boolean specialVariantsEnabled = true;

    /** Mini creeper spawn chance (0.0 - 1.0) */
    private float miniCreeperChance = 0.1f;

    /** Giant creeper spawn chance (0.0 - 1.0) */
    private float giantCreeperChance = 0.05f;

    /** Spider creeper spawn chance (0.0 - 1.0) */
    private float spiderCreeperChance = 0.08f;

    /** Ninja creeper spawn chance (0.0 - 1.0) */
    private float ninjaCreeperChance = 0.08f;

    /** Rainbow creeper spawn chance (0.0 - 1.0) */
    private float rainbowCreeperChance = 0.07f;

    /** Bouncy creeper spawn chance (0.0 - 1.0) */
    private float bouncyCreeperChance = 0.06f;

    /** Jockey creeper spawn chance (0.0 - 1.0) */
    private float jockeyCreeperChance = 0.05f;

    /** Happy creeper spawn chance (0.0 - 1.0) */
    private float happyCreeperChance = 0.04f;

    /** Lightning creeper spawn chance (0.0 - 1.0) */
    private float lightningCreeperChance = 0.02f;

    // ==================== QUALITY OF LIFE ====================

    /** Enable death counter for creeper deaths */
    private boolean deathCounterEnabled = true;

    /** Enable statistics tracking */
    private boolean statsTrackingEnabled = true;

    /** Enable near miss counter */
    private boolean nearMissCounterEnabled = true;
    private float nearMissDistance = 3.0f;

    /** Respawn with items (for practice) */
    private boolean keepInventoryOnDeath = false;

    // ==================== STREAMING/CONTENT FEATURES ====================

    /** Auto-screenshot on death */
    private boolean autoScreenshotOnDeath = false;

    /** Show HUD overlay */
    private boolean showHudOverlay = true;

    /** Death replay system (records last 5 seconds) */
    private boolean deathReplayEnabled = false;
    private int deathReplaySeconds = 5;

    /** Chat command prefix for stats display */
    private String chatCommandPrefix = "!creeper";

    // ==================== NEW GAME MODES ====================

    /** NO DAY MODE - ignore day progression, all creepers spawn with equal chance */
    private boolean ignoreDayProgression = false;

    // ==================== SPAWN LIMITS ====================

    /** Max creepers in Overworld (1-1000) */
    private int maxCreepersOverworld = 200;

    /** Max creepers in Nether (1-1000) */
    private int maxCreepersNether = 200;

    /** Max creepers in End (1-1000) */
    private int maxCreepersEnd = 200;

    // ==================== GETTERS ====================

    public boolean isEnabled() { return enabled; }
    public boolean showWelcomeMessage() { return showWelcomeMessage; }

    public float getBaseSpawnMultiplier() { return baseSpawnMultiplier; }
    public float getMaxSpawnMultiplier() { return maxSpawnMultiplier; }
    public float getEscalationRate() { return escalationRate; }
    public float getEscalationSpeed() { return escalationSpeed; }

    public boolean replaceHostileMobs() { return replaceHostileMobs; }
    public boolean replacePassiveMobs() { return replacePassiveMobs; }
    public boolean replaceNeutralMobs() { return replaceNeutralMobs; }
    public boolean keepBlazes() { return keepBlazes; }
    public boolean keepVillagers() { return keepVillagers; }
    public boolean keepEnderDragon() { return keepEnderDragon; }
    public boolean keepIronGolems() { return keepIronGolems; }

    public boolean milestonesEnabled() { return milestonesEnabled; }
    public boolean bloodMoonEnabled() { return bloodMoonEnabled; }
    public boolean chargedDayEnabled() { return chargedDayEnabled; }
    public float getChargedDayChance() { return chargedDayChance; }
    public boolean swarmDayEnabled() { return swarmDayEnabled; }

    public boolean specialVariantsEnabled() { return specialVariantsEnabled; }
    public float getMiniCreeperChance() { return miniCreeperChance; }
    public float getGiantCreeperChance() { return giantCreeperChance; }
    public float getSpiderCreeperChance() { return spiderCreeperChance; }
    public float getNinjaCreeperChance() { return ninjaCreeperChance; }
    public float getRainbowCreeperChance() { return rainbowCreeperChance; }
    public float getBouncyCreeperChance() { return bouncyCreeperChance; }
    public float getJockeyCreeperChance() { return jockeyCreeperChance; }
    public float getHappyCreeperChance() { return happyCreeperChance; }
    public float getLightningCreeperChance() { return lightningCreeperChance; }

    public boolean deathCounterEnabled() { return deathCounterEnabled; }
    public boolean statsTrackingEnabled() { return statsTrackingEnabled; }
    public boolean nearMissCounterEnabled() { return nearMissCounterEnabled; }
    public float getNearMissDistance() { return nearMissDistance; }
    public boolean keepInventoryOnDeath() { return keepInventoryOnDeath; }

    public boolean autoScreenshotOnDeath() { return autoScreenshotOnDeath; }
    public boolean deathReplayEnabled() { return deathReplayEnabled; }
    public int getDeathReplaySeconds() { return deathReplaySeconds; }
    public String getChatCommandPrefix() { return chatCommandPrefix; }

    // ==================== SETTERS ====================

    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setShowWelcomeMessage(boolean show) { this.showWelcomeMessage = show; }

    public void setBaseSpawnMultiplier(float mult) { this.baseSpawnMultiplier = clampNonNegative(mult, 1.0f); }
    public void setMaxSpawnMultiplier(float max) { this.maxSpawnMultiplier = Math.max(1.0f, Math.min(20.0f, max)); }
    public void setEscalationRate(float rate) { this.escalationRate = clampNonNegative(rate, 1.0f); }
    public void setEscalationSpeed(float speed) { this.escalationSpeed = clampNonNegative(speed, 1.0f); }

    public void setReplaceHostileMobs(boolean replace) { this.replaceHostileMobs = replace; }
    public void setReplacePassiveMobs(boolean replace) { this.replacePassiveMobs = replace; }
    public void setReplaceNeutralMobs(boolean replace) { this.replaceNeutralMobs = replace; }

    public void setMilestonesEnabled(boolean enabled) { this.milestonesEnabled = enabled; }
    public void setBloodMoonEnabled(boolean enabled) { this.bloodMoonEnabled = enabled; }
    public void setChargedDayEnabled(boolean enabled) { this.chargedDayEnabled = enabled; }
    public void setChargedDayChance(float chance) { this.chargedDayChance = clampChance(chance); }
    public void setSwarmDayEnabled(boolean enabled) { this.swarmDayEnabled = enabled; }

    public void setSpecialVariantsEnabled(boolean enabled) { this.specialVariantsEnabled = enabled; }
    public void setMiniCreeperChance(float chance) { this.miniCreeperChance = clampChance(chance); }
    public void setGiantCreeperChance(float chance) { this.giantCreeperChance = clampChance(chance); }
    public void setSpiderCreeperChance(float chance) { this.spiderCreeperChance = clampChance(chance); }
    public void setNinjaCreeperChance(float chance) { this.ninjaCreeperChance = clampChance(chance); }
    public void setRainbowCreeperChance(float chance) { this.rainbowCreeperChance = clampChance(chance); }
    public void setBouncyCreeperChance(float chance) { this.bouncyCreeperChance = clampChance(chance); }
    public void setJockeyCreeperChance(float chance) { this.jockeyCreeperChance = clampChance(chance); }
    public void setHappyCreeperChance(float chance) { this.happyCreeperChance = clampChance(chance); }
    public void setLightningCreeperChance(float chance) { this.lightningCreeperChance = clampChance(chance); }

    public void setDeathCounterEnabled(boolean enabled) { this.deathCounterEnabled = enabled; }
    public void setStatsTrackingEnabled(boolean enabled) { this.statsTrackingEnabled = enabled; }
    public void setNearMissCounterEnabled(boolean enabled) { this.nearMissCounterEnabled = enabled; }
    public void setNearMissDistance(float distance) { this.nearMissDistance = clampNonNegative(distance, 3.0f); }
    public void setKeepInventoryOnDeath(boolean keep) { this.keepInventoryOnDeath = keep; }

    public void setAutoScreenshotOnDeath(boolean enabled) { this.autoScreenshotOnDeath = enabled; }
    public boolean keepInventoryOnCreeperDeath() { return keepInventoryOnDeath; }

    public boolean showHudOverlay() { return showHudOverlay; }
    public void setShowHudOverlay(boolean show) { this.showHudOverlay = show; }

    public void setDeathReplayEnabled(boolean enabled) { this.deathReplayEnabled = enabled; }

    private float clampChance(float value) {
        if (!Float.isFinite(value)) {
            return 0.0f;
        }
        if (value < 0.0f) {
            return 0.0f;
        }
        if (value > 1.0f) {
            return 1.0f;
        }
        return value;
    }

    private float clampNonNegative(float value, float fallback) {
        if (!Float.isFinite(value)) {
            return fallback;
        }
        return Math.max(0.0f, value);
    }

    private int clampIntRange(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    // ==================== LOAD/SAVE ====================

    private static Path getGlobalConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE);
    }

    private static Path getWorldConfigPath(MinecraftServer server) {
        return server.getSavePath(WorldSavePath.ROOT).resolve(CONFIG_FILE);
    }

    private static CreeperConfig tryLoadFromPath(Path configPath) {
        if (!Files.exists(configPath)) {
            return null;
        }

        try {
            String json = Files.readString(configPath);
            CreeperConfig config = fromJson(json);
            if (config == null) {
                CreeperApocalypse.LOGGER.error("Config file exists but could not be parsed: " + configPath);
            }
            return config;
        } catch (IOException e) {
            CreeperApocalypse.LOGGER.error("Failed to load config from " + configPath + ": " + e.getMessage());
            return null;
        }
    }

    private void saveToPath(Path configPath) {
        try {
            Path parent = configPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(configPath, GSON.toJson(this));
            CreeperApocalypse.LOGGER.info("Configuration saved to " + configPath);
        } catch (IOException e) {
            CreeperApocalypse.LOGGER.error("Failed to save config to " + configPath + ": " + e.getMessage());
        }
    }

    /**
     * Loads configuration from file or creates default
     */
    public static CreeperConfig load() {
        Path configPath = getGlobalConfigPath();
        CreeperConfig config = tryLoadFromPath(configPath);
        if (config != null) {
            CreeperApocalypse.LOGGER.info("Configuration loaded from " + configPath);
            return config;
        }

        // Create default config
        CreeperConfig defaults = new CreeperConfig();
        defaults.save();
        return defaults;
    }

    /**
     * Loads world-scoped configuration, migrating legacy global config once if needed.
     */
    public static CreeperConfig load(MinecraftServer server) {
        Path worldConfigPath = getWorldConfigPath(server);
        CreeperConfig worldConfig = tryLoadFromPath(worldConfigPath);
        if (worldConfig != null) {
            CreeperApocalypse.LOGGER.info("World configuration loaded from " + worldConfigPath);
            return worldConfig;
        }

        Path globalConfigPath = getGlobalConfigPath();
        CreeperConfig legacyConfig = tryLoadFromPath(globalConfigPath);
        if (legacyConfig != null) {
            legacyConfig.save(server);
            CreeperApocalypse.LOGGER.info("Migrated legacy global config into world config: " + worldConfigPath);
            return legacyConfig;
        }

        CreeperConfig defaults = new CreeperConfig();
        defaults.save(server);
        CreeperApocalypse.LOGGER.info("Created default world configuration at " + worldConfigPath);
        return defaults;
    }

    public static CreeperConfig fromJson(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }

        try {
            CreeperConfig config = GSON.fromJson(json, CreeperConfig.class);
            if (config != null) {
                config.normalizeAfterLoad();
            }
            return config;
        } catch (Exception e) {
            CreeperApocalypse.LOGGER.error("Failed to parse config update payload: " + e.getMessage());
            return null;
        }
    }

    /**
     * Gson field deserialization bypasses setter guards, so re-apply bounds here.
     */
    private void normalizeAfterLoad() {
        setBaseSpawnMultiplier(baseSpawnMultiplier);
        setMaxSpawnMultiplier(maxSpawnMultiplier);
        setEscalationRate(escalationRate);
        setEscalationSpeed(escalationSpeed);
        setChargedDayChance(chargedDayChance);

        setMiniCreeperChance(miniCreeperChance);
        setGiantCreeperChance(giantCreeperChance);
        setSpiderCreeperChance(spiderCreeperChance);
        setNinjaCreeperChance(ninjaCreeperChance);
        setRainbowCreeperChance(rainbowCreeperChance);
        setBouncyCreeperChance(bouncyCreeperChance);
        setJockeyCreeperChance(jockeyCreeperChance);
        setHappyCreeperChance(happyCreeperChance);
        setLightningCreeperChance(lightningCreeperChance);

        setNearMissDistance(nearMissDistance);
        deathReplaySeconds = clampIntRange(deathReplaySeconds, 1, 60);

        setMaxCreepersOverworld(maxCreepersOverworld);
        setMaxCreepersNether(maxCreepersNether);
        setMaxCreepersEnd(maxCreepersEnd);

        if (chatCommandPrefix == null || chatCommandPrefix.isBlank()) {
            chatCommandPrefix = "!creeper";
        }
    }

    public boolean ignoreDayProgression() { return ignoreDayProgression; }
    public void setIgnoreDayProgression(boolean ignoreDayProgression) { this.ignoreDayProgression = ignoreDayProgression; }

    public int getMaxCreepersOverworld() { return maxCreepersOverworld; }
    public void setMaxCreepersOverworld(int max) { this.maxCreepersOverworld = clampLimit(max); }

    public int getMaxCreepersNether() { return maxCreepersNether; }
    public void setMaxCreepersNether(int max) { this.maxCreepersNether = clampLimit(max); }

    public int getMaxCreepersEnd() { return maxCreepersEnd; }
    public void setMaxCreepersEnd(int max) { this.maxCreepersEnd = clampLimit(max); }

    private int clampLimit(int val) {
        return Math.max(1, Math.min(1000, val));
    }

    /**
     * Saves configuration to file
     */
    public void save() {
        saveToPath(getGlobalConfigPath());
    }

    /**
     * Saves configuration to the active world save.
     */
    public void save(MinecraftServer server) {
        saveToPath(getWorldConfigPath(server));
    }
}

