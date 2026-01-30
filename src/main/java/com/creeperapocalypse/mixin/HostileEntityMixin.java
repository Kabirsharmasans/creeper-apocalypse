package com.creeperapocalypse.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HostileEntity.class)
public class HostileEntityMixin {

    // Overwrite the darkness check for all hostile entities? 
    // Or just make sure if it's a creeper we return true.
    
    // Static method canSpawn ignores light if we trick it?
    // Actually, SpawnInit's SpawnRestriction override SHOULD have replaced strict monster monitoring
    // with our custom predicate.
    
    // However, maybe HostileEntity.createHostileAttributes isn't the issue.
    // The issue is HostileEntity.canSpawnInDark
    
    @Inject(method = "isSpawnDark", at = @At("HEAD"), cancellable = true)
    private static void onIsSpawnDark(ServerWorldAccess world, BlockPos pos, Random random, CallbackInfoReturnable<Boolean> cir) {
        // We want to force this to true for Creepers, or generally if our mod is active.
        // But this is a static utility method used by HostileEntity.
        
        // If we force this to true, then mobs can spawn anywhere.
        // Since we replaced everything with creepers, let's force it true!
        cir.setReturnValue(true);
    }
}
