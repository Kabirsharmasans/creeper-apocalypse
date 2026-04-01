package com.creeperapocalypse.entity;

import com.creeperapocalypse.CreeperApocalypse;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

/**
 * Registers custom creeper variant entities
 */
public class ModEntities {

    // Registry keys
    public static final RegistryKey<EntityType<?>> MINI_CREEPER_KEY = RegistryKey.of(
        RegistryKeys.ENTITY_TYPE, Identifier.of(CreeperApocalypse.MOD_ID, "mini_creeper")
    );
    public static final RegistryKey<EntityType<?>> GIANT_CREEPER_KEY = RegistryKey.of(
        RegistryKeys.ENTITY_TYPE, Identifier.of(CreeperApocalypse.MOD_ID, "giant_creeper")
    );
    public static final RegistryKey<EntityType<?>> SPIDER_CREEPER_KEY = RegistryKey.of(
        RegistryKeys.ENTITY_TYPE, Identifier.of(CreeperApocalypse.MOD_ID, "spider_creeper")
    );
    public static final RegistryKey<EntityType<?>> NINJA_CREEPER_KEY = RegistryKey.of(
        RegistryKeys.ENTITY_TYPE, Identifier.of(CreeperApocalypse.MOD_ID, "ninja_creeper")
    );
    public static final RegistryKey<EntityType<?>> RAINBOW_CREEPER_KEY = RegistryKey.of(
        RegistryKeys.ENTITY_TYPE, Identifier.of(CreeperApocalypse.MOD_ID, "rainbow_creeper")
    );
    public static final RegistryKey<EntityType<?>> BOUNCY_CREEPER_KEY = RegistryKey.of(
        RegistryKeys.ENTITY_TYPE, Identifier.of(CreeperApocalypse.MOD_ID, "bouncy_creeper")
    );
    public static final RegistryKey<EntityType<?>> JOCKEY_CREEPER_KEY = RegistryKey.of(
        RegistryKeys.ENTITY_TYPE, Identifier.of(CreeperApocalypse.MOD_ID, "jockey_creeper")
    );
    public static final RegistryKey<EntityType<?>> HAPPY_CREEPER_KEY = RegistryKey.of(
        RegistryKeys.ENTITY_TYPE, Identifier.of(CreeperApocalypse.MOD_ID, "happy_creeper")
    );
    public static final RegistryKey<EntityType<?>> LIGHTNING_CREEPER_KEY = RegistryKey.of(
        RegistryKeys.ENTITY_TYPE, Identifier.of(CreeperApocalypse.MOD_ID, "lightning_creeper")
    );
    
    // Mini Creeper - smaller, faster, less damage
    public static final EntityType<MiniCreeperEntity> MINI_CREEPER = Registry.register(
        Registries.ENTITY_TYPE,
        MINI_CREEPER_KEY,
        EntityType.Builder.<MiniCreeperEntity>create(MiniCreeperEntity::new, SpawnGroup.MONSTER)
            .dimensions(0.3f, 0.85f) // Half size
                .build(MINI_CREEPER_KEY.getValue().toString())
    );

    // Giant Creeper - larger, slower, massive explosion
    public static final EntityType<GiantCreeperEntity> GIANT_CREEPER = Registry.register(
        Registries.ENTITY_TYPE,
        GIANT_CREEPER_KEY,
        EntityType.Builder.<GiantCreeperEntity>create(GiantCreeperEntity::new, SpawnGroup.MONSTER)
            .dimensions(1.2f, 3.4f) // Double size
                .build(GIANT_CREEPER_KEY.getValue().toString())
    );

    // Spider Creeper - can climb walls
    public static final EntityType<SpiderCreeperEntity> SPIDER_CREEPER = Registry.register(
        Registries.ENTITY_TYPE,
        SPIDER_CREEPER_KEY,
        EntityType.Builder.<SpiderCreeperEntity>create(SpiderCreeperEntity::new, SpawnGroup.MONSTER)
            .dimensions(0.6f, 1.7f)
                .build(SPIDER_CREEPER_KEY.getValue().toString())
    );

    // Ninja Creeper - invisible until close
    public static final EntityType<NinjaCreeperEntity> NINJA_CREEPER = Registry.register(
        Registries.ENTITY_TYPE,
        NINJA_CREEPER_KEY,
        EntityType.Builder.<NinjaCreeperEntity>create(NinjaCreeperEntity::new, SpawnGroup.MONSTER)
            .dimensions(0.6f, 1.7f)
                .build(NINJA_CREEPER_KEY.getValue().toString())
    );

