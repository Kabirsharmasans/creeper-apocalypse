package com.creeperapocalypse.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.world.World;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Lightning Creeper - Boss variant. Always charged.
 * When "super charged" (Day 7+), becomes purple/pink and gains massive speed boost.
 */
public class LightningCreeperEntity extends CreeperEntity {

    private boolean superCharged = false;
    private int particleTick = 0;
    private final World entityWorld;

    public LightningCreeperEntity(EntityType<? extends CreeperEntity> entityType, World world) {
        super(entityType, world);
        this.entityWorld = world;
    }

    public static DefaultAttributeContainer.Builder createLightningCreeperAttributes() {
        return HostileEntity.createHostileAttributes()
            .add(EntityAttributes.MAX_HEALTH, 10.0) // Original HP restored
            .add(EntityAttributes.MOVEMENT_SPEED, 0.45) // Original speed restored
            .add(EntityAttributes.FOLLOW_RANGE, 32.0); // NERFED: Follow range (was 40)
    }

    public void setSuperCharged(boolean superCharged) {
        this.superCharged = superCharged;
        if (superCharged) {
            // Original super charged stats restored
            this.getAttributeInstance(EntityAttributes.MAX_HEALTH).setBaseValue(9.0);
            this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).setBaseValue(0.4875);
            this.setHealth(9.0f);
        }
    }

    public boolean isSuperCharged() {
        return this.superCharged;
    }

    @Override
    public void tick() {
        super.tick();
        // Force charged state always
           if (!this.entityWorld.isClient() && !this.isCharged()) {
             this.dataTracker.set(CreeperEntity.CHARGED, true);
        }

        // Purple/pink particles for super charged state
        if (this.superCharged) {
            particleTick++;
            if (particleTick % 4 == 0 && this.entityWorld.isClient()) {
                // Spawn purple/pink particles (use dragon breath for purple, or mix colors)
                double x = this.getX() + (this.random.nextDouble() - 0.5) * 0.5;
                double y = this.getY() + this.random.nextDouble() * 1.5;
                double z = this.getZ() + (this.random.nextDouble() - 0.5) * 0.5;

                // Purple particles (use WITCH for purple magic effect as it is SimpleParticleType)
                spawnParticleCompat(net.minecraft.particle.ParticleTypes.WITCH, x, y, z, 0.0, 0.02, 0.0);

                // Pink particles (effect - use portal for pink-ish effect)
                if (this.random.nextFloat() < 0.3f) {
                    spawnParticleCompat(net.minecraft.particle.ParticleTypes.PORTAL, x, y, z,
                        (this.random.nextDouble() - 0.5) * 0.1,
                        (this.random.nextDouble() - 0.5) * 0.1,
                        (this.random.nextDouble() - 0.5) * 0.1);
                }
            }
        }
    }

    // NERFED: Fuse time only 10% faster when super charged (was 20%)
    public int getModifiedFuseTime(int baseFuseTime) {
        if (this.superCharged) {
            return (int) (baseFuseTime * 0.9); // 10% faster (was 20%)
        }
        return baseFuseTime;
    }

    // NERFED: +1 explosion radius when super charged (was +2)
    public int getExplosionRadius() {
        return this.superCharged ? 4 : 3; // Normal creeper = 3, super = 4 (was 5)
    }

    public void writeCustomDataToNbt(NbtCompound nbt) {
        invokeLegacySuperNbt("writeCustomDataToNbt", nbt);
        nbt.putBoolean("SuperCharged", this.superCharged);
    }

    public void readCustomDataFromNbt(NbtCompound nbt) {
        invokeLegacySuperNbt("readCustomDataFromNbt", nbt);
        setSuperCharged(readBooleanCompat(nbt, "SuperCharged", false));
    }

    private void spawnParticleCompat(ParticleEffect effect, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        World world = this.entityWorld;
        if (!tryParticleMethod(world, "addParticle", effect, x, y, z, velocityX, velocityY, velocityZ)) {
            tryParticleMethod(world, "addParticleClient", effect, x, y, z, velocityX, velocityY, velocityZ);
        }
    }

    private boolean tryParticleMethod(World world, String methodName, ParticleEffect effect, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        try {
            Method method = world.getClass().getMethod(methodName,
                ParticleEffect.class,
                double.class,
                double.class,
                double.class,
                double.class,
                double.class,
                double.class);
            method.invoke(world, effect, x, y, z, velocityX, velocityY, velocityZ);
            return true;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    private void invokeLegacySuperNbt(String methodName, NbtCompound nbt) {
        try {
            MethodHandles.lookup()
                .findSpecial(CreeperEntity.class, methodName, MethodType.methodType(void.class, NbtCompound.class), LightningCreeperEntity.class)
                .bindTo(this)
                .invoke(nbt);
        } catch (Throwable ignored) {
            // Newer versions use different serialization APIs; this fallback keeps compatibility.
        }
    }

    private boolean readBooleanCompat(NbtCompound nbt, String key, boolean fallback) {
        try {
            Method method = nbt.getClass().getMethod("getBoolean", String.class);
            Object value = method.invoke(nbt, key);

            if (value instanceof Boolean boolValue) {
                return boolValue;
            }

            if (value instanceof Optional<?> optionalValue) {
                Object optionalInner = optionalValue.orElse(null);
                if (optionalInner instanceof Boolean boolValue) {
                    return boolValue;
                }
            }
        } catch (ReflectiveOperationException ignored) {
            // Keep fallback below for versions with different NBT getters.
        }

        return fallback;
    }
}

