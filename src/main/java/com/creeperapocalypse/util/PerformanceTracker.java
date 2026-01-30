package com.creeperapocalypse.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

/**
 * Tracks average MSPT (milliseconds per tick) to detect lag.
 */
public final class PerformanceTracker {

    private static long tickStartNs = 0L;
    private static double averageMspt = 0.0;
    private static final double ALPHA = 0.05; // smoothing factor

    private PerformanceTracker() {
    }

    public static void register() {
        ServerTickEvents.START_SERVER_TICK.register(PerformanceTracker::onTickStart);
        ServerTickEvents.END_SERVER_TICK.register(PerformanceTracker::onTickEnd);
    }

    private static void onTickStart(MinecraftServer server) {
        tickStartNs = System.nanoTime();
    }

    private static void onTickEnd(MinecraftServer server) {
        if (tickStartNs == 0L) {
            return;
        }
        long durationNs = System.nanoTime() - tickStartNs;
        double mspt = durationNs / 1_000_000.0;

        if (averageMspt <= 0.0) {
            averageMspt = mspt;
        } else {
            averageMspt = averageMspt + (mspt - averageMspt) * ALPHA;
        }
    }

    public static double getAverageMspt() {
        return averageMspt <= 0.0 ? 50.0 : averageMspt;
    }
}
