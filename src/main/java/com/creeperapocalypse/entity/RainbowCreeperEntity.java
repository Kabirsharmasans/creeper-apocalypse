package com.creeperapocalypse.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.particle.ParticleTypes;
import java.util.List;
import java.util.Random;

/**
 * Rainbow Creeper - Colorful and fabulous! Glows with rainbow texture.
 * Visual: Has glowing effect and rainbow-colored texture
 */
public class RainbowCreeperEntity extends CreeperEntity {

    private final World entityWorld;

    public RainbowCreeperEntity(EntityType<? extends CreeperEntity> entityType, World world) {
        super(entityType, world);
        this.entityWorld = world;
    }

    public static DefaultAttributeContainer.Builder createRainbowCreeperAttributes() {
        return HostileEntity.createHostileAttributes()
            .add(EntityAttributes.MAX_HEALTH, 30.0)
            .add(EntityAttributes.MOVEMENT_SPEED, 0.33)
            .add(EntityAttributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    public void tick() {
        super.tick();

        if (age % 40 == 0) {
            if (!hasStatusEffect(StatusEffects.GLOWING)) {
                addStatusEffect(new StatusEffectInstance(
                    StatusEffects.GLOWING,
                    60, 0, false, false, true
                ));
            }
        }
    }

    public void doRainbowExplosion() {
        if (!this.entityWorld.isClient()) {
            // Chaos explosion!
            this.entityWorld.createExplosion(this, this.getX(), this.getY(), this.getZ(), 4.0f, World.ExplosionSourceType.MOB);

            if (this.entityWorld instanceof ServerWorld serverWorld) {
                // Particles
                serverWorld.spawnParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 1, this.getZ(), 50, 2.0, 2.0, 2.0, 0.1);

                // Random effects on nearby entities
                Box box = this.getBoundingBox().expand(8.0);
                List<LivingEntity> nearby = this.entityWorld.getEntitiesByClass(LivingEntity.class, box, e -> e != this);
                Random rand = new Random();

                for (LivingEntity target : nearby) {
                    // Apply random chaos effects (Shortened duration)
                    target.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 100, 0));
                    target.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 100, 0));
                    target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 200, 0));
                    target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 1));

                    // Launch into air
                    target.addVelocity(0, 0.8, 0);
                }
            }
            this.discard();
        }
    }

    public String getVariantName() {
        return "rainbow";
    }
}

