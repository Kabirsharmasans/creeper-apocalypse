package com.creeperapocalypse.mixin.client;

import com.creeperapocalypse.CreeperApocalypse;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.ScreenshotRecorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for MinecraftClient to handle client-side utilities like screenshots
 */
@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    
    // Tracks screenshot delay counter
    @Unique
    private int creeperApocalypse$screenshotDelay = 0;
    
    /**
     * Tick handler for client-side features
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (!CreeperApocalypse.CONFIG.isEnabled()) {
            return;
        }
        
        MinecraftClient client = (MinecraftClient) (Object) this;

        // Handle pending death screenshot using flag from main mod
        if (CreeperApocalypse.pendingDeathScreenshot && CreeperApocalypse.CONFIG.autoScreenshotOnDeath()) {
            if (creeperApocalypse$screenshotDelay < 20) {
                creeperApocalypse$screenshotDelay++;
            } else {
                // Take screenshot using newer API
                try {
                    ScreenshotRecorder.saveScreenshot(
                        client.runDirectory,
                        client.getFramebuffer(),
                        message -> CreeperApocalypse.LOGGER.info("Death screenshot: " + message.getString())
                    );
                } catch (Exception e) {
                    CreeperApocalypse.LOGGER.warn("Failed to take death screenshot: " + e.getMessage());
                }
                CreeperApocalypse.pendingDeathScreenshot = false;
                creeperApocalypse$screenshotDelay = 0;
            }
        } else {
            creeperApocalypse$screenshotDelay = 0;
        }
    }
}
