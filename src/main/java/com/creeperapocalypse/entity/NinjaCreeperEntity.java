package com.creeperapocalypse.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

/**
 * Ninja Creeper - Invisible until close to player, then surprise attack!
 * Visual: Invisible with name hidden until revealed
 */
public class NinjaCreeperEntity extends CreeperEntity {
    
    private boolean revealed = false;
    
    public NinjaCreeperEntity(EntityType<? extends CreeperEntity> entityType, World world) {
        super(entityType, world);
    }
    
    public static DefaultAttributeContainer.Builder createNinjaCreeperAttributes() {
        return HostileEntity.createHostileAttributes()
            .add(EntityAttributes.MAX_HEALTH, 15.0)
            .add(EntityAttributes.MOVEMENT_SPEED, 0.35)
            .add(EntityAttributes.FOLLOW_RANGE, 30.0);
    }
    
    @Override
    public void tick() {
        super.tick();
        PlayerEntity nearest = findNearestPlayer();
        if (nearest == null) {
            if (revealed) {
                hide();
            }
            ensureInvisible();
        } else {
            if (!revealed) {
                reveal();
            }
        }
    }
    
    private PlayerEntity findNearestPlayer() {
        return (PlayerEntity) this.getEntityWorld().getClosestPlayer(this.getX(), this.getY(), this.getZ(), 6.0, false);
    }
    
    private void reveal() {
        if (!revealed) {
            revealed = true;
            removeStatusEffect(StatusEffects.INVISIBILITY);
            setCustomNameVisible(true);
            playSound(net.minecraft.sound.SoundEvents.ENTITY_CREEPER_PRIMED, 1.5f, 1.5f);
            spawnSmoke();
        }
    }

    private void hide() {
        if (revealed) {
            revealed = false;
            setCustomNameVisible(false);
            spawnSmoke();
        }
    }

    private void ensureInvisible() {
        if (!hasStatusEffect(StatusEffects.INVISIBILITY)) {
            addStatusEffect(new StatusEffectInstance(
                StatusEffects.INVISIBILITY,
                40, 0, false, false, false
            ));
        }
    }

    private void spawnSmoke() {
        if (this.getEntityWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.SMOKE,
                this.getX(), this.getY() + 0.5, this.getZ(),
                10, 0.2, 0.2, 0.2, 0.01);
        }
    }
    
    @Override
    public void setTarget(net.minecraft.entity.LivingEntity target) {
        super.setTarget(target);
        if (target != null) {
            reveal();
        }
    }
    
    public String getVariantName() {
        return "ninja";
    }
}
