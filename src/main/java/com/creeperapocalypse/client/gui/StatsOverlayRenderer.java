package com.creeperapocalypse.client.gui;

import com.creeperapocalypse.CreeperApocalypse;
import com.creeperapocalypse.data.PlayerStats;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/**
 * Renders the statistics overlay on the HUD
 * Shows detailed player stats when toggled
 */
public class StatsOverlayRenderer {

    private final MinecraftClient client;

    public StatsOverlayRenderer() {
        this.client = MinecraftClient.getInstance();
    }

    /**
     * Renders the full stats overlay
     */
    public void render(DrawContext context, float tickDelta) {
        if (client.player == null) return;

        PlayerStats stats = PlayerStats.getOrCreate(client.player);

        int x = 10;
        int y = 40; // Below the mini overlay
        int lineHeight = 12;
        int bgWidth = 180;
        int bgHeight = 130;

        // Semi-transparent background
        context.fill(x - 5, y - 5, x + bgWidth, y + bgHeight, 0xB0000000);

        // Header
        context.drawText(client.textRenderer, "§c§l[CHALLENGE STATS]", x, y, 0xFFFFFF, true);
        y += lineHeight + 5;

        // Current challenge info
        int day = CreeperApocalypse.CHALLENGE_DATA.getCurrentDay();
        float multiplier = CreeperApocalypse.CHALLENGE_DATA.getSpawnMultiplier();
        context.drawText(client.textRenderer, "§eDay: §f" + day + " §7| §6Multiplier: §f" +
            String.format("%.1fx", multiplier), x, y, 0xFFFFFF, true);
        y += lineHeight;

        // Separator
        context.drawText(client.textRenderer, "§8-----------------", x, y, 0xFFFFFF, false);
        y += lineHeight;

        // Personal stats header
        context.drawText(client.textRenderer, "§b§lYour Stats:", x, y, 0xFFFFFF, true);
        y += lineHeight;

        // Deaths
        context.drawText(client.textRenderer, "§7Creeper Deaths: §c" + stats.getCreeperDeaths(), x, y, 0xFFFFFF, true);
        y += lineHeight;

        // Kills
        context.drawText(client.textRenderer, "§7Creepers Killed: §a" + stats.getCreepersKilled(), x, y, 0xFFFFFF, true);
        y += lineHeight;

        // Near Misses
        String closestMiss = stats.getClosestNearMiss() == Float.MAX_VALUE ? "N/A" :
            String.format("%.1f", stats.getClosestNearMiss());
        context.drawText(client.textRenderer, "§7Near Misses: §6" + stats.getNearMisses() +
            " §7(Closest: " + closestMiss + ")", x, y, 0xFFFFFF, true);
        y += lineHeight;

        // Explosions Survived
        context.drawText(client.textRenderer, "§7Explosions Survived: §b" + stats.getExplosionsSurvived(), x, y, 0xFFFFFF, true);
        y += lineHeight;

        // Survival Streak
        context.drawText(client.textRenderer, "§7Survival Streak: §a" + stats.getCurrentSurvivalStreak() +
            " §7days (Best: §a" + stats.getLongestSurvivalStreak() + "§7)", x, y, 0xFFFFFF, true);
        y += lineHeight;

        // Separator
        context.drawText(client.textRenderer, "§8-----------------", x, y, 0xFFFFFF, false);
        y += lineHeight;

        // Global stats header
        context.drawText(client.textRenderer, "§d§lGlobal Stats:", x, y, 0xFFFFFF, true);
        y += lineHeight;

        // Total spawned
        long totalSpawned = CreeperApocalypse.CHALLENGE_DATA.getTotalCreepersSpawned();
        context.drawText(client.textRenderer, "§7Total Spawned: §f" + formatNumber(totalSpawned), x, y, 0xFFFFFF, true);
        y += lineHeight;

        // Total killed
        long totalKilled = CreeperApocalypse.CHALLENGE_DATA.getTotalCreepersKilled();
        context.drawText(client.textRenderer, "§7Total Killed: §f" + formatNumber(totalKilled), x, y, 0xFFFFFF, true);
        y += lineHeight;

        // Total explosions
        long totalExplosions = CreeperApocalypse.CHALLENGE_DATA.getTotalExplosions();
        context.drawText(client.textRenderer, "§7Total Explosions: §f" + formatNumber(totalExplosions), x, y, 0xFFFFFF, true);
        y += lineHeight;

        // Highest day
        int highestDay = CreeperApocalypse.CHALLENGE_DATA.getHighestDayReached();
        context.drawText(client.textRenderer, "§7Highest Day: §e" + highestDay, x, y, 0xFFFFFF, true);

        // Hotkey reminder at bottom
        context.drawText(client.textRenderer, "§8Press J to toggle", x, y + lineHeight + 5, 0x888888, false);
    }

    /**
     * Formats a number with K/M suffixes for readability
     */
    private String formatNumber(long number) {
        if (number >= 1_000_000) {
            return String.format("%.1fM", number / 1_000_000.0);
        } else if (number >= 1_000) {
            return String.format("%.1fK", number / 1_000.0);
        }
        return String.valueOf(number);
    }
}

