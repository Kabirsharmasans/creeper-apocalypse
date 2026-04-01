package com.creeperapocalypse.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

/**
 * Happy Creeper - Pink, heals players instead of hurting them.
 */
public class HappyCreeperEntity extends CreeperEntity {

    // Workaround for missing getWorld() in some mappings
    private final World entityWorld;

    public HappyCreeperEntity(EntityType<? extends CreeperEntity> entityType, World world) {
        super(entityType, world);
        this.entityWorld = world;
    }

    public static DefaultAttributeContainer.Builder createHappyCreeperAttributes() {
        return HostileEntity.createHostileAttributes()
            .add(EntityAttributes.MAX_HEALTH, 20.0)
            .add(EntityAttributes.MOVEMENT_SPEED, 0.25);
    }

    // Custom logic called by Mixin
    public void doHappyExplosion() {
        if (!this.entityWorld.isClient()) {
            this.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE.value(), 1.0f, 0.8f);

            boolean charged = this.isCharged();

            // Spawn hearts (more if charged) and optional lightning
            if (this.entityWorld instanceof net.minecraft.server.world.ServerWorld serverWorld) {
                int particleCount = charged ? 100 : 20; // Tuned Boost: more hearts when charged
                serverWorld.spawnParticles(net.minecraft.particle.ParticleTypes.HEART,
                    this.getX(), this.getY() + 1.0, this.getZ(),
                    particleCount, 1.0, 1.0, 1.0, 0.5);

                if (charged) {
                    // Summon a lightning bolt at the creeper's location
                    var bolt = net.minecraft.entity.EntityType.LIGHTNING_BOLT.create(serverWorld, net.minecraft.entity.SpawnReason.TRIGGERED);
                    if (bolt != null) {
                        bolt.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), 0.0f);
                        serverWorld.spawnEntity(bolt);
                    }
                    serverWorld.playSound(null, this.getBlockPos(), net.minecraft.sound.SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, net.minecraft.sound.SoundCategory.WEATHER, 1.0f, 1.0f);
                }
            }

            // Heal nearby living entities; charged happy creeper gives stronger heals + Absorption V
            // NEW LOGIC: Damage other creepers, Heal/Buff non-creepers

            // Buffed radius by 75% (6.0 -> 10.5, 8.0 -> 14.0)
            double radius = charged ? 14.0 : 10.5;

            this.getEntityWorld().getEntitiesByClass(net.minecraft.entity.LivingEntity.class,
                this.getBoundingBox().expand(radius), entity -> true)
                .forEach(entity -> {
                    // Skip self
                    if (entity == this) return;

                    if (entity instanceof CreeperEntity && !(entity instanceof HappyCreeperEntity)) {
                        // DAMAGE hostile creepers
                        // Massive damage to ensure they die or get hurt badly
                        if (this.getEntityWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {
                             entity.damage(serverWorld, serverWorld.getDamageSources().magic(), charged ? 50.0f : 20.0f);
                        }
                    } else if (!(entity instanceof CreeperEntity)) {
                         // BUFF non-creepers (Players, passive mobs, etc)
                         // Buffed duration and amplifier by ~75%
                         // Regen III (was II)
                         entity.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, charged ? 2100 : 1050, 2));
                         // Instant Health III (was II)
                         entity.addStatusEffect(new StatusEffectInstance(StatusEffects.INSTANT_HEALTH, 1, 2));

                         if (charged) {
                             // Absorption X (was V) - Massive shield
                             entity.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 3600, 9));
                             // Resistance II for good measure
                             entity.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 2100, 1));
                         }
                    }
                });

            this.discard();
        }
    }
}

