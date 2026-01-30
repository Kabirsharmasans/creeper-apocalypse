package com.creeperapocalypse.mixin.client;

import com.creeperapocalypse.CreeperApocalypse;
import com.creeperapocalypse.client.render.CreeperVariantRenderer;
import net.minecraft.client.render.entity.feature.CreeperChargeFeatureRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to swap the charged creeper armor texture to purple for Super Charged Lightning Creepers.
 * Works with the ThreadLocal flag set in CreeperVariantRenderer.LightningCreeperRenderer.
 */
@Mixin(CreeperChargeFeatureRenderer.class)
public class CreeperChargeFeatureRendererMixin {
    
    @Unique
    private static final Identifier PURPLE_ARMOR = Identifier.of(CreeperApocalypse.MOD_ID, "textures/entity/creeper_armor_purple.png");
    
    /**
     * Intercept getEnergySwirlTexture to return purple texture for super charged Lightning Creepers.
     * The ThreadLocal RENDERING_SUPER_CHARGED is set by LightningCreeperRenderer.scale() before rendering.
     */
    @Inject(method = "getEnergySwirlTexture", at = @At("HEAD"), cancellable = true)
    private void swapTextureForSuperCharged(CallbackInfoReturnable<Identifier> cir) {
        if (CreeperVariantRenderer.RENDERING_SUPER_CHARGED.get()) {
            cir.setReturnValue(PURPLE_ARMOR);
        }
    }
}
