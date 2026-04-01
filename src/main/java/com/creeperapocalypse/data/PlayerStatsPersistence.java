package com.creeperapocalypse.data;

import com.creeperapocalypse.CreeperApocalypse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

final class PlayerStatsPersistence {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String DATA_FILE = "creeper_apocalypse_player_stats.json";

    private PlayerStatsPersistence() {
    }

    static void loadAll(MinecraftServer server, Map<UUID, PlayerStats> statsByPlayer) {
        Path dataPath = getDataPath(server);
        statsByPlayer.clear();

        if (!Files.exists(dataPath)) {
            CreeperApocalypse.LOGGER.info("No player stats file found; starting fresh");
            return;
        }

        try {
            String json = Files.readString(dataPath);
            PlayerStats[] loaded = GSON.fromJson(json, PlayerStats[].class);

            if (loaded != null) {
                for (PlayerStats stats : loaded) {
                    if (stats != null && stats.playerId != null) {
                        statsByPlayer.put(stats.playerId, stats);
                    }
                }
            }

            CreeperApocalypse.LOGGER.info("Loaded " + statsByPlayer.size() + " player stats entries");
        } catch (IOException | RuntimeException e) {
            CreeperApocalypse.LOGGER.error("Failed to load player stats: " + e.getMessage());
        }
    }

    static void saveAll(MinecraftServer server, Map<UUID, PlayerStats> statsByPlayer) {
        Path dataPath = getDataPath(server);

        try {
            Files.writeString(dataPath, GSON.toJson(new ArrayList<>(statsByPlayer.values())));
            CreeperApocalypse.LOGGER.info("Saved " + statsByPlayer.size() + " player stats entries");
        } catch (IOException e) {
            CreeperApocalypse.LOGGER.error("Failed to save player stats: " + e.getMessage());
        }
    }

    private static Path getDataPath(MinecraftServer server) {
        return server.getSavePath(WorldSavePath.ROOT).resolve(DATA_FILE);
    }
}
