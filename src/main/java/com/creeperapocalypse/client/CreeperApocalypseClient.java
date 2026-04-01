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
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Client-side initialization for Creeper Apocalypse Escalation
 */
@Environment(EnvType.CLIENT)
public class CreeperApocalypseClient implements ClientModInitializer {

    private static KeyBinding openSettingsKey;
    private static KeyBinding toggleStatsKey;
    private static StatsOverlayRenderer statsRenderer;
    private static boolean statsOverlayVisible = false;

    private static final String MOD_CATEGORY = "key.categories.creeper-apocalypse";

    @Override
    public void onInitializeClient() {
        CreeperApocalypse.LOGGER.info("Initializing client-side features...");

        registerEntityRenderers();
        registerKeyBindings();

        ModNetworking.registerS2CPackets();

        statsRenderer = new StatsOverlayRenderer();

        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            if (statsOverlayVisible && CreeperApocalypse.CONFIG.isEnabled()) {
                statsRenderer.render(drawContext, 0.0f);
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
        openSettingsKey = KeyBindingHelper.registerKeyBinding(createKeyBinding(
            "key.creeper-apocalypse.open_settings",
            GLFW.GLFW_KEY_K
        ));

        toggleStatsKey = KeyBindingHelper.registerKeyBinding(createKeyBinding(
            "key.creeper-apocalypse.toggle_stats",
            GLFW.GLFW_KEY_J
        ));

        CreeperApocalypse.LOGGER.info("Key bindings registered");
    }

    private KeyBinding createKeyBinding(String translationKey, int keyCode) {
        try {
            for (Constructor<?> constructor : KeyBinding.class.getConstructors()) {
                Class<?>[] parameterTypes = constructor.getParameterTypes();
                if (parameterTypes.length != 4) {
                    continue;
                }

                if (parameterTypes[0] != String.class || parameterTypes[1] != InputUtil.Type.class || parameterTypes[2] != int.class) {
                    continue;
                }

                Object categoryArgument = resolveCategoryArgument(parameterTypes[3]);
                if (categoryArgument != null) {
                    return (KeyBinding) constructor.newInstance(translationKey, InputUtil.Type.KEYSYM, keyCode, categoryArgument);
                }
            }
        } catch (ReflectiveOperationException exception) {
            CreeperApocalypse.LOGGER.error("Failed to register key binding {}", translationKey, exception);
        }

        throw new IllegalStateException("No compatible KeyBinding constructor available for current Minecraft version");
    }

    private Object resolveCategoryArgument(Class<?> categoryType) {
        if (categoryType == String.class) {
            return MOD_CATEGORY;
        }

        Object createdCategory = tryCreateCategoryWithFactory(categoryType);
        if (createdCategory != null) {
            return createdCategory;
        }

        Object staticCategory = tryGetStaticCategory(categoryType);
        if (staticCategory != null) {
            return staticCategory;
        }

        return null;
    }

    private Object tryCreateCategoryWithFactory(Class<?> categoryType) {
        try {
            for (Method method : categoryType.getDeclaredMethods()) {
                if (!Modifier.isStatic(method.getModifiers()) || method.getReturnType() != categoryType) {
                    continue;
                }

                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length == 1 && parameterTypes[0] == String.class) {
                    method.setAccessible(true);
                    return method.invoke(null, MOD_CATEGORY);
                }
            }
        } catch (ReflectiveOperationException ignored) {
            // Try static field fallback below.
        }

        return null;
    }

    private Object tryGetStaticCategory(Class<?> categoryType) {
        try {
            for (Field field : categoryType.getFields()) {
                if (Modifier.isStatic(field.getModifiers()) && field.getType() == categoryType) {
                    return field.get(null);
                }
            }
        } catch (ReflectiveOperationException ignored) {
            // No compatible static category available.
        }

        return null;
    }

    public static void toggleStatsOverlay() {
        statsOverlayVisible = !statsOverlayVisible;
    }

    public static boolean isStatsOverlayVisible() {
        return statsOverlayVisible;
    }
}

