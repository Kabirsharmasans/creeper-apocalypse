package com.creeperapocalypse.data;

import com.creeperapocalypse.CreeperApocalypse;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks per-player statistics for the Creeper Apocalypse challenge
 */
public class PlayerStats {
    
    // Static cache of player stats
    private static final Map<UUID, PlayerStats> PLAYER_STATS = new ConcurrentHashMap<>();
    
    // Player identification
    UUID playerId;
    String playerName;
    
    // Death and survival stats
    int creeperDeaths = 0;
    int creepersKilled = 0;
    int explosionsSurvived = 0;
    int nearMisses = 0; // Times within 3 blocks of explosion
    float closestNearMiss = Float.MAX_VALUE;
    
    // Variant kills
    int miniCreepersKilled = 0;
    int giantCreepersKilled = 0;
    int spiderCreepersKilled = 0;
    int chargedCreepersKilled = 0;
    
    // Survival streaks
    int longestSurvivalStreak = 0; // Days survived without dying
    int currentSurvivalStreak = 0;
    
    // Time tracking
    long playTimeTicks = 0;
    
    // Session stats (not persisted)
    private transient int sessionDeaths = 0;
    private transient int sessionKills = 0;
    private transient long sessionStartTime = System.currentTimeMillis();
    
    /**
     * Default constructor for deserialization
     */
    public PlayerStats() {
        this.playerId = UUID.randomUUID();
        this.playerName = "Unknown";
    }
    
    /**
     * Creates new stats for a player
     */
    public PlayerStats(PlayerEntity player) {
        this.playerId = player.getUuid();
        this.playerName = player.getName().getString();
    }
    
    // ==================== RECORDING EVENTS ====================
    
    /**
     * Records a death by creeper explosion
     */
    public void recordCreeperDeath() {
        creeperDeaths++;
        sessionDeaths++;
        currentSurvivalStreak = 0;
        CreeperApocalypse.LOGGER.debug("Player " + playerName + " died to creeper (total: " + creeperDeaths + ")");
    }
    
    /**
     * Records a creeper kill
     */
    public void recordCreeperKill(String variantType) {
        creepersKilled++;
        sessionKills++;
        
        switch (variantType.toLowerCase()) {
            case "mini" -> miniCreepersKilled++;
            case "giant" -> giantCreepersKilled++;
            case "spider" -> spiderCreepersKilled++;
            case "charged" -> chargedCreepersKilled++;
        }
    }
    
    /**
     * Records surviving an explosion within damage range
     */
    public void recordExplosionSurvived() {
        explosionsSurvived++;
    }
    
    /**
     * Records a near miss (within 3 blocks of explosion)
     */
    public void recordNearMiss(float distance) {
        nearMisses++;
        if (distance < closestNearMiss) {
            closestNearMiss = distance;
        }
    }
    
    /**
     * Updates the survival streak (called on day change)
     */
    public void updateSurvivalStreak() {
        currentSurvivalStreak++;
        if (currentSurvivalStreak > longestSurvivalStreak) {
            longestSurvivalStreak = currentSurvivalStreak;
        }
    }
    
    /**
     * Updates play time
     */
    public void tick() {
        playTimeTicks++;
    }
    
    // ==================== GETTERS ====================
    
    public int getCreeperDeaths() { return creeperDeaths; }
    public int getCreepersKilled() { return creepersKilled; }
    public int getExplosionsSurvived() { return explosionsSurvived; }
    public int getNearMisses() { return nearMisses; }
    public float getClosestNearMiss() { return closestNearMiss; }
    public int getMiniCreepersKilled() { return miniCreepersKilled; }
    public int getGiantCreepersKilled() { return giantCreepersKilled; }
    public int getSpiderCreepersKilled() { return spiderCreepersKilled; }
    public int getChargedCreepersKilled() { return chargedCreepersKilled; }
    public int getLongestSurvivalStreak() { return longestSurvivalStreak; }
    public int getCurrentSurvivalStreak() { return currentSurvivalStreak; }
    public long getPlayTimeTicks() { return playTimeTicks; }
    public int getSessionDeaths() { return sessionDeaths; }
    public int getSessionKills() { return sessionKills; }
    
