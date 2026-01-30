package com.creeperapocalypse.util;

import com.creeperapocalypse.CreeperApocalypse;
import com.creeperapocalypse.entity.ModEntities;
import com.creeperapocalypse.util.PerformanceTracker;
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
 * PASSIVE → CREEPER:
 * Allay, Armadillo, Bat, Camel, Cat, Chicken, Cow, Donkey, Fox, Horse,
 * Mooshroom, Mule, Ocelot, Parrot, Pig, Rabbit, Sheep, Skeleton Horse,
 * Sniffer, Snow Golem, Strider
 * 
 * NEUTRAL → CREEPER:
 * Bee, Cave Spider, Enderman, Goat, Iron Golem, Llama, Panda, Piglin,
 * Polar Bear, Spider, Trader Llama, Wolf, Zombified Piglin
 * 
 * HOSTILE → CREEPER:
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
        // Custom dimensions - use Overworld limit
        return countCreepersInOverworld(world) < dynamicLimit;
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
     * Get which type of creeper to spawn - variants spawn from Day 1!
     * Uses config-based chances from GUI settings.
     */
    public static EntityType<?> getReplacementType(EntityType<?> original) {
        // Check if variants are enabled in config
        if (!CreeperApocalypse.CONFIG.specialVariantsEnabled()) {
            return EntityType.CREEPER;
        }
        
        // NO DAY MODE: Spawn all creepers with equal chance, ignoring day progression
        if (CreeperApocalypse.CONFIG.ignoreDayProgression()) {
            // 10 types total: Normal + 9 variants
            // 10% chance for each variant, otherwise normal
            // Actually, let's just use equal weights for all
            int variant = RANDOM.nextInt(10);
            switch (variant) {
                case 0: return ModEntities.LIGHTNING_CREEPER;
                case 1: return ModEntities.HAPPY_CREEPER;
                case 2: return ModEntities.JOCKEY_CREEPER;
                case 3: return ModEntities.BOUNCY_CREEPER;
                case 4: return ModEntities.SPIDER_CREEPER;
                case 5: return ModEntities.RAINBOW_CREEPER;
                case 6: return ModEntities.NINJA_CREEPER;
                case 7: return ModEntities.GIANT_CREEPER;
                case 8: return ModEntities.MINI_CREEPER;
                default: return EntityType.CREEPER; // 9 = Normal
            }
        }
        
        int currentDay = CreeperApocalypse.CHALLENGE_DATA.getCurrentDay();
        float roll = RANDOM.nextFloat();

        // Day gates for variants
        boolean allowSomeVariants = currentDay >= 3;
        boolean allowMiniAndBouncy = currentDay >= 5;
        boolean allowAllVariants = currentDay >= 8;

        // No variants before Day 3
        if (!allowSomeVariants) {
            return EntityType.CREEPER;
        }

        // Make variants rarer overall, then scale slightly by day
        float rarityMultiplier = 0.35f;
        float dayBonus = Math.max(0, currentDay - 3) * 0.005f;
        
        // Get chances from config (set in GUI)
        float miniChance = (CreeperApocalypse.CONFIG.getMiniCreeperChance() * rarityMultiplier) + dayBonus;
        float giantChance = (CreeperApocalypse.CONFIG.getGiantCreeperChance() * rarityMultiplier) + dayBonus;
        float spiderChance = (CreeperApocalypse.CONFIG.getSpiderCreeperChance() * rarityMultiplier) + dayBonus;
        
        // Fixed chances for other variants
        float ninjaChance = (CreeperApocalypse.CONFIG.getNinjaCreeperChance() * rarityMultiplier) + dayBonus;
        float rainbowChance = (CreeperApocalypse.CONFIG.getRainbowCreeperChance() * rarityMultiplier) + dayBonus;
        float bouncyChance = (CreeperApocalypse.CONFIG.getBouncyCreeperChance() * rarityMultiplier) + dayBonus;
        float jockeyChance = (CreeperApocalypse.CONFIG.getJockeyCreeperChance() * rarityMultiplier) + dayBonus;
        float happyChance = (CreeperApocalypse.CONFIG.getHappyCreeperChance() * rarityMultiplier) + dayBonus;
        float lightningChance = (CreeperApocalypse.CONFIG.getLightningCreeperChance() * rarityMultiplier) + dayBonus;

        // Gate mini + bouncy until Day 5
        if (!allowMiniAndBouncy) {
            miniChance = 0.0f;
            bouncyChance = 0.0f;
        }

        // Gate some variants until Day 8 (Giant, Rainbow, and Lightning are late-game)
        if (!allowAllVariants) {
            giantChance = 0.0f;
            rainbowChance = 0.0f;
            lightningChance = 0.0f;
            // Happy creeper should be available from Day 3, so we don't zero it here
        }

        // Roll for each variant type
        
        // Lightning Creeper
        if (roll < lightningChance) {
            CreeperApocalypse.LOGGER.debug("Spawning LIGHTNING creeper (roll: " + roll + ")");
            return ModEntities.LIGHTNING_CREEPER;
        }
        roll -= lightningChance;

        // Happy Creeper
        if (roll < happyChance) {
            CreeperApocalypse.LOGGER.debug("Spawning HAPPY creeper (roll: " + roll + ")");
            return ModEntities.HAPPY_CREEPER;
        }
        roll -= happyChance;

        // Jockey Creeper
        if (roll < jockeyChance) {
            CreeperApocalypse.LOGGER.debug("Spawning JOCKEY creeper (roll: " + roll + ")");
            return ModEntities.JOCKEY_CREEPER;
        }
        roll -= jockeyChance;
        
        // Bouncy Creeper
        if (roll < bouncyChance) {
            CreeperApocalypse.LOGGER.debug("Spawning BOUNCY creeper (roll: " + roll + ")");
            return ModEntities.BOUNCY_CREEPER;
        }
        roll -= bouncyChance;
        
        // Spider Creeper
        if (roll < spiderChance) {
            CreeperApocalypse.LOGGER.debug("Spawning SPIDER creeper (roll: " + roll + ")");
            return ModEntities.SPIDER_CREEPER;
        }
        roll -= spiderChance;
        
        // Rainbow Creeper
        if (roll < rainbowChance) {
            CreeperApocalypse.LOGGER.debug("Spawning RAINBOW creeper (roll: " + roll + ")");
            return ModEntities.RAINBOW_CREEPER;
        }
        roll -= rainbowChance;
        
        // Giant Creeper
        if (roll < giantChance) {
            CreeperApocalypse.LOGGER.debug("Spawning GIANT creeper (roll: " + roll + ")");
            return ModEntities.GIANT_CREEPER;
        }
        roll -= giantChance;
        
        // Ninja Creeper
        if (roll < ninjaChance) {
            CreeperApocalypse.LOGGER.debug("Spawning NINJA creeper (roll: " + roll + ")");
            return ModEntities.NINJA_CREEPER;
        }
        roll -= ninjaChance;
        
        // Mini Creeper
        if (roll < miniChance) {
            CreeperApocalypse.LOGGER.debug("Spawning MINI creeper (roll: " + roll + ")");
            return ModEntities.MINI_CREEPER;
        }
        
        return EntityType.CREEPER;
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
        
        EntityType<?> creeperType = getReplacementType(originalType);
        
        Entity replacement = creeperType.create(world, SpawnReason.MOB_SUMMONED);
        if (replacement == null) {
            replacement = EntityType.CREEPER.create(world, SpawnReason.MOB_SUMMONED);
            if (replacement == null) return null;
        }
        
        replacement.setPosition(original.getX(), original.getY(), original.getZ());
        replacement.setYaw(original.getYaw());
        replacement.setPitch(original.getPitch());
        
        if (replacement instanceof CreeperEntity creeper) {
            applyDayEnhancements(creeper);
        }
        
        // Increment dimension-specific cache
        if (world.getRegistryKey().equals(World.OVERWORLD)) {
            cachedOverworldCreeperCount++;
        } else if (world.getRegistryKey().equals(World.NETHER)) {
            cachedNetherCreeperCount++;
        } else if (world.getRegistryKey().equals(World.END)) {
            cachedEndCreeperCount++;
        }
        return replacement;
    }
    
    /**
     * Apply day-based enhancements (charged creepers etc)
     */
    public static void applyDayEnhancements(CreeperEntity creeper) {
        int currentDay = CreeperApocalypse.CHALLENGE_DATA.getCurrentDay();
        
        // Special handling for Lightning Creeper: set super charged on Day 7+
        if (creeper instanceof com.creeperapocalypse.entity.LightningCreeperEntity lightningCreeper) {
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

        EntityType<?> creeperType = getReplacementType(EntityType.ENDERMAN);
        // Use SPAWNER reason so daylight doesn't block forced spawns on the surface.
        Entity replacement = creeperType.create(world, SpawnReason.SPAWNER);
        if (replacement == null) {
            replacement = EntityType.CREEPER.create(world, SpawnReason.SPAWNER);
            if (replacement == null) return null;
        }

        replacement.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);

        if (replacement instanceof CreeperEntity creeper) {
            applyDayEnhancements(creeper);
        }

        // Increment dimension-specific cache
        if (world.getRegistryKey().equals(World.OVERWORLD)) {
            cachedOverworldCreeperCount++;
        } else if (world.getRegistryKey().equals(World.NETHER)) {
            cachedNetherCreeperCount++;
        } else if (world.getRegistryKey().equals(World.END)) {
            cachedEndCreeperCount++;
        }
        return replacement;
    }
    
    public static void decrementCreeperCount() {
        // Legacy method - decrement all caches slightly to avoid stale data
        if (cachedOverworldCreeperCount > 0) cachedOverworldCreeperCount--;
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
