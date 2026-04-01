package com.creeperapocalypse.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;

/**
 * Mini Creeper - A smaller, faster creeper with reduced explosion damage
 * Visual: Tiny size + green "Mini" name tag
 */
public class MiniCreeperEntity extends CreeperEntity {

    private static final int MINI_EXPLOSION_RADIUS = 2;

    public MiniCreeperEntity(EntityType<? extends CreeperEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createMiniCreeperAttributes() {
        return HostileEntity.createHostileAttributes()
            .add(EntityAttributes.MAX_HEALTH, 20.0)
            .add(EntityAttributes.MOVEMENT_SPEED, 0.375)
            .add(EntityAttributes.FOLLOW_RANGE, 40.0);
    }

    public String getVariantName() {
        return "mini";
    }

    public int getExplosionRadius() {
        return MINI_EXPLOSION_RADIUS;
    }
}

