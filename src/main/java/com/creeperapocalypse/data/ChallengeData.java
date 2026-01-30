package com.creeperapocalypse.data;

import com.creeperapocalypse.CreeperApocalypse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Persistent challenge data that tracks progress across game sessions
 * Includes current day, spawn multiplier, and milestone status
 */
public class ChallengeData {
    
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String DATA_FILE = "creeper_apocalypse_data.json";
    
    /** Current challenge day (starts at 1) */
    private int currentDay = 1;
    
    /** Current spawn multiplier */
    private float spawnMultiplier = 1.0f;
    
    /** Total creepers spawned */
    private long totalCreepersSpawned = 0;
    
    /** Total creepers killed */
    private long totalCreepersKilled = 0;
    
    /** Total explosions (from creepers) */
    private long totalExplosions = 0;
    
    /** Total player deaths to creepers */
    private long totalCreeperDeaths = 0;
    
    /** Highest day reached */
    private int highestDayReached = 1;
    
    /** Challenge start time (world time) */
    private long challengeStartTime = 0;
    
    /** Last recorded world time (for day change detection) */
    private long lastWorldTime = 0;
    
    /** Blood Moon milestone triggered */
    private boolean bloodMoonTriggered = false;
    
    /** Charged Day milestone triggered */
    private boolean chargedDayTriggered = false;
    
    /** Swarm Day milestone triggered */
    private boolean swarmDayTriggered = false;
    
    // ==================== GETTERS ====================
    
    public int getCurrentDay() { return currentDay; }
    public float getSpawnMultiplier() { return spawnMultiplier; }
    public long getTotalCreepersSpawned() { return totalCreepersSpawned; }
    public long getTotalCreepersKilled() { return totalCreepersKilled; }
    public long getTotalExplosions() { return totalExplosions; }
    public long getTotalCreeperDeaths() { return totalCreeperDeaths; }
    public int getHighestDayReached() { return highestDayReached; }
    public long getChallengeStartTime() { return challengeStartTime; }
    public long getLastWorldTime() { return lastWorldTime; }
    
    public boolean isBloodMoonTriggered() { return bloodMoonTriggered; }
    public boolean isChargedDayTriggered() { return chargedDayTriggered; }
    public boolean isSwarmDayTriggered() { return swarmDayTriggered; }
    
    // ==================== SETTERS ====================
    
    public void setCurrentDay(int day) {
        this.currentDay = day;
        if (day > highestDayReached) {
            highestDayReached = day;
        }
        updateSpawnMultiplier();
    }
    
    public void setLastWorldTime(long time) { this.lastWorldTime = time; }
    public void setChallengeStartTime(long time) { this.challengeStartTime = time; }
    
    public void setBloodMoonTriggered(boolean triggered) { this.bloodMoonTriggered = triggered; }
    public void setChargedDayTriggered(boolean triggered) { this.chargedDayTriggered = triggered; }
    public void setSwarmDayTriggered(boolean triggered) { this.swarmDayTriggered = triggered; }
    
    public void setHighestDayReached(int day) { this.highestDayReached = day; }
    public void setTotalCreepersSpawned(long count) { this.totalCreepersSpawned = count; }
    public void setTotalCreepersKilled(long count) { this.totalCreepersKilled = count; }
    public void setTotalExplosions(long count) { this.totalExplosions = count; }
    public void setTotalCreeperDeaths(long count) { this.totalCreeperDeaths = count; }
    
    // ==================== STATISTICS ====================
    
    public void incrementCreepersSpawned() { totalCreepersSpawned++; }
    public void incrementCreepersKilled() { totalCreepersKilled++; }
    public void incrementExplosions() { totalExplosions++; }
    public void incrementCreeperDeaths() { totalCreeperDeaths++; }
    
    public void addCreepersSpawned(int count) { totalCreepersSpawned += count; }
    public void addCreepersKilled(int count) { totalCreepersKilled += count; }
    
    // ==================== DAY PROGRESSION ====================
    
    /**
     * Advances to the next day
     */
    public void advanceDay() {
        currentDay++;
        if (currentDay > highestDayReached) {
            highestDayReached = currentDay;
        }
        updateSpawnMultiplier();
        resetDailyMilestones();
        
        CreeperApocalypse.LOGGER.info("Day advanced to " + currentDay + " - Spawn multiplier: " + spawnMultiplier + "x");
    }
    
