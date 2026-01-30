package com.creeperapocalypse.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnRestriction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(SpawnRestriction.class)
public interface SpawnRestrictionAccessor {
    @Accessor("RESTRICTIONS")
    static Map<EntityType<?>, ?> getRestrictions() {
        throw new UnsupportedOperationException();
    }

    @Accessor("RESTRICTIONS")
    static void setRestrictions(Map<EntityType<?>, ?> restrictions) {
        throw new UnsupportedOperationException();
    }
}
