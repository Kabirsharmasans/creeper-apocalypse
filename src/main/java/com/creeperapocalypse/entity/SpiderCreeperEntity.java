package com.creeperapocalypse.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import net.minecraft.entity.ai.pathing.SpiderNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;

/**
 * Spider Creeper - A creeper that can climb walls like a spider
 * Visual: Purple "Spider" name tag
 */
public class SpiderCreeperEntity extends CreeperEntity {
    
    private boolean isClimbingWall = false;
    
    public SpiderCreeperEntity(EntityType<? extends CreeperEntity> entityType, World world) {
        super(entityType, world);
        this.navigation = new SpiderNavigation(this, world);
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        return new SpiderNavigation(this, world);
    }
    
    public static DefaultAttributeContainer.Builder createSpiderCreeperAttributes() {
        return HostileEntity.createHostileAttributes()
            .add(EntityAttributes.MAX_HEALTH, 16.0)
            .add(EntityAttributes.MOVEMENT_SPEED, 0.30)
            .add(EntityAttributes.FOLLOW_RANGE, 40.0);
    }
    
    @Override
    public void tick() {
        super.tick();
        this.isClimbingWall = this.horizontalCollision;
        this.setNoGravity(this.isClimbingWall);

        if (this.isClimbingWall && this.getTarget() != null) {
            if (this.getTarget().getY() > this.getY() + 0.5) {
                Vec3d vel = this.getVelocity();
                this.setVelocity(vel.x, 0.25, vel.z);
            }
        }
    }
    
    @Override
    public boolean isClimbing() {
        return this.isClimbingWall;
    }
    
    @Override
    public void move(MovementType movementType, Vec3d movement) {
        super.move(movementType, movement);
        if (isClimbingWall && getTarget() != null) {
            if (getTarget().getY() > this.getY()) {
                this.setVelocity(this.getVelocity().add(0, 0.2, 0));
            }
        }
    }
    
    public String getVariantName() {
        return "spider";
    }
    
    public int getExplosionRadius() {
        return 3;
    }
}