    /**
     * Updates spawn multiplier based on current day and config
     */
    private void updateSpawnMultiplier() {
        float base = CreeperApocalypse.CONFIG.getBaseSpawnMultiplier();
        float rate = CreeperApocalypse.CONFIG.getEscalationRate();
        float speed = CreeperApocalypse.CONFIG.getEscalationSpeed();
        float max = CreeperApocalypse.CONFIG.getMaxSpawnMultiplier();
        
        // Calculate multiplier: base + (day - 1) * rate * speed
        spawnMultiplier = base + (currentDay - 1) * rate * speed;
        
        // Cap at maximum
        spawnMultiplier = Math.min(spawnMultiplier, max);
    }
    
    /**
     * Resets daily milestone flags for the new day
     */
    private void resetDailyMilestones() {
        // Milestones are checked fresh each day
        bloodMoonTriggered = false;
        chargedDayTriggered = false;
        swarmDayTriggered = false;
    }
    
    // ==================== RESET ====================
    
    /**
     * Resets the challenge to day 1
     */
    public void reset() {
        currentDay = 1;
        spawnMultiplier = CreeperApocalypse.CONFIG.getBaseSpawnMultiplier();
        totalCreepersSpawned = 0;
        totalCreepersKilled = 0;
        totalExplosions = 0;
        totalCreeperDeaths = 0;
        challengeStartTime = 0;
        lastWorldTime = 0;
        bloodMoonTriggered = false;
        chargedDayTriggered = false;
        swarmDayTriggered = false;
        // Note: highestDayReached is preserved as a record
        
        CreeperApocalypse.LOGGER.info("Challenge reset to Day 1!");
    }
    
    // ==================== PERSISTENCE ====================
    
    /**
     * Loads challenge data from the world save
     */
    public static ChallengeData load(MinecraftServer server) {
        Path dataPath = server.getSavePath(WorldSavePath.ROOT).resolve(DATA_FILE);
        
        if (Files.exists(dataPath)) {
            try {
                String json = Files.readString(dataPath);
                ChallengeData data = GSON.fromJson(json, ChallengeData.class);
                CreeperApocalypse.LOGGER.info("Challenge data loaded - Day " + data.currentDay);
                return data;
            } catch (IOException e) {
                CreeperApocalypse.LOGGER.error("Failed to load challenge data: " + e.getMessage());
            }
        }
        
        // Return new data if none exists
        ChallengeData data = new ChallengeData();
        data.updateSpawnMultiplier();
        return data;
    }
    
    /**
     * Saves challenge data to the world save
     */
    public void save(MinecraftServer server) {
        Path dataPath = server.getSavePath(WorldSavePath.ROOT).resolve(DATA_FILE);
        
        try {
            Files.writeString(dataPath, GSON.toJson(this));
            CreeperApocalypse.LOGGER.info("Challenge data saved - Day " + currentDay);
        } catch (IOException e) {
            CreeperApocalypse.LOGGER.error("Failed to save challenge data: " + e.getMessage());
        }
    }
    
    // ==================== UTILITY ====================
    
    /**
     * Gets a formatted stats summary
     */
    public String getStatsSummary() {
        return String.format(
            "Day %d | Multiplier: %.1fx | Spawned: %d | Killed: %d | Deaths: %d | Explosions: %d",
            currentDay, spawnMultiplier, totalCreepersSpawned, totalCreepersKilled,
            totalCreeperDeaths, totalExplosions
        );
    }
    
    /**
     * Creates a copy for networking
     */
    public ChallengeData copy() {
        ChallengeData copy = new ChallengeData();
        copy.currentDay = this.currentDay;
        copy.spawnMultiplier = this.spawnMultiplier;
        copy.totalCreepersSpawned = this.totalCreepersSpawned;
        copy.totalCreepersKilled = this.totalCreepersKilled;
        copy.totalExplosions = this.totalExplosions;
        copy.totalCreeperDeaths = this.totalCreeperDeaths;
        copy.highestDayReached = this.highestDayReached;
        copy.challengeStartTime = this.challengeStartTime;
        copy.bloodMoonTriggered = this.bloodMoonTriggered;
        copy.chargedDayTriggered = this.chargedDayTriggered;
        copy.swarmDayTriggered = this.swarmDayTriggered;
        return copy;
    }
}
