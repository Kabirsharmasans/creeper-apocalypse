package com.creeperapocalypse.mixin;

import net.minecraft.entity.SpawnGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpawnGroup.class)
public class SpawnGroupMixin {

    @Inject(method = "getCapacity", at = @At("HEAD"), cancellable = true)
    private void overrideCapacity(CallbackInfoReturnable<Integer> cir) {
        // Boost capacity for MONSTER and CREATURE groups to allow the apocalypse
        // "this" is the enum instance
        SpawnGroup group = (SpawnGroup) (Object) this;
        
        if (group == SpawnGroup.MONSTER) {
            cir.setReturnValue(500); // Increased cap (default ~70)
        } else if (group == SpawnGroup.CREATURE) {
            cir.setReturnValue(150); // Increased cap (default ~10)
        }
    }
}
