package com.creeperapocalypse.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.world.World;
import net.minecraft.server.world.ServerWorld;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.server.world.ServerWorld;

/**
 * Jockey Creeper - A regular creeper with a Mini Creeper riding on top!
 * When this creeper dies, the mini creeper continues attacking!
 * Visual: Has a visible rider and special name
 */
public class JockeyCreeperEntity extends CreeperEntity {

    private boolean hasSpawnedRider = false;

    public JockeyCreeperEntity(EntityType<? extends CreeperEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createJockeyCreeperAttributes() {
        return HostileEntity.createHostileAttributes()
            .add(EntityAttributes.MAX_HEALTH, 25.0)
            .add(EntityAttributes.MOVEMENT_SPEED, 0.22)
            .add(EntityAttributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        // Immune to Creeper explosions, but vulnerable to TNT/Players
        if (source.getAttacker() instanceof CreeperEntity) {
            return false;
        }
        return super.damage(world, source, amount);
    }

    @Override
    public void tick() {
        super.tick();

        if (!hasSpawnedRider && age > 1) {
            spawnRider();
            hasSpawnedRider = true;
        }
    }

    private void spawnRider() {
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld serverWorld) {
            MiniCreeperEntity rider = ModEntities.MINI_CREEPER.create(serverWorld, SpawnReason.JOCKEY);
            if (rider != null) {
                rider.setPosition(getX(), getY(), getZ());
                serverWorld.spawnEntity(rider);
                rider.startRiding(this);
            }
        }
    }

    @Override
    public void onDeath(net.minecraft.entity.damage.DamageSource damageSource) {
        removeAllPassengers();
        super.onDeath(damageSource);
    }

    public void protectRiderFromExplosion() {
        for (Entity passenger : this.getPassengerList()) {
            if (passenger instanceof MiniCreeperEntity mini) {
                mini.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 60, 4, false, false, true));
                mini.stopRiding();
            }
        }
    }

    public String getVariantName() {
        return "jockey";
    }
}

