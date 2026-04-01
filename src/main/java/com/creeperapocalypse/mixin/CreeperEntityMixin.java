package com.creeperapocalypse.mixin;

import com.creeperapocalypse.CreeperApocalypse;
import com.creeperapocalypse.entity.GiantCreeperEntity;
import com.creeperapocalypse.entity.HappyCreeperEntity;
import com.creeperapocalypse.entity.JockeyCreeperEntity;
import com.creeperapocalypse.entity.LightningCreeperEntity;
import com.creeperapocalypse.entity.RainbowCreeperEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for creeper entities to track explosions and apply milestone effects
 */
@Mixin(CreeperEntity.class)
public abstract class CreeperEntityMixin {

    @Shadow
    private int explosionRadius;

    @Shadow
    private int currentFuseTime;

    @Shadow
    private int fuseTime;

    /**
     * Called when creeper explodes - track the explosion
     */
    @Inject(method = "explode", at = @At("HEAD"), cancellable = true)
    private void onExplode(CallbackInfo ci) {
        CreeperEntity creeper = (CreeperEntity) (Object) this;

        // Happy Creeper override
        if (creeper instanceof HappyCreeperEntity happy) {
            happy.doHappyExplosion();
            ci.cancel();
            return;
        }

        // Rainbow Creeper override
        if (creeper instanceof RainbowCreeperEntity rainbow) {
            rainbow.doRainbowExplosion();
            ci.cancel();
            return;
        }

        if (creeper instanceof JockeyCreeperEntity jockey) {
            jockey.protectRiderFromExplosion();
        }

        if (!CreeperApocalypse.CONFIG.isEnabled()) {
            return;
        }

        // Track explosion
        CreeperApocalypse.CHALLENGE_DATA.incrementExplosions();

        CreeperApocalypse.LOGGER.debug("Creeper explosion tracked at " + creeper.getBlockPos());
    }

    /**
     * Modifies explosion radius based on day/milestones
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (!CreeperApocalypse.CONFIG.isEnabled()) {
            return;
        }

        CreeperEntity creeper = (CreeperEntity) (Object) this;

        if (creeper instanceof GiantCreeperEntity) {
            explosionRadius = 12;
        }

        // Super-charged Lightning Creeper: faster fuse + bigger explosion
        if (creeper instanceof LightningCreeperEntity lightningCreeper) {
            if (lightningCreeper.isSuperCharged()) {
                // +2 explosion radius (3 -> 5)
                explosionRadius = 5;

                // 20% faster fuse time
                if (currentFuseTime > 0) {
                    int maxFuse = 24; // 20% faster than default 30
                    if (fuseTime != maxFuse) {
                        fuseTime = maxFuse;
                    }
                }
            }
        }

        // Slightly increase explosion radius on later days
        int currentDay = CreeperApocalypse.CHALLENGE_DATA.getCurrentDay();

        // After day 10, creepers get stronger
        if (currentDay >= 10) {
            int bonusRadius = (currentDay - 10) / 5; // +1 radius every 5 days after day 10
            // Don't go crazy - cap at +2
            bonusRadius = Math.min(bonusRadius, 2);

            // Only modify if not already modified
            if (explosionRadius <= 3 + bonusRadius) {
                // explosionRadius = 3 + bonusRadius; // Default is 3
            }
        }
    }
}

