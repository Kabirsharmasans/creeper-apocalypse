package com.creeperapocalypse.util;

import com.creeperapocalypse.CreeperApocalypse;
import com.creeperapocalypse.entity.ModEntities;
import net.minecraft.entity.EntityType;

import java.util.Random;

final class CreeperVariantSelector {

    private static final int TOTAL_VARIANTS_WITH_NORMAL = 10;
    private static final int DAY_VARIANTS_START = 3;
    private static final int DAY_MINI_BOUNCY_START = 5;
    private static final int DAY_ALL_VARIANTS_START = 8;
    private static final float BASE_RARITY_MULTIPLIER = 0.35f;

    private CreeperVariantSelector() {
    }

    static EntityType<?> select(Random random, int currentDay) {
        if (!CreeperApocalypse.CONFIG.specialVariantsEnabled()) {
            return EntityType.CREEPER;
        }

        if (CreeperApocalypse.CONFIG.ignoreDayProgression()) {
            return selectNoDayProgression(random);
        }

        if (currentDay < DAY_VARIANTS_START) {
            return EntityType.CREEPER;
        }

        VariantChances chances = computeVariantChances(currentDay);
        return rollVariant(random.nextFloat(), chances);
    }

    private static EntityType<?> selectNoDayProgression(Random random) {
        return switch (random.nextInt(TOTAL_VARIANTS_WITH_NORMAL)) {
            case 0 -> ModEntities.LIGHTNING_CREEPER;
            case 1 -> ModEntities.HAPPY_CREEPER;
            case 2 -> ModEntities.JOCKEY_CREEPER;
            case 3 -> ModEntities.BOUNCY_CREEPER;
            case 4 -> ModEntities.SPIDER_CREEPER;
            case 5 -> ModEntities.RAINBOW_CREEPER;
            case 6 -> ModEntities.NINJA_CREEPER;
            case 7 -> ModEntities.GIANT_CREEPER;
            case 8 -> ModEntities.MINI_CREEPER;
            default -> EntityType.CREEPER;
        };
    }

    private static VariantChances computeVariantChances(int currentDay) {
        float dayBonus = Math.max(0, currentDay - DAY_VARIANTS_START) * 0.005f;

        float miniChance = scaledChance(CreeperApocalypse.CONFIG.getMiniCreeperChance(), dayBonus);
        float giantChance = scaledChance(CreeperApocalypse.CONFIG.getGiantCreeperChance(), dayBonus);
        float spiderChance = scaledChance(CreeperApocalypse.CONFIG.getSpiderCreeperChance(), dayBonus);
        float ninjaChance = scaledChance(CreeperApocalypse.CONFIG.getNinjaCreeperChance(), dayBonus);
        float rainbowChance = scaledChance(CreeperApocalypse.CONFIG.getRainbowCreeperChance(), dayBonus);
        float bouncyChance = scaledChance(CreeperApocalypse.CONFIG.getBouncyCreeperChance(), dayBonus);
        float jockeyChance = scaledChance(CreeperApocalypse.CONFIG.getJockeyCreeperChance(), dayBonus);
        float happyChance = scaledChance(CreeperApocalypse.CONFIG.getHappyCreeperChance(), dayBonus);
        float lightningChance = scaledChance(CreeperApocalypse.CONFIG.getLightningCreeperChance(), dayBonus);

        if (currentDay < DAY_MINI_BOUNCY_START) {
            miniChance = 0.0f;
            bouncyChance = 0.0f;
        }

        if (currentDay < DAY_ALL_VARIANTS_START) {
            giantChance = 0.0f;
            rainbowChance = 0.0f;
            lightningChance = 0.0f;
        }

        return new VariantChances(
            lightningChance,
            happyChance,
            jockeyChance,
            bouncyChance,
            spiderChance,
            rainbowChance,
            giantChance,
            ninjaChance,
            miniChance
        );
    }

    private static float scaledChance(float configuredChance, float dayBonus) {
        return configuredChance * BASE_RARITY_MULTIPLIER + dayBonus;
    }

    private static EntityType<?> rollVariant(float initialRoll, VariantChances chances) {
        float roll = initialRoll;

        if (roll < chances.lightning()) {
            CreeperApocalypse.LOGGER.debug("Spawning LIGHTNING creeper (roll: " + roll + ")");
            return ModEntities.LIGHTNING_CREEPER;
        }
        roll -= chances.lightning();

        if (roll < chances.happy()) {
            CreeperApocalypse.LOGGER.debug("Spawning HAPPY creeper (roll: " + roll + ")");
            return ModEntities.HAPPY_CREEPER;
        }
        roll -= chances.happy();

        if (roll < chances.jockey()) {
            CreeperApocalypse.LOGGER.debug("Spawning JOCKEY creeper (roll: " + roll + ")");
            return ModEntities.JOCKEY_CREEPER;
        }
        roll -= chances.jockey();

        if (roll < chances.bouncy()) {
            CreeperApocalypse.LOGGER.debug("Spawning BOUNCY creeper (roll: " + roll + ")");
            return ModEntities.BOUNCY_CREEPER;
        }
        roll -= chances.bouncy();

        if (roll < chances.spider()) {
            CreeperApocalypse.LOGGER.debug("Spawning SPIDER creeper (roll: " + roll + ")");
            return ModEntities.SPIDER_CREEPER;
        }
        roll -= chances.spider();

        if (roll < chances.rainbow()) {
            CreeperApocalypse.LOGGER.debug("Spawning RAINBOW creeper (roll: " + roll + ")");
            return ModEntities.RAINBOW_CREEPER;
        }
        roll -= chances.rainbow();

        if (roll < chances.giant()) {
            CreeperApocalypse.LOGGER.debug("Spawning GIANT creeper (roll: " + roll + ")");
            return ModEntities.GIANT_CREEPER;
        }
        roll -= chances.giant();

        if (roll < chances.ninja()) {
            CreeperApocalypse.LOGGER.debug("Spawning NINJA creeper (roll: " + roll + ")");
            return ModEntities.NINJA_CREEPER;
        }
        roll -= chances.ninja();

        if (roll < chances.mini()) {
            CreeperApocalypse.LOGGER.debug("Spawning MINI creeper (roll: " + roll + ")");
            return ModEntities.MINI_CREEPER;
        }

        return EntityType.CREEPER;
    }

    private record VariantChances(
        float lightning,
        float happy,
        float jockey,
        float bouncy,
        float spider,
        float rainbow,
        float giant,
        float ninja,
        float mini
    ) {
    }
}
