package com.creeperapocalypse.mixin;

import com.creeperapocalypse.CreeperApocalypse;
import com.creeperapocalypse.entity.ModEntities;
import com.creeperapocalypse.util.MobReplacementHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to intercept natural mob spawning and replace mobs with creepers
 * This is the core of the mob replacement system
 */
@Mixin(ServerWorld.class)
public class ServerWorldMixin {

    /**
     * Thread-local flag to prevent infinite recursion when creating replacement entities
     */
    @Unique
    private static final ThreadLocal<Boolean> isReplacingEntity = ThreadLocal.withInitial(() -> false);

    @Unique
    private int creeperSweepTicker = 0;

    /**
     * Intercepts entity spawning to potentially replace with creepers
     */
    @Inject(
        method = "spawnEntity",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onSpawnEntity(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        // Skip if challenge is disabled
        if (!CreeperApocalypse.CONFIG.isEnabled()) {
            return;
        }

        if (entity == null) {
            return;
        }

        // Prevent infinite recursion - if we're already replacing, don't replace again
        if (isReplacingEntity.get()) {
            return;
        }

        // Don't replace our already-spawned variants!
        if (ModEntities.isVariant(entity.getType())) {
            return;
        }

        // Check if this entity type should be replaced
        if (MobReplacementHelper.shouldReplace(entity)) {
            try {
                isReplacingEntity.set(true);
                ServerWorld world = (ServerWorld) (Object) this;

                // Try to create a creeper
                Entity replacement = MobReplacementHelper.createReplacementCreeper(world, entity);

                if (replacement != null) {
                    // Success! Spawn the creeper
                    world.spawnEntity(replacement);
                    CreeperApocalypse.CHALLENGE_DATA.incrementCreepersSpawned();
                    CreeperApocalypse.LOGGER.debug("Replaced " + entity.getType().getTranslationKey() + " with creeper");
                }

                // ALWAYS CANCEL THE ORIGINAL SPAWN
                // 1. If replacement spawned, we don't want the original.
                // 2. If replacement was null (fish/limit reached), we DON'T want the original either.
                cir.setReturnValue(true);
                cir.cancel();

            } finally {
                isReplacingEntity.set(false);
            }
        }
    }

    /**
     * Periodic cleanup to replace existing non-creeper mobs (e.g., cats/iron golems)
     * that were already in the world before the replacement logic ran.
     */
    @Inject(method = "tick", at = @At("TAIL"))
    private void onWorldTick(CallbackInfo ci) {
        if (!CreeperApocalypse.CONFIG.isEnabled()) {
            return;
        }

        if (isReplacingEntity.get()) {
            return;
        }

        creeperSweepTicker++;
        if (creeperSweepTicker % 200 != 0) { // every 10 seconds
            return;
        }

        ServerWorld world = (ServerWorld) (Object) this;
        int replaced = 0;

        for (Entity entity : world.iterateEntities()) {
            if (replaced >= 20) {
                break;
            }

            if (entity == null) {
                continue;
            }

            if (entity instanceof CreeperEntity) {
                continue;
            }

            if (ModEntities.isVariant(entity.getType())) {
                continue;
            }

            if (!MobReplacementHelper.shouldReplace(entity)) {
                continue;
            }

            try {
                isReplacingEntity.set(true);
                Entity replacement = MobReplacementHelper.createReplacementCreeper(world, entity);

                if (replacement != null) {
                    world.spawnEntity(replacement);
                    CreeperApocalypse.CHALLENGE_DATA.incrementCreepersSpawned();
                }

                // Remove the original entity regardless (fish/over-cap cases return null)
                entity.discard();
                replaced++;
            } finally {
                isReplacingEntity.set(false);
            }
        }
    }
}

