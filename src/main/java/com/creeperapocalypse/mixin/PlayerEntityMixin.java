package com.creeperapocalypse.mixin;

import com.creeperapocalypse.CreeperApocalypse;
import com.creeperapocalypse.data.PlayerStats;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin for player entity to track deaths and damage from creepers
 */
@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    
    /**
     * Called when player dies - check if it was from a creeper
     */
    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onPlayerDeath(DamageSource damageSource, CallbackInfo ci) {
        if (!CreeperApocalypse.CONFIG.isEnabled()) {
            return;
        }
        
        PlayerEntity player = (PlayerEntity) (Object) this;
        
        // Check if death was caused by a creeper or explosion
        if (isCreeperDeath(damageSource)) {
            PlayerStats stats = PlayerStats.getOrCreate(player);
            stats.recordCreeperDeath();
            
            CreeperApocalypse.LOGGER.info(player.getName().getString() + " was killed by a creeper!");
            
            // Trigger death screenshot on client side
            CreeperApocalypse.pendingDeathScreenshot = true;
            
            // Send death message with stats if on server
            if (player instanceof ServerPlayerEntity serverPlayer) {
                int deaths = stats.getCreeperDeaths();
                int day = CreeperApocalypse.CHALLENGE_DATA.getCurrentDay();
                
                serverPlayer.sendMessage(
                    net.minecraft.text.Text.literal("§c☠ Creeper Death #" + deaths + " on Day " + day + " §c☠"),
                    false
                );
            }
        }
    }
    
    /**
     * Called when player takes damage - check for near misses
     */
    @Inject(method = "damage", at = @At("RETURN"))
    private void onPlayerDamage(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!CreeperApocalypse.CONFIG.isEnabled()) {
            return;
        }
        
        if (!CreeperApocalypse.CONFIG.nearMissCounterEnabled()) {
            return;
        }
        
        PlayerEntity player = (PlayerEntity) (Object) this;
        
        // Check if damage was from explosion and player survived
        if (source.isOf(net.minecraft.entity.damage.DamageTypes.EXPLOSION) || 
            source.isOf(net.minecraft.entity.damage.DamageTypes.PLAYER_EXPLOSION)) {
            
            if (player.getHealth() > 0) {
                PlayerStats stats = PlayerStats.getOrCreate(player);
                stats.recordExplosionSurvived();
                
                // Check for near miss (configurable distance)
                if (source.getSource() instanceof CreeperEntity creeper) {
                    double distance = player.distanceTo(creeper);
                    float nearMissDistance = CreeperApocalypse.CONFIG.getNearMissDistance();
                    
                    if (distance <= nearMissDistance) {
                        stats.recordNearMiss((float) distance);
                        
                        // Notify player of near miss
                        if (player instanceof ServerPlayerEntity serverPlayer) {
                            serverPlayer.sendMessage(
                                net.minecraft.text.Text.literal("§6⚠ NEAR MISS! §7(" + 
                                    String.format("%.1f", distance) + " blocks)"),
                                true
                            );
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Checks if the damage source was from a creeper
     */
    private boolean isCreeperDeath(DamageSource source) {
        // Check if attacker is a creeper
        if (source.getAttacker() instanceof CreeperEntity) {
            return true;
        }
        
        // Check if source entity is a creeper
        if (source.getSource() instanceof CreeperEntity) {
            return true;
        }
        
        // Check for explosion damage types
        if (source.isOf(net.minecraft.entity.damage.DamageTypes.EXPLOSION) ||
            source.isOf(net.minecraft.entity.damage.DamageTypes.PLAYER_EXPLOSION)) {
            // Could be creeper explosion
            return true;
        }
        
        return false;
    }
}
