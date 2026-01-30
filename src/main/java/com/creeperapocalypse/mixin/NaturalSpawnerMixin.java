package com.creeperapocalypse.mixin;

import com.creeperapocalypse.CreeperApocalypse;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.world.SpawnHelper;

/**
 * Mixin for the natural mob spawner - DISABLED for now
 * The main mob replacement is handled by ServerWorldMixin
 */
@Mixin(SpawnHelper.class)
public class NaturalSpawnerMixin {
    // Disabled - ServerWorldMixin handles all replacements
}
