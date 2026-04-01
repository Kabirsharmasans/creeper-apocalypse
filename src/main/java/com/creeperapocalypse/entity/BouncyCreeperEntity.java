package com.creeperapocalypse.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;

/**
 * Bouncy Creeper - Hops around like a slime! Very unpredictable movement.
 * Visual: Cyan name + constantly jumping
 */
public class BouncyCreeperEntity extends CreeperEntity {

    private int bounceTicks = 0;

    public BouncyCreeperEntity(EntityType<? extends CreeperEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createBouncyCreeperAttributes() {
        return HostileEntity.createHostileAttributes()
            .add(EntityAttributes.GENERIC_MAX_HEALTH, 18.0)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.30)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 35.0);
    }

    @Override
    public void tick() {
        super.tick();
        bounceTicks++;

        if (bounceTicks % 20 == 0) {
            if (!hasStatusEffect(StatusEffects.JUMP_BOOST)) {
                addStatusEffect(new StatusEffectInstance(
                    StatusEffects.JUMP_BOOST,
                    40, 2, false, true, true
                ));
            }

            if (isOnGround() && random.nextFloat() < 0.3f) {
                setVelocity(getVelocity().add(
                    (random.nextFloat() - 0.5) * 0.5,
                    0.5 + random.nextFloat() * 0.3,
                    (random.nextFloat() - 0.5) * 0.5
                ));
            }
        }
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, net.minecraft.block.BlockState state, net.minecraft.util.math.BlockPos landedPosition) {
        // No fall damage - bouncy!
    }

    public String getVariantName() {
        return "bouncy";
    }
}

