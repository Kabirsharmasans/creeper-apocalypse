package com.creeperapocalypse.event;

import com.creeperapocalypse.CreeperApocalypse;
import com.creeperapocalypse.util.MobReplacementHelper;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

public class SurfaceSpawnHandler {

    private int tickCounter = 0;
    private boolean registered;

    public void register() {
        if (registered) {
            CreeperApocalypse.LOGGER.debug("Surface spawn handler already registered; skipping duplicate registration");
            return;
        }

        ServerTickEvents.END_WORLD_TICK.register(this::onWorldTick);
        registered = true;
        CreeperApocalypse.LOGGER.info("Surface spawn handler registered");
    }

    private void onWorldTick(ServerWorld world) {
        if (!CreeperApocalypse.CONFIG.isEnabled()) {
            return;
        }

        if (!world.getRegistryKey().equals(World.OVERWORLD)) {
            return;
        }

        tickCounter++;
        if (tickCounter % 80 != 0) { // once per 4 seconds
            return;
        }

        if (!MobReplacementHelper.canSpawnMoreCreepers(world)) {
            return;
        }

        for (ServerPlayerEntity player : world.getPlayers()) {
            if (!MobReplacementHelper.canSpawnMoreCreepers(world)) {
                break;
            }

            BlockPos basePos = player.getBlockPos();

            // Spawn away from the player to avoid instant deaths
            int minRadius = 24;
            int maxRadius = 48;

            for (int attempt = 0; attempt < 6; attempt++) {
                if (!MobReplacementHelper.canSpawnMoreCreepers(world)) {
                    break;
                }

                int dx = world.getRandom().nextBetween(minRadius, maxRadius) * (world.getRandom().nextBoolean() ? 1 : -1);
                int dz = world.getRandom().nextBetween(minRadius, maxRadius) * (world.getRandom().nextBoolean() ? 1 : -1);
                BlockPos sample = basePos.add(dx, 0, dz);

                if (!world.isChunkLoaded(sample)) {
                    continue;
                }

                int topY = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, sample.getX(), sample.getZ());
                BlockPos spawnPos = new BlockPos(sample.getX(), topY, sample.getZ());

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

