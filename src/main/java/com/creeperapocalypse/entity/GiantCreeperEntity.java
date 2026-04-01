package com.creeperapocalypse.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;

/**
 * Giant Creeper - A massive creeper with huge explosion but slower movement
 * Visual: HUGE size + red "GIANT" name tag
 */
public class GiantCreeperEntity extends CreeperEntity {

    private static final int GIANT_EXPLOSION_RADIUS = 12;
    private boolean forcedIgnite = false;

    public GiantCreeperEntity(EntityType<? extends CreeperEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createGiantCreeperAttributes() {
        return HostileEntity.createHostileAttributes()
            .add(EntityAttributes.GENERIC_MAX_HEALTH, 48.0)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.065)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 80.0)
            .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.5);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.getEntityWorld().isClient()) {
            if (this.getTarget() != null && this.squaredDistanceTo(this.getTarget()) <= 8.0 * 8.0) {
                forcedIgnite = true;
                this.ignite();
            }

            if (this.getFuseSpeed() > 0 || this.isIgnited()) {
                forcedIgnite = true;
            }
            if (forcedIgnite) {
                this.ignite();
                this.setFuseSpeed(1);
            }

            if ((Object) this instanceof com.creeperapocalypse.mixin.CreeperEntityAccessor accessor) {
                if (accessor.getFuseTime() < 90) {
                    accessor.setFuseTime(90);
                }
            }
        }
    }

    public String getVariantName() {
        return "giant";
    }

    public int getExplosionRadius() {
        return GIANT_EXPLOSION_RADIUS;
    }
}

