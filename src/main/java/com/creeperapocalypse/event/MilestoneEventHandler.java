package com.creeperapocalypse.event;

import com.creeperapocalypse.CreeperApocalypse;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

/**
 * Handles milestone-specific events and effects
 */
public class MilestoneEventHandler {

    private boolean bloodMoonActive = false;
    private boolean chargedDayActive = false;
    private boolean swarmActive = false;
    private int currentDay = 1;

    public enum MilestoneType {
        BLOOD_MOON,
        CHARGED_DAY,
        THE_SWARM
    }

    public void updateFromChallengeData() {
        currentDay = CreeperApocalypse.CHALLENGE_DATA.getCurrentDay();
        bloodMoonActive = CreeperApocalypse.CHALLENGE_DATA.isBloodMoonTriggered() && currentDay >= 5;
        chargedDayActive = CreeperApocalypse.CHALLENGE_DATA.isChargedDayTriggered() && currentDay >= 7;
        swarmActive = CreeperApocalypse.CHALLENGE_DATA.isSwarmDayTriggered() && currentDay >= 10;
    }

    public boolean isActive(MilestoneType type) {
        updateFromChallengeData();
        return switch (type) {
            case BLOOD_MOON -> bloodMoonActive && CreeperApocalypse.CONFIG.bloodMoonEnabled();
            case CHARGED_DAY -> chargedDayActive && CreeperApocalypse.CONFIG.chargedDayEnabled();
            case THE_SWARM -> swarmActive && CreeperApocalypse.CONFIG.swarmDayEnabled();
        };
    }

    public float getMilestoneSpawnBonus() {
        float bonus = 0.0f;
        if (isActive(MilestoneType.BLOOD_MOON)) bonus += 0.5f;
        if (isActive(MilestoneType.THE_SWARM)) bonus += 1.0f;
        return bonus;
    }

    public float getChargedChance() {
        if (!isActive(MilestoneType.CHARGED_DAY)) return 0.0f;
        float baseChance = 0.25f;
        int daysAfterSeven = Math.max(0, currentDay - 7);
        return Math.min(0.75f, baseChance + (daysAfterSeven * 0.05f));
    }

    public float getVariantSpawnRate() {
        float rate = 0.0f;
        if (currentDay >= 3) rate += 0.1f;
        if (currentDay >= 5) rate += 0.05f;
        if (currentDay >= 7) rate += 0.08f;
        if (isActive(MilestoneType.THE_SWARM)) rate *= 1.5f;
        return rate;
    }

    public void applySpawnEffects(CreeperEntity creeper) {
        if (!CreeperApocalypse.CONFIG.isEnabled()) return;

        updateFromChallengeData();

        if (isActive(MilestoneType.BLOOD_MOON)) {
            creeper.addStatusEffect(new StatusEffectInstance(
                StatusEffects.GLOWING,
                Integer.MAX_VALUE,
                0, false, false, true
            ));
        }

        if (isActive(MilestoneType.CHARGED_DAY)) {
            float chargeChance = getChargedChance();
            if (creeper.getRandom().nextFloat() < chargeChance) {
                // Set creeper to charged state using data tracker
                CreeperApocalypse.LOGGER.debug("Spawned CHARGED creeper (Day " + currentDay + ")");
            }
        }

        if (isActive(MilestoneType.THE_SWARM)) {
            creeper.addStatusEffect(new StatusEffectInstance(
                StatusEffects.SPEED,
                Integer.MAX_VALUE,
                0, false, false, false
            ));
        }
    }

    public void onCreeperExplode(CreeperEntity creeper) {
        if (isActive(MilestoneType.BLOOD_MOON)) {
            if (creeper.getRandom().nextFloat() < 0.2f) {
                CreeperApocalypse.LOGGER.debug("Blood Moon: Spawning additional creeper from explosion");
            }
        }
    }

    public void reset() {
        bloodMoonActive = false;
        chargedDayActive = false;
        swarmActive = false;
        currentDay = 1;
    }

    public boolean isBloodMoonActive() { return bloodMoonActive; }
    public boolean isChargedDayActive() { return chargedDayActive; }
    public boolean isSwarmActive() { return swarmActive; }
    public int getCurrentDay() { return currentDay; }

    public String getMilestoneStatus() {
        updateFromChallengeData();
        StringBuilder sb = new StringBuilder();
        if (isActive(MilestoneType.BLOOD_MOON)) sb.append("§4[BLOOD MOON] ");
        if (isActive(MilestoneType.CHARGED_DAY)) sb.append("§b[CHARGED DAY] ");
        if (isActive(MilestoneType.THE_SWARM)) sb.append("§2[THE SWARM] ");
        return sb.toString().trim();
    }
}

