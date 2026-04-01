package com.creeperapocalypse.init;

import com.creeperapocalypse.CreeperApocalypse;
import com.creeperapocalypse.mixin.SpawnRestrictionAccessor;
import com.creeperapocalypse.util.MobReplacementHelper;
import com.creeperapocalypse.entity.ModEntities;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.SpawnLocationTypes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.SpawnSettings;

public class SpawnInit {

    public static void init() {
        CreeperApocalypse.LOGGER.info("Initializing Spawn Table Overrides - APOCALYPSE MODE");

        // 1. CLEAR almost everything from spawning in biomes
        BiomeModifications.create(Identifier.of("creeper-apocalypse", "wipe_spawns"))
                .add(ModificationPhase.REPLACEMENTS, BiomeSelectors.all(), context -> {
                    // Iterate over all registered entity types to find ones to remove
                    for (EntityType<?> type : Registries.ENTITY_TYPE) {
                        // If it's NOT a living entity type or it's excluded, skip removal
                        // We check "isExcluded" which returns true for Blaze, Villager, etc.
                        if (MobReplacementHelper.isExcluded(type)) {
                            continue;
                        }

                        // Keep creepers and modded variants in the spawn list
                        if (type == EntityType.CREEPER || ModEntities.isVariant(type)) {
                            continue;
                        }

                        // If it's a type we want removed (Cow, Zombie, Fish, etc.), remove it from ALL groups
                        // This prevents natural spawning
                        context.getSpawnSettings().removeSpawnsOfEntityType(type);
                    }
                });

        // 2. Add CREEPERS everywhere
        // High weight to ensure they dominate any remaining spawns
        BiomeModifications.addSpawn(BiomeSelectors.all(), SpawnGroup.MONSTER, EntityType.CREEPER, 800, 4, 4);
        BiomeModifications.addSpawn(BiomeSelectors.all(), SpawnGroup.CREATURE, EntityType.CREEPER, 800, 4, 4);

        // 3. FORCE CREEPERS TO SPAWN IN DAYLIGHT
        // By default, Creepers only spawn in dark. We remove this restriction.
        overrideCreeperSpawnRestriction();
    }

    private static void overrideCreeperSpawnRestriction() {
        try {
            // Remove the default restriction (which requires darkness)
            SpawnRestrictionAccessor.getRestrictions().remove(EntityType.CREEPER);

            // Register new restriction:
            // - Location: ON_GROUND (standard)
            // - Heightmap: MOTION_BLOCKING_NO_LEAVES (surface)
            // - Predicate: MobEntity::canMobSpawn (Check collision/liquids, BUT IGNORE LIGHT)

            // NOTE: We used to use MobEntity::canMobSpawn, but on some blocks (like End Stone or Nether Bricks)
            // outside of their native dimensions/biomes, the checks can be weird.
            // Also, we want them to spawn EVERYWHERE.
            // We'll use a custom predicate that is extremely permissive.

            SpawnRestriction.register(
                EntityType.CREEPER,
                SpawnLocationTypes.ON_GROUND,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                (type, world, spawnReason, pos, random) -> {
                     // 1. Must be solid block below
                     if (!world.getBlockState(pos.down()).isSolidBlock(world, pos.down())) return false;
                     // 2. Must not collide with blocks
                     if (!world.getBlockState(pos).isAir() && world.getBlockState(pos).isSolidBlock(world, pos)) return false;
                     // 3. That's it. Ignore light. Ignore biome specific block tags.
                     return true;
                }
            );

            CreeperApocalypse.LOGGER.info("Creeper spawn restrictions relaxed (Daylight Spawning ENABLED)");
        } catch (Exception e) {
            CreeperApocalypse.LOGGER.error("Failed to override Creeper spawn restrictions", e);
        }
    }
}