    /**
     * Gets play time formatted as hours:minutes:seconds
     */
    public String getPlayTimeFormatted() {
        long seconds = playTimeTicks / 20;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        return String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60);
    }
    
    /**
     * Calculates K/D ratio
     */
    public float getKDRatio() {
        if (creeperDeaths == 0) return creepersKilled;
        return (float) creepersKilled / creeperDeaths;
    }
    
    // ==================== NBT SERIALIZATION ====================
    
    /**
     * Serializes stats to NBT for saving
     */
    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("playerId", playerId.toString());
        nbt.putString("playerName", playerName);
        nbt.putInt("creeperDeaths", creeperDeaths);
        nbt.putInt("creepersKilled", creepersKilled);
        nbt.putInt("explosionsSurvived", explosionsSurvived);
        nbt.putInt("nearMisses", nearMisses);
        nbt.putFloat("closestNearMiss", closestNearMiss);
        nbt.putInt("miniCreepersKilled", miniCreepersKilled);
        nbt.putInt("giantCreepersKilled", giantCreepersKilled);
        nbt.putInt("spiderCreepersKilled", spiderCreepersKilled);
        nbt.putInt("chargedCreepersKilled", chargedCreepersKilled);
        nbt.putInt("longestSurvivalStreak", longestSurvivalStreak);
        nbt.putInt("currentSurvivalStreak", currentSurvivalStreak);
        nbt.putLong("playTimeTicks", playTimeTicks);
        return nbt;
    }
    
    /**
     * Deserializes stats from NBT
     */
    public static PlayerStats fromNbt(NbtCompound nbt) {
        PlayerStats stats = new PlayerStats();
        String uuidStr = nbt.getString("playerId").orElse(UUID.randomUUID().toString());
        stats.playerId = UUID.fromString(uuidStr);
        stats.playerName = nbt.getString("playerName").orElse("Unknown");
        stats.creeperDeaths = nbt.getInt("creeperDeaths").orElse(0);
        stats.creepersKilled = nbt.getInt("creepersKilled").orElse(0);
        stats.explosionsSurvived = nbt.getInt("explosionsSurvived").orElse(0);
        stats.nearMisses = nbt.getInt("nearMisses").orElse(0);
        stats.closestNearMiss = nbt.getFloat("closestNearMiss").orElse(Float.MAX_VALUE);
        stats.miniCreepersKilled = nbt.getInt("miniCreepersKilled").orElse(0);
        stats.giantCreepersKilled = nbt.getInt("giantCreepersKilled").orElse(0);
        stats.spiderCreepersKilled = nbt.getInt("spiderCreepersKilled").orElse(0);
        stats.chargedCreepersKilled = nbt.getInt("chargedCreepersKilled").orElse(0);
        stats.longestSurvivalStreak = nbt.getInt("longestSurvivalStreak").orElse(0);
        stats.currentSurvivalStreak = nbt.getInt("currentSurvivalStreak").orElse(0);
        stats.playTimeTicks = nbt.getLong("playTimeTicks").orElse(0L);
        return stats;
    }
    
    // ==================== STATIC METHODS ====================
    
    /**
     * Gets or creates stats for a player
     */
    public static PlayerStats getOrCreate(PlayerEntity player) {
        return PLAYER_STATS.computeIfAbsent(player.getUuid(), uuid -> new PlayerStats(player));
    }
    
    /**
     * Gets stats for a player by UUID
     */
    public static PlayerStats get(UUID playerId) {
        return PLAYER_STATS.get(playerId);
    }
    
    /**
     * Saves stats for a player
     */
    public static void save(UUID playerId, PlayerStats stats) {
        PLAYER_STATS.put(playerId, stats);
    }
    
    /**
     * Clears all player stats (for reset)
     */
    public static void clearAll() {
        PLAYER_STATS.clear();
    }
    
    /**
     * Gets all tracked player stats
     */
    public static Map<UUID, PlayerStats> getAll() {
        return PLAYER_STATS;
    }
}
