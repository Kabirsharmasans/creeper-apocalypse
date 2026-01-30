package com.creeperapocalypse.client;

import com.creeperapocalypse.CreeperApocalypse;
import com.creeperapocalypse.client.gui.ChallengeSettingsScreen;
import com.creeperapocalypse.client.gui.StatsOverlayRenderer;
import com.creeperapocalypse.client.render.CreeperVariantRenderer;
import com.creeperapocalypse.entity.ModEntities;
import com.creeperapocalypse.network.ModNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

/**
 * Client-side initialization for Creeper Apocalypse Escalation
 */
@Environment(EnvType.CLIENT)
public class CreeperApocalypseClient implements ClientModInitializer {
    
    private static KeyBinding openSettingsKey;
    private static KeyBinding toggleStatsKey;
    private static StatsOverlayRenderer statsRenderer;
    private static boolean statsOverlayVisible = false;
    
    // Create custom key binding category for this mod
    private static final KeyBinding.Category MOD_CATEGORY = KeyBinding.Category.create(
        Identifier.of(CreeperApocalypse.MOD_ID, "keybindings")
    );
    
    @Override
    public void onInitializeClient() {
        CreeperApocalypse.LOGGER.info("Initializing client-side features...");
        
        registerEntityRenderers();
        registerKeyBindings();
        
        ModNetworking.registerS2CPackets();
        
        statsRenderer = new StatsOverlayRenderer();
        
        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            if (statsOverlayVisible && CreeperApocalypse.CONFIG.isEnabled()) {
                statsRenderer.render(drawContext, tickCounter.getTickProgress(true));
            }
        });
        
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openSettingsKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new ChallengeSettingsScreen(null));
                }
            }
            
            while (toggleStatsKey.wasPressed()) {
                statsOverlayVisible = !statsOverlayVisible;
            }
        });
        
        CreeperApocalypse.LOGGER.info("Client-side initialization complete!");
    }

    
    
    private void registerEntityRenderers() {
        // Original variants
        EntityRendererRegistry.register(ModEntities.MINI_CREEPER, CreeperVariantRenderer.MiniCreeperRenderer::new);
        EntityRendererRegistry.register(ModEntities.GIANT_CREEPER, CreeperVariantRenderer.GiantCreeperRenderer::new);
        EntityRendererRegistry.register(ModEntities.SPIDER_CREEPER, CreeperVariantRenderer.SpiderCreeperRenderer::new);
        
        // New fun variants (use standard creeper renderer - they have name tags for identification)
        EntityRendererRegistry.register(ModEntities.NINJA_CREEPER, CreeperVariantRenderer.NinjaCreeperRenderer::new);
        EntityRendererRegistry.register(ModEntities.RAINBOW_CREEPER, CreeperVariantRenderer.RainbowCreeperRenderer::new);
        EntityRendererRegistry.register(ModEntities.BOUNCY_CREEPER, CreeperVariantRenderer.BouncyCreeperRenderer::new);
        EntityRendererRegistry.register(ModEntities.JOCKEY_CREEPER, CreeperVariantRenderer.JockeyCreeperRenderer::new);
        EntityRendererRegistry.register(ModEntities.HAPPY_CREEPER, CreeperVariantRenderer.HappyCreeperRenderer::new);
        EntityRendererRegistry.register(ModEntities.LIGHTNING_CREEPER, CreeperVariantRenderer.LightningCreeperRenderer::new);
        
        CreeperApocalypse.LOGGER.info("All 9 creeper variant renderers registered!");
    }
    
    private void registerKeyBindings() {
        openSettingsKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                "key.creeper-apocalypse.open_settings",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                MOD_CATEGORY
            )
        );
        
        toggleStatsKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                "key.creeper-apocalypse.toggle_stats",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                MOD_CATEGORY
            )
        );
        
        CreeperApocalypse.LOGGER.info("Key bindings registered");
    }

    

    public static void toggleStatsOverlay() {
        statsOverlayVisible = !statsOverlayVisible;
    }
    
    public static boolean isStatsOverlayVisible() {
        return statsOverlayVisible;
    }
}
