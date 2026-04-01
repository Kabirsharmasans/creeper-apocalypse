package com.creeperapocalypse.util;

import com.creeperapocalypse.CreeperApocalypse;
import com.creeperapocalypse.entity.LightningCreeperEntity;
import com.creeperapocalypse.entity.ModEntities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * CREEPER APOCALYPSE - Mob Replacement Helper
 *
 * SIMPLE RULES:
 * 1. ALL living mobs become CREEPERS
 * 2. EXCEPT: Blaze, Villager, Wandering Trader, Ender Dragon, Wither
 * 3. EXCEPT: All fish/aquatic (they cause infinite spawn lag)
 * 4. MAX 400 creepers to prevent lag
 *
 * MOBS THAT BECOME CREEPERS (from user's list):
 *
 * PASSIVE -> CREEPER:
 * Allay, Armadillo, Bat, Camel, Cat, Chicken, Cow, Donkey, Fox, Horse,
 * Mooshroom, Mule, Ocelot, Parrot, Pig, Rabbit, Sheep, Skeleton Horse,
 * Sniffer, Snow Golem, Strider
 *
 * NEUTRAL -> CREEPER:
 * Bee, Cave Spider, Enderman, Goat, Iron Golem, Llama, Panda, Piglin,
 * Polar Bear, Spider, Trader Llama, Wolf, Zombified Piglin
 *
 * HOSTILE -> CREEPER:
 * Bogged, Breeze, Creaking, Drowned, Elder Guardian, Endermite, Evoker,
 * Ghast, Guardian, Hoglin, Husk, Magma Cube, Phantom, Piglin Brute,
 * Pillager, Ravager, Shulker, Silverfish, Skeleton, Slime, Stray, Vex,
 * Vindicator, Warden, Witch, Wither Skeleton, Zoglin, Zombie, Zombie Horse,
 * Zombie Villager, Giant, Illusioner
 *
 * STAYS NORMAL (NEVER becomes creeper):
 * - Blaze (need for blaze rods)
 * - Villager (need for trading)
 * - Wandering Trader (need for trading)
 * - Ender Dragon (boss)
 * - Wither (boss)
 * - Fish: Cod, Salmon, Tropical Fish, Pufferfish (infinite spawn)
 * - Aquatic: Squid, Glow Squid, Dolphin, Axolotl, Tadpole, Frog, Turtle
 */
public class MobReplacementHelper {

    private static final Set<EntityType<?>> NEVER_REPLACE = new HashSet<>();
    private static final Set<EntityType<?>> FISH_TYPES = new HashSet<>();
    private static final Random RANDOM = new Random();

    // Cached counts per dimension
    private static int cachedOverworldCreeperCount = 0;
    private static int cachedNetherCreeperCount = 0;
    private static int cachedEndCreeperCount = 0;
    private static long lastOverworldCountTime = 0;
    private static long lastNetherCountTime = 0;
    private static long lastEndCountTime = 0;
    private static final long COUNT_CACHE_MS = 1000;

    static {
        // =============================================
        // ONLY THESE STAY NORMAL - EVERYTHING ELSE
        // BECOMES A CREEPER!!!
        // =============================================

        // === ESSENTIAL FOR GAME PROGRESSION ===
        NEVER_REPLACE.add(EntityType.BLAZE);           // Need blaze rods!
        NEVER_REPLACE.add(EntityType.VILLAGER);        // Need trading!
        NEVER_REPLACE.add(EntityType.WANDERING_TRADER);// Need trading!

        // === BOSSES ===
        NEVER_REPLACE.add(EntityType.ENDER_DRAGON);
        NEVER_REPLACE.add(EntityType.WITHER);

        // === FISH - DON'T SPAWN AT ALL ===
        FISH_TYPES.add(EntityType.COD);
        FISH_TYPES.add(EntityType.SALMON);
        FISH_TYPES.add(EntityType.TROPICAL_FISH);
        FISH_TYPES.add(EntityType.PUFFERFISH);

        // === THESE BECOME CREEPERS NOW! ===
        // Squid, Glow Squid, Dolphin, Axolotl, Tadpole, Frog, Turtle
        // ALL become CREEPERS!

        // === PLAYER (obviously) ===
        NEVER_REPLACE.add(EntityType.PLAYER);

        // === NON-MOB ENTITIES (items, projectiles, etc.) ===
        // These are checked separately - only LivingEntity can be replaced
    }

    /**
     * Count creepers in Overworld (cached for performance)
     */
    public static int countCreepersInOverworld(ServerWorld world) {
        if (!world.getRegistryKey().equals(World.OVERWORLD)) {
            return 0;
        }
        long now = System.currentTimeMillis();
        if (now - lastOverworldCountTime > COUNT_CACHE_MS) {
            cachedOverworldCreeperCount = 0;
            for (Entity entity : world.iterateEntities()) {
                if (entity instanceof CreeperEntity) {
                    cachedOverworldCreeperCount++;
                }
            }
            lastOverworldCountTime = now;
        }
        return cachedOverworldCreeperCount;
    }

    /**
     * Count creepers in the Nether (cached for performance)
     */
    public static int countCreepersInNether(ServerWorld world) {
        if (!world.getRegistryKey().equals(World.NETHER)) {
            return 0;
        }
        long now = System.currentTimeMillis();
        if (now - lastNetherCountTime > COUNT_CACHE_MS) {
            cachedNetherCreeperCount = 0;
            for (Entity entity : world.iterateEntities()) {
                if (entity instanceof CreeperEntity) {
                    cachedNetherCreeperCount++;
                }
            }
            lastNetherCountTime = now;
        }
        return cachedNetherCreeperCount;
    }

    /**
     * Count creepers in The End only (separate cache)
     */
    public static int countCreepersInEnd(ServerWorld world) {
        if (!world.getRegistryKey().equals(World.END)) {
            return 0;
        }
        long now = System.currentTimeMillis();
        if (now - lastEndCountTime > COUNT_CACHE_MS) {
            cachedEndCreeperCount = 0;
            for (Entity entity : world.iterateEntities()) {
                if (entity instanceof CreeperEntity) {
                    cachedEndCreeperCount++;
                }
            }
            lastEndCountTime = now;
        }
        return cachedEndCreeperCount;
    }

    /**
     * Can we spawn more creepers in this dimension?
     */
    public static boolean canSpawnMoreCreepers(ServerWorld world) {
        int dynamicLimit = getDynamicLimit(world);
        if (world.getRegistryKey().equals(World.OVERWORLD)) {
            return countCreepersInOverworld(world) < dynamicLimit;
        } else if (world.getRegistryKey().equals(World.NETHER)) {
            return countCreepersInNether(world) < dynamicLimit;
        } else if (world.getRegistryKey().equals(World.END)) {
            return countCreepersInEnd(world) < dynamicLimit;
        }
        // Custom dimensions use Overworld-configured limit but count local dimension creepers.
        return countCreepersInWorld(world) < dynamicLimit;
    }

    private static int countCreepersInWorld(ServerWorld world) {
        int count = 0;
        for (Entity entity : world.iterateEntities()) {
            if (entity instanceof CreeperEntity) {
                count++;
            }
        }
        return count;
    }

    public static boolean canSpawnMoreCreepersInEnd(ServerWorld world) {
        return countCreepersInEnd(world) < getDynamicLimit(world);
    }

    public static boolean canSpawnMoreCreepersInNether(ServerWorld world) {
        return countCreepersInNether(world) < getDynamicLimit(world);
    }

    private static int getDynamicLimit(ServerWorld world) {
        int baseLimit;
        if (world.getRegistryKey().equals(World.OVERWORLD)) {
            baseLimit = CreeperApocalypse.CONFIG.getMaxCreepersOverworld();
        } else if (world.getRegistryKey().equals(World.NETHER)) {
            baseLimit = CreeperApocalypse.CONFIG.getMaxCreepersNether();
        } else if (world.getRegistryKey().equals(World.END)) {
            baseLimit = CreeperApocalypse.CONFIG.getMaxCreepersEnd();
        } else {
            baseLimit = CreeperApocalypse.CONFIG.getMaxCreepersOverworld();
        }

        double mspt = PerformanceTracker.getAverageMspt();
        double multiplier;
        if (mspt <= 50.0) {
            multiplier = 1.0;
        } else if (mspt <= 60.0) {
            multiplier = 0.85;
        } else if (mspt <= 70.0) {
            multiplier = 0.7;
        } else if (mspt <= 80.0) {
            multiplier = 0.55;
        } else {
            multiplier = 0.4;
        }

        int adjusted = (int) Math.round(baseLimit * multiplier);
        return Math.max(50, adjusted);
    }

    /**
     * THE MAIN CHECK - Should this entity become a creeper?
     */
    public static boolean shouldReplace(Entity entity) {
        // Must be enabled
        if (!CreeperApocalypse.CONFIG.isEnabled()) {
            return false;
        }

        // MUST be a living entity (not items, projectiles, etc.)
        if (!(entity instanceof LivingEntity)) {
            return false;
        }

        // Already a variant? Don't replace our own custom guys!
        if (ModEntities.isVariant(entity.getType())) {
            return false;
        }

        EntityType<?> type = entity.getType();

        // FISH: Don't spawn at all (will return null in createReplacementCreeper)
        if (FISH_TYPES.contains(type)) {
            return true; // Intercept them so we can remove them
        }

        // Check the exclusion list
        if (NEVER_REPLACE.contains(type)) {
            return false;
        }

        // End-only cap: limit Enderman -> Creeper replacements in The End
        if (type == EntityType.ENDERMAN && entity.getEntityWorld() instanceof ServerWorld serverWorld) {
            if (serverWorld.getRegistryKey().equals(World.END) && !canSpawnMoreCreepersInEnd(serverWorld)) {
                return false; // Allow Enderman to spawn once End cap is hit
            }
        }

        // REMOVED: Check creeper limit
        // We now handle this in the mixin/replacement logic.
        // If limit is reached, we still return TRUE here so the mixin can intercept
        // and prevent the normal mob from spawning.

        // ========================================
        // IF WE GET HERE, IT BECOMES A CREEPER!
        // ========================================
        // This includes ALL of these from user's list:
        //
        // PASSIVE: Allay, Armadillo, Bat, Camel, Cat, Chicken, Cow, Donkey,
        //          Fox, Horse, Mooshroom, Mule, Ocelot, Parrot, Pig, Rabbit,
        //          Sheep, Skeleton Horse, Sniffer, Snow Golem, Strider
        //
        // NEUTRAL: Bee, Cave Spider, Enderman, Goat, Iron Golem, Llama,
        //          Panda, Piglin, Polar Bear, Spider, Trader Llama, Wolf,
        //          Zombified Piglin
        //
        // HOSTILE: Bogged, Breeze, Creaking, Drowned, Elder Guardian,
        //          Endermite, Evoker, Ghast, Guardian, Hoglin, Husk,
        //          Magma Cube, Phantom, Piglin Brute, Pillager, Ravager,
        //          Shulker, Silverfish, Skeleton, Slime, Stray, Vex,
        //          Vindicator, Warden, Witch, Wither Skeleton, Zoglin,
        //          Zombie, Zombie Horse, Zombie Villager
        //
        // UNUSED: Giant, Illusioner, Killer Bunny
        //
        return true;
    }

    /**
     * Get which type of creeper to spawn.
     */
    public static EntityType<?> getReplacementType() {
        int currentDay = CreeperApocalypse.CHALLENGE_DATA.getCurrentDay();
        return CreeperVariantSelector.select(RANDOM, currentDay);
    }

    /**
     * Create the replacement creeper
     */
    public static Entity createReplacementCreeper(ServerWorld world, Entity original) {
        EntityType<?> originalType = original.getType();

        // FISH: Don't spawn at all - return null to prevent spawn
        if (FISH_TYPES.contains(originalType)) {
            return null;
        }

        // End-only hard cap: never create more creepers in The End past the cap
        if (world.getRegistryKey().equals(World.END) && !canSpawnMoreCreepersInEnd(world)) {
            return null;
        }

        if (!canSpawnMoreCreepers(world)) {
            return null;
        }

        if (originalType == EntityType.ENDERMAN && world.getRegistryKey().equals(World.END)) {
            if (!canSpawnMoreCreepersInEnd(world)) {
                return null;
            }
        }

        EntityType<?> creeperType = getReplacementType();

        Entity replacement = createCreeperEntity(world, creeperType, SpawnReason.MOB_SUMMONED);
        if (replacement == null) {
            return null;
        }

        replacement.setPosition(original.getX(), original.getY(), original.getZ());
        replacement.setYaw(original.getYaw());
        replacement.setPitch(original.getPitch());

        if (replacement instanceof CreeperEntity creeper) {
            applyDayEnhancements(creeper);
        }

        incrementCachedCount(world);
        return replacement;
    }

    /**
     * Apply day-based enhancements (charged creepers etc)
     */
    public static void applyDayEnhancements(CreeperEntity creeper) {
        int currentDay = CreeperApocalypse.CHALLENGE_DATA.getCurrentDay();

        // Special handling for Lightning Creeper: set super charged on Day 7+
        if (creeper instanceof LightningCreeperEntity lightningCreeper) {
            if (currentDay >= 7 && CreeperApocalypse.CONFIG.chargedDayEnabled()) {
                float superChargeChance = 0.25f + ((currentDay - 7) * 0.05f);
                superChargeChance = Math.min(superChargeChance, 0.75f);
                if (RANDOM.nextFloat() < superChargeChance) {
                    lightningCreeper.setSuperCharged(true);
                    CreeperApocalypse.LOGGER.debug("Lightning Creeper became SUPER CHARGED (purple/pink)!");
                }
            }
            // Lightning Creeper is already charged, don't apply normal charged logic below
            var handler = CreeperApocalypse.getMilestoneHandler();
            if (handler != null) {
                handler.applySpawnEffects(creeper);
            }
            return;
        }

        // Day 7+: Charged creepers (normal logic for other creepers)
        if (currentDay >= 7 && CreeperApocalypse.CONFIG.chargedDayEnabled()) {
            float chargedChance = 0.25f + ((currentDay - 7) * 0.05f);
            chargedChance = Math.min(chargedChance, 0.75f);
            if (RANDOM.nextFloat() < chargedChance) {
                creeper.getDataTracker().set(CreeperEntity.CHARGED, true);
            }
        }

        var handler = CreeperApocalypse.getMilestoneHandler();
        if (handler != null) {
            handler.applySpawnEffects(creeper);
        }
    }

    /**
     * Get spawn count based on multiplier
     */
    public static int getSpawnCount() {
        float multiplier = CreeperApocalypse.CHALLENGE_DATA.getSpawnMultiplier();
        int base = 1;
        int bonus = 0;
        float remaining = multiplier - 1.0f;

        while (remaining > 0) {
            if (remaining >= 1.0f || RANDOM.nextFloat() < remaining) {
                bonus++;
            }
            remaining -= 1.0f;
        }

        return base + bonus;
    }

    /**
     * Spawn a creeper (or variant) directly at a position, respecting the cap.
     * This is used for forced spawning in special cases like The End surface.
     */
    public static Entity createApocalypseCreeper(ServerWorld world, net.minecraft.util.math.BlockPos pos) {
        if (!canSpawnMoreCreepers(world)) {
            return null;
        }

        if (world.getRegistryKey().equals(World.END) && !canSpawnMoreCreepersInEnd(world)) {
            return null;
        }

        EntityType<?> creeperType = getReplacementType();
        // Use SPAWNER reason so daylight doesn't block forced spawns on the surface.
        Entity replacement = createCreeperEntity(world, creeperType, SpawnReason.SPAWNER);
        if (replacement == null) {
            return null;
        }

        replacement.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);

        if (replacement instanceof CreeperEntity creeper) {
            applyDayEnhancements(creeper);
        }

        incrementCachedCount(world);
        return replacement;
    }

    private static Entity createCreeperEntity(ServerWorld world, EntityType<?> preferredType, SpawnReason reason) {
        Entity replacement = preferredType.create(world);
        if (replacement != null) {
            return replacement;
        }
        return EntityType.CREEPER.create(world);
    }

    private static void incrementCachedCount(ServerWorld world) {
        if (world.getRegistryKey().equals(World.OVERWORLD)) {
            cachedOverworldCreeperCount++;
        } else if (world.getRegistryKey().equals(World.NETHER)) {
            cachedNetherCreeperCount++;
        } else if (world.getRegistryKey().equals(World.END)) {
            cachedEndCreeperCount++;
        }
    }

    public static void decrementCreeperCount() {
        // Legacy method - decrement all caches slightly to avoid stale data
        if (cachedOverworldCreeperCount > 0) cachedOverworldCreeperCount--;
        if (cachedNetherCreeperCount > 0) cachedNetherCreeperCount--;
        if (cachedEndCreeperCount > 0) cachedEndCreeperCount--;
    }

    public static boolean isExcluded(EntityType<?> type) {
        return NEVER_REPLACE.contains(type);
    }

    public static int getMaxCreepers() {
        return CreeperApocalypse.CONFIG.getMaxCreepersOverworld();
    }

    public static int getMaxEndCreepers() {
        return CreeperApocalypse.CONFIG.getMaxCreepersEnd();
    }

    public static int getMaxNetherCreepers() {
        return CreeperApocalypse.CONFIG.getMaxCreepersNether();
    }

    public static int getCachedCreeperCount() {
        // Return total across all dimensions for HUD display
        return cachedOverworldCreeperCount + cachedNetherCreeperCount + cachedEndCreeperCount;
    }
}

