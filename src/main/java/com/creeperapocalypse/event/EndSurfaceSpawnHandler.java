package com.creeperapocalypse.event;

import com.creeperapocalypse.CreeperApocalypse;
import com.creeperapocalypse.util.MobReplacementHelper;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

public class EndSurfaceSpawnHandler {

    private int tickCounter = 0;

    public void register() {
        ServerTickEvents.END_WORLD_TICK.register(this::onWorldTick);
        CreeperApocalypse.LOGGER.info("End surface spawn handler registered");
    }

    private void onWorldTick(ServerWorld world) {
        if (!CreeperApocalypse.CONFIG.isEnabled()) {
            return;
        }

        if (!world.getRegistryKey().equals(World.END)) {
            return;
        }

        tickCounter++;
        if (tickCounter % 60 != 0) { // once per 3 seconds
            return;
        }

        if (!MobReplacementHelper.canSpawnMoreCreepers(world)) {
            return;
        }

        if (!MobReplacementHelper.canSpawnMoreCreepersInEnd(world)) {
            return;
        }

        for (ServerPlayerEntity player : world.getPlayers()) {
            if (!MobReplacementHelper.canSpawnMoreCreepers(world)) {
                break;
            }

            if (!MobReplacementHelper.canSpawnMoreCreepersInEnd(world)) {
                break;
            }

            BlockPos basePos = player.getBlockPos();

            // Force spawns FAR from the player to avoid instant deaths
            // Keep radius relatively tight so we usually stay on the main island.
            int minRadius = 30;
            int maxRadius = 55;

            // Try multiple positions around the player each second
            for (int attempt = 0; attempt < 6; attempt++) {
                if (!MobReplacementHelper.canSpawnMoreCreepers(world)) {
                    break;
                }

                if (!MobReplacementHelper.canSpawnMoreCreepersInEnd(world)) {
                    break;
                }

                int dx = world.getRandom().nextBetween(minRadius, maxRadius) * (world.getRandom().nextBoolean() ? 1 : -1);
                int dz = world.getRandom().nextBetween(minRadius, maxRadius) * (world.getRandom().nextBoolean() ? 1 : -1);
                BlockPos sample = basePos.add(dx, 0, dz);

                if (!world.isChunkLoaded(sample)) {
                    continue;
                }

                // Robust surface finder: scan DOWN from above the player to locate the first solid block.
                // This avoids heightmap weirdness and prevents underground-only spawns.
                int startY = basePos.getY() + 96;
                int worldTop = world.getTopY(Heightmap.Type.WORLD_SURFACE, sample.getX(), sample.getZ()) - 1;
                startY = Math.min(startY, worldTop);

                BlockPos spawnPos = null;
                for (int y = startY; y > world.getBottomY(); y--) {
                    BlockPos check = new BlockPos(sample.getX(), y, sample.getZ());
                    if (!world.getBlockState(check).isSolidBlock(world, check)) {
                        continue;
                    }

                    BlockPos above = check.up();
                    if (!world.getBlockState(above).getCollisionShape(world, above).isEmpty()) {
                        continue;
                    }

                    spawnPos = above;
                    break;
                }

                if (spawnPos == null) {
                    continue;
                }

                // Only spawn if the block below is solid and the spawn space is clear
                if (!world.getBlockState(spawnPos.down()).isSolidBlock(world, spawnPos.down())) {
                    continue;
                }
                if (!world.getBlockState(spawnPos).getCollisionShape(world, spawnPos).isEmpty()) {
                    continue;
                }

                var creeper = MobReplacementHelper.createApocalypseCreeper(world, spawnPos);
                if (creeper != null) {
                    world.spawnEntity(creeper);
                    CreeperApocalypse.CHALLENGE_DATA.incrementCreepersSpawned();
                }
            }
        }
    }
}
