package com.creeperapocalypse.mixin.client;

import com.creeperapocalypse.CreeperApocalypse;
import com.creeperapocalypse.data.PlayerStats;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.systems.RenderSystem;

/**
 * Mixin to add challenge info overlay to the HUD
 */
@Mixin(InGameHud.class)
public class InGameHudMixin {
    
    @Shadow @Final
    private MinecraftClient client;
    
    /**
     * Renders challenge information on the HUD
     */
    @Inject(method = "render", at = @At("TAIL"))
    private void renderChallengeOverlay(DrawContext context, net.minecraft.client.render.RenderTickCounter tickCounter, CallbackInfo ci) {
        // Overlay removed as requested
    }
    
    /**
     * Renders a small overlay with current day and multiplier
     * Improved visibility for all monitor sizes
     */
    private void renderMiniOverlay(DrawContext context) {
        // Overlay removed
    }
}