    // Rainbow Creeper - colorful and glowing
    public static final EntityType<RainbowCreeperEntity> RAINBOW_CREEPER = Registry.register(
        Registries.ENTITY_TYPE,
        RAINBOW_CREEPER_KEY,
        EntityType.Builder.<RainbowCreeperEntity>create(RainbowCreeperEntity::new, SpawnGroup.MONSTER)
            .dimensions(0.6f, 1.7f)
                .build(RAINBOW_CREEPER_KEY.getValue().toString())
    );

    // Bouncy Creeper - hops around like slime
    public static final EntityType<BouncyCreeperEntity> BOUNCY_CREEPER = Registry.register(
        Registries.ENTITY_TYPE,
        BOUNCY_CREEPER_KEY,
        EntityType.Builder.<BouncyCreeperEntity>create(BouncyCreeperEntity::new, SpawnGroup.MONSTER)
            .dimensions(0.6f, 1.7f)
                .build(BOUNCY_CREEPER_KEY.getValue().toString())
    );

    // Jockey Creeper - mini creeper rides on top!
    public static final EntityType<JockeyCreeperEntity> JOCKEY_CREEPER = Registry.register(
        Registries.ENTITY_TYPE,
        JOCKEY_CREEPER_KEY,
        EntityType.Builder.<JockeyCreeperEntity>create(JockeyCreeperEntity::new, SpawnGroup.MONSTER)
            .dimensions(0.6f, 1.7f)
                .build(JOCKEY_CREEPER_KEY.getValue().toString())
    );

    // Happy Creeper - Heals players
    public static final EntityType<HappyCreeperEntity> HAPPY_CREEPER = Registry.register(
        Registries.ENTITY_TYPE,
        HAPPY_CREEPER_KEY,
        EntityType.Builder.<HappyCreeperEntity>create(HappyCreeperEntity::new, SpawnGroup.MONSTER)
            .dimensions(0.6f, 1.7f)
                .build(HAPPY_CREEPER_KEY.getValue().toString())
    );

    // Lightning Creeper - Boss variant
    public static final EntityType<LightningCreeperEntity> LIGHTNING_CREEPER = Registry.register(
        Registries.ENTITY_TYPE,
        LIGHTNING_CREEPER_KEY,
        EntityType.Builder.<LightningCreeperEntity>create(LightningCreeperEntity::new, SpawnGroup.MONSTER)
            .dimensions(0.6f, 1.7f)
                .build(LIGHTNING_CREEPER_KEY.getValue().toString())
    );

    /**
     * Registers all custom entities and their attributes
     */
    public static void register() {
        CreeperApocalypse.LOGGER.info("Registering custom creeper variants...");

        // Register entity attributes - THIS IS REQUIRED!
        FabricDefaultAttributeRegistry.register(MINI_CREEPER, MiniCreeperEntity.createMiniCreeperAttributes());
        FabricDefaultAttributeRegistry.register(GIANT_CREEPER, GiantCreeperEntity.createGiantCreeperAttributes());
        FabricDefaultAttributeRegistry.register(SPIDER_CREEPER, SpiderCreeperEntity.createSpiderCreeperAttributes());
        FabricDefaultAttributeRegistry.register(NINJA_CREEPER, NinjaCreeperEntity.createNinjaCreeperAttributes());
        FabricDefaultAttributeRegistry.register(RAINBOW_CREEPER, RainbowCreeperEntity.createRainbowCreeperAttributes());
        FabricDefaultAttributeRegistry.register(BOUNCY_CREEPER, BouncyCreeperEntity.createBouncyCreeperAttributes());
        FabricDefaultAttributeRegistry.register(JOCKEY_CREEPER, JockeyCreeperEntity.createJockeyCreeperAttributes());
        FabricDefaultAttributeRegistry.register(HAPPY_CREEPER, HappyCreeperEntity.createHappyCreeperAttributes());
        FabricDefaultAttributeRegistry.register(LIGHTNING_CREEPER, LightningCreeperEntity.createLightningCreeperAttributes());

        CreeperApocalypse.LOGGER.info("Registered 9 creeper variants");
    }

    public static boolean isVariant(EntityType<?> type) {
        return type == MINI_CREEPER ||
               type == GIANT_CREEPER ||
               type == SPIDER_CREEPER ||
               type == NINJA_CREEPER ||
               type == HAPPY_CREEPER ||
               type == LIGHTNING_CREEPER ||
               type == RAINBOW_CREEPER ||
               type == BOUNCY_CREEPER ||
               type == JOCKEY_CREEPER;
    }
}

