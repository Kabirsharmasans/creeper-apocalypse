package com.creeperapocalypse;

import com.creeperapocalypse.config.CreeperConfig;
import com.creeperapocalypse.init.SpawnInit;
import com.creeperapocalypse.init.ModItems;
import com.creeperapocalypse.data.ChallengeData;
import com.creeperapocalypse.data.PlayerStats;
import com.creeperapocalypse.entity.ModEntities;
import com.creeperapocalypse.event.DayChangeHandler;
import com.creeperapocalypse.event.EndSurfaceSpawnHandler;
import com.creeperapocalypse.event.SurfaceSpawnHandler;
import com.creeperapocalypse.event.MilestoneEventHandler;
import com.creeperapocalypse.network.ModNetworking;
import com.creeperapocalypse.util.PerformanceTracker;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Creeper Apocalypse Escalation - Main Mod Entry Point
 *
 * This mod replaces all mob spawns (except Blazes, Villagers, and Ender Dragon)
 * with Creepers. Features an auto-escalating spawn rate system that increases
 * each day, milestone events, special creeper variants, and streaming features.
 */
public class CreeperApocalypse implements ModInitializer {

    public static final String MOD_ID = "creeper-apocalypse";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static CreeperConfig CONFIG;
    public static ChallengeData CHALLENGE_DATA;

    // Flag for death screenshot - set by PlayerEntityMixin, read by MinecraftClientMixin
    public static volatile boolean pendingDeathScreenshot = false;

    private static DayChangeHandler dayChangeHandler;
    private static MilestoneEventHandler milestoneHandler;
    private static EndSurfaceSpawnHandler endSurfaceSpawnHandler;
    private static SurfaceSpawnHandler surfaceSpawnHandler;
    private static MinecraftServer serverInstance;

    @Override
    public void onInitialize() {
        LOGGER.info("==============================================");
        LOGGER.info("  CREEPER APOCALYPSE ESCALATION");
        LOGGER.info("  The creepers are coming... every single day.");
        LOGGER.info("==============================================");

        CONFIG = CreeperConfig.load();
        LOGGER.info("Configuration loaded");

        CHALLENGE_DATA = new ChallengeData();

        ModEntities.register();
        LOGGER.info("Custom entities registered");

        ModItems.register();

        SpawnInit.init(); // Initialize Spawn Table Overrides
        LOGGER.info("Spawn tables intercepted");
        PerformanceTracker.register();

        ModNetworking.registerRuntimeControlPackets();
        LOGGER.info("Network channels registered");

        dayChangeHandler = new DayChangeHandler();
        milestoneHandler = new MilestoneEventHandler();
        endSurfaceSpawnHandler = new EndSurfaceSpawnHandler();
        surfaceSpawnHandler = new SurfaceSpawnHandler();

        registerServerEvents();

        endSurfaceSpawnHandler.register();
        surfaceSpawnHandler.register();

        LOGGER.info("Creeper Apocalypse Escalation initialized!");
        LOGGER.info("Challenge is " + (CONFIG.isEnabled() ? "ENABLED" : "DISABLED"));
    }

    private void registerServerEvents() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            LOGGER.info("Server starting - loading challenge data...");
            serverInstance = server;
            CONFIG = CreeperConfig.load(server);
            CHALLENGE_DATA = ChallengeData.load(server);
            CHALLENGE_DATA.refreshSpawnMultiplier();
            PlayerStats.loadAll(server);
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if (CONFIG.isEnabled()) {
                LOGGER.info("Challenge active! Current day: " + CHALLENGE_DATA.getCurrentDay());
                LOGGER.info("Current spawn multiplier: " + CHALLENGE_DATA.getSpawnMultiplier() + "x");
            }
            dayChangeHandler.register();
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            LOGGER.info("Server stopping - saving challenge data...");
            CHALLENGE_DATA.save(server);
            PlayerStats.saveAll(server);
            CONFIG.save(server);
            serverInstance = null;
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            var player = handler.getPlayer();
            PlayerStats.getOrCreate(player);
            ModNetworking.syncChallengeData(player, CHALLENGE_DATA);

            if (CONFIG.isEnabled() && CONFIG.showWelcomeMessage()) {
                player.sendMessage(
                    net.minecraft.text.Text.literal("§c§l[CREEPER APOCALYPSE]§r §eDay " +
                        CHALLENGE_DATA.getCurrentDay() + " - Spawn multiplier: " +
                        CHALLENGE_DATA.getSpawnMultiplier() + "x"),
                    false
                );
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            var player = handler.getPlayer();
            var stats = PlayerStats.get(player.getUuid());
            if (stats != null) {
                PlayerStats.save(player.getUuid(), stats);
            }
            PlayerStats.saveAll(server);
        });
    }

    public static float getCurrentSpawnMultiplier() {
        if (!CONFIG.isEnabled()) {
            return 1.0f;
        }
        return CHALLENGE_DATA.getSpawnMultiplier();
    }

    public static boolean isMilestoneActive(MilestoneEventHandler.MilestoneType milestone) {
        return milestoneHandler != null && milestoneHandler.isActive(milestone);
    }

    public static MilestoneEventHandler getMilestoneHandler() {
        return milestoneHandler;
    }

    public static DayChangeHandler getDayChangeHandler() {
        return dayChangeHandler;
    }

    public static MinecraftServer getServer() {
        return serverInstance;
    }

    public static boolean applyConfigForCurrentWorld() {
        return applyConfigForCurrentWorld(null);
    }

    public static boolean applyConfigForCurrentWorld(Consumer<CreeperConfig> configUpdater) {
        MinecraftServer server = serverInstance;
        if (server == null || CHALLENGE_DATA == null || CONFIG == null) {
            return false;
        }

        server.execute(() -> {
            if (configUpdater != null) {
                configUpdater.accept(CONFIG);
            }
            CHALLENGE_DATA.refreshSpawnMultiplier();
            CONFIG.save(server);
            ModNetworking.syncToAllPlayers(server, CHALLENGE_DATA);
        });
        return true;
    }

    public static void resetChallenge() {
        CHALLENGE_DATA = new ChallengeData();
        CHALLENGE_DATA.refreshSpawnMultiplier();
        if (dayChangeHandler != null) {
            dayChangeHandler.reset();
        }
        if (milestoneHandler != null) {
            milestoneHandler.reset();
        }
        PlayerStats.clearAll();
        LOGGER.info("Challenge has been reset!");
    }
}

