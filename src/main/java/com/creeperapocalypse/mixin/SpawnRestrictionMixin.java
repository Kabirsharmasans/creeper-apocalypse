package com.creeperapocalypse.mixin;

import com.creeperapocalypse.CreeperApocalypse;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.server.world.ServerWorld; // Needed for ServerWorld not ServerWorldAccess in newer versions sometimes? No, canSpawn takes ServerWorldAccess usually.
// Wait, allow match to find correct signature.
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpawnRestriction.class)
public class SpawnRestrictionMixin {

    @Inject(
        method = "canSpawn",
        at = @At("HEAD"),
        cancellable = true
    )
    private static <T extends net.minecraft.entity.Entity> void forceCreeperSpawn(
            EntityType<T> type, 
            ServerWorldAccess world, 
            SpawnReason spawnReason, 
            BlockPos pos, 
            Random random, 
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (type == EntityType.CREEPER) {
            // "Nuclear Option": If it's a Creeper, we say YES.
            // We still want basic checks (block is solid), so we duplicate the permissive check here.
            
            // 1. Must implement basic ground check so they don't spawn in mid-air or inside walls
            boolean solidBelow = world.getBlockState(pos.down()).isSolidBlock(world, pos.down());
            boolean clearSpace = world.getBlockState(pos).getCollisionShape(world, pos).isEmpty();
            
            if (solidBelow && clearSpace) {
                 cir.setReturnValue(true);
            }
        }
    }
}
