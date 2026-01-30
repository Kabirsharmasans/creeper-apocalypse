package com.creeperapocalypse.mixin;

import com.creeperapocalypse.CreeperApocalypse;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Placeholder mixin - mob spawner functionality is handled by other mixins
 * This exists to satisfy the mixin config reference
 */
@Mixin(net.minecraft.block.entity.MobSpawnerBlockEntity.class)
public class MobSpawnerMixin {
    // The main mob replacement logic is in NaturalSpawnerMixin and MobSpawnMixin
}
