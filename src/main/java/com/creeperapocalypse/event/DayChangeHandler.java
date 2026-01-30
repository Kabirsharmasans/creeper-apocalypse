package com.creeperapocalypse.event;

import com.creeperapocalypse.CreeperApocalypse;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Handles day change detection and escalation announcements
 */
public class DayChangeHandler {
    
    private long lastDayTime = -1;
    private int lastDay = 0;
    
    public void register() {
        ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);
        CreeperApocalypse.LOGGER.info("Day change handler registered");
    }
    
    private void onServerTick(MinecraftServer server) {
        if (!CreeperApocalypse.CONFIG.isEnabled()) {
            return;
        }
        
        ServerWorld overworld = server.getOverworld();
        if (overworld == null) return;
        
        // Track strictly by Minecraft Day count (getTimeOfDay / 24000), not daily cycle/ticks
        // Minecraft Day 0 = Challenge Day 1
        int worldDay = (int) (overworld.getTimeOfDay() / 24000L);
        int expectedChallengeDay = worldDay + 1;
        int currentChallengeDay = CreeperApocalypse.CHALLENGE_DATA.getCurrentDay();
        
        // If the world day has advanced beyond the current challenge day
        if (expectedChallengeDay > currentChallengeDay) {
            CreeperApocalypse.CHALLENGE_DATA.setCurrentDay(expectedChallengeDay);
            onDayChange(server, expectedChallengeDay);
        }
        // Optional: If time is set backwards (e.g. /time set 0), sync back silently?
        // For now, let's only escalate forward to prevent accidental setbacks, unless reset is requested.
        // User said "track minecraft DAY", implying sync. If they reset world, they might expect Day 1.
        else if (expectedChallengeDay < currentChallengeDay && expectedChallengeDay >= 1) {
             // If severe desync (e.g. world reset), sync back
             CreeperApocalypse.CHALLENGE_DATA.setCurrentDay(expectedChallengeDay);
             // No announcement for going backwards to avoid spam if time fluctuates
        }
    }
    
    private void onDayChange(MinecraftServer server, int challengeDay) {
        float multiplier = CreeperApocalypse.CHALLENGE_DATA.getSpawnMultiplier();
        
        CreeperApocalypse.LOGGER.info("Day " + challengeDay + " has begun!");
        CreeperApocalypse.LOGGER.info("Spawn multiplier: " + multiplier + "x");
        
        Text announcement = Text.literal("")
            .append(Text.literal("[CREEPER APOCALYPSE] ").formatted(Formatting.RED, Formatting.BOLD))
            .append(Text.literal("Day " + challengeDay + " has begun! ").formatted(Formatting.YELLOW))
            .append(Text.literal("Spawn multiplier: " + String.format("%.1fx", multiplier)).formatted(Formatting.GOLD));
        
        server.getPlayerManager().broadcast(announcement, false);
        
        checkMilestones(server, challengeDay);
        
        for (var player : server.getPlayerManager().getPlayerList()) {
            player.playSound(net.minecraft.sound.SoundEvents.ENTITY_WITHER_SPAWN, 0.5f, 1.5f);
        }
    }
    
    private void checkMilestones(MinecraftServer server, int day) {
        if (!CreeperApocalypse.CONFIG.milestonesEnabled()) {
            return;
        }
        
        if (day == 5 && CreeperApocalypse.CONFIG.bloodMoonEnabled()) {
            announceBloodMoon(server);
        }
        
        if (day == 7 && CreeperApocalypse.CONFIG.chargedDayEnabled()) {
            announceChargedDay(server);
        }
        
        if (day == 10 && CreeperApocalypse.CONFIG.swarmDayEnabled()) {
            announceSwarm(server);
        }
        
        if (day > 10 && day % 5 == 0) {
            announceSuperMilestone(server, day);
        }
    }
    
    private void announceBloodMoon(MinecraftServer server) {
        Text announcement = Text.literal("")
            .append(Text.literal("☠ ").formatted(Formatting.DARK_RED))
            .append(Text.literal("BLOOD MOON").formatted(Formatting.RED, Formatting.BOLD))
            .append(Text.literal(" ☠").formatted(Formatting.DARK_RED))
            .append(Text.literal("\nAll creepers glow with crimson fury!").formatted(Formatting.RED));
        
        server.getPlayerManager().broadcast(announcement, false);
        
        for (var player : server.getPlayerManager().getPlayerList()) {
            player.playSound(net.minecraft.sound.SoundEvents.AMBIENT_CAVE.value(), 1.0f, 0.5f);
        }
        
        CreeperApocalypse.CHALLENGE_DATA.setBloodMoonTriggered(true);
    }
    
    private void announceChargedDay(MinecraftServer server) {
        Text announcement = Text.literal("")
            .append(Text.literal("⚡ ").formatted(Formatting.AQUA))
            .append(Text.literal("CHARGED DAY").formatted(Formatting.BLUE, Formatting.BOLD))
            .append(Text.literal(" ⚡").formatted(Formatting.AQUA))
            .append(Text.literal("\n25% of creepers spawn CHARGED!").formatted(Formatting.AQUA));
        
        server.getPlayerManager().broadcast(announcement, false);
        
        for (var player : server.getPlayerManager().getPlayerList()) {
            player.playSound(net.minecraft.sound.SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
        }
        
        CreeperApocalypse.CHALLENGE_DATA.setChargedDayTriggered(true);
    }
    
    private void announceSwarm(MinecraftServer server) {
        Text announcement = Text.literal("")
            .append(Text.literal("💀 ").formatted(Formatting.GREEN))
            .append(Text.literal("THE SWARM").formatted(Formatting.DARK_GREEN, Formatting.BOLD))
            .append(Text.literal(" 💀").formatted(Formatting.GREEN))
            .append(Text.literal("\nDay 10 - They come in WAVES!").formatted(Formatting.GREEN));
        
        server.getPlayerManager().broadcast(announcement, false);
        
        for (var player : server.getPlayerManager().getPlayerList()) {
            player.playSound(net.minecraft.sound.SoundEvents.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.8f);
        }
        
        CreeperApocalypse.CHALLENGE_DATA.setSwarmDayTriggered(true);
    }
    
    private void announceSuperMilestone(MinecraftServer server, int day) {
        Text announcement = Text.literal("")
            .append(Text.literal("🏆 ").formatted(Formatting.GOLD))
            .append(Text.literal("MILESTONE: DAY " + day).formatted(Formatting.GOLD, Formatting.BOLD))
            .append(Text.literal(" 🏆").formatted(Formatting.GOLD))
            .append(Text.literal("\nYou've survived " + day + " days!").formatted(Formatting.YELLOW));
        
        server.getPlayerManager().broadcast(announcement, false);
    }
    
    public void reset() {
        lastDayTime = -1;
        lastDay = 0;
    }
}
