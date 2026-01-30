package com.creeperapocalypse.client.render;

import com.creeperapocalypse.CreeperApocalypse;
import com.creeperapocalypse.entity.GiantCreeperEntity;
import com.creeperapocalypse.entity.MiniCreeperEntity;
import com.creeperapocalypse.entity.SpiderCreeperEntity;
import com.creeperapocalypse.entity.NinjaCreeperEntity;
import com.creeperapocalypse.entity.RainbowCreeperEntity;
import com.creeperapocalypse.entity.BouncyCreeperEntity;
import com.creeperapocalypse.entity.JockeyCreeperEntity;
import com.creeperapocalypse.entity.LightningCreeperEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.CreeperEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.CreeperChargeFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.CreeperEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.entity.state.CreeperEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.util.Identifier;

/**
 * Custom renderers for creeper variants
 * Each variant has unique visual characteristics via custom textures
 */
public class CreeperVariantRenderer {
    
    // Thread-local flag to track when rendering a super charged Lightning Creeper
    // Used by CreeperChargeFeatureRendererMixin to swap the aura texture to purple
    public static final ThreadLocal<Boolean> RENDERING_SUPER_CHARGED = ThreadLocal.withInitial(() -> false);
    
    // Custom textures for each variant
    private static final Identifier MINI_CREEPER_TEXTURE = Identifier.of(CreeperApocalypse.MOD_ID, "textures/entity/mini_creeper.png");
    private static final Identifier GIANT_CREEPER_TEXTURE = Identifier.of(CreeperApocalypse.MOD_ID, "textures/entity/giant_creeper.png");
    private static final Identifier SPIDER_CREEPER_TEXTURE = Identifier.of(CreeperApocalypse.MOD_ID, "textures/entity/spider_creeper.png");
    private static final Identifier NINJA_CREEPER_TEXTURE = Identifier.of(CreeperApocalypse.MOD_ID, "textures/entity/ninja_creeper.png");
    private static final Identifier RAINBOW_CREEPER_TEXTURE = Identifier.of(CreeperApocalypse.MOD_ID, "textures/entity/rainbow_creeper.png");
    private static final Identifier BOUNCY_CREEPER_TEXTURE = Identifier.of(CreeperApocalypse.MOD_ID, "textures/entity/bouncy_creeper.png");
    private static final Identifier JOCKEY_CREEPER_TEXTURE = Identifier.of(CreeperApocalypse.MOD_ID, "textures/entity/jockey_creeper.png");
    private static final Identifier HAPPY_CREEPER_TEXTURE = Identifier.of(CreeperApocalypse.MOD_ID, "textures/entity/happy_creeper.png");
    private static final Identifier LIGHTNING_CREEPER_TEXTURE = Identifier.of(CreeperApocalypse.MOD_ID, "textures/entity/lightning_creeper.png");
    
    /**
     * Renderer for Mini Creepers - 50% scale
     */
    public static class MiniCreeperRenderer extends CreeperEntityRenderer {
        
        public MiniCreeperRenderer(EntityRendererFactory.Context context) {
            super(context);
            this.shadowRadius = 0.25f;
        }
        
        @Override
        public Identifier getTexture(CreeperEntityRenderState state) {
            return MINI_CREEPER_TEXTURE;
        }
        
        @Override
        protected void scale(CreeperEntityRenderState state, MatrixStack matrices) {
            matrices.scale(0.5f, 0.5f, 0.5f);
            super.scale(state, matrices);
        }
    }
    
    /**
     * Renderer for Giant Creepers - 200% scale
     */
    public static class GiantCreeperRenderer extends CreeperEntityRenderer {
        
        public GiantCreeperRenderer(EntityRendererFactory.Context context) {
            super(context);
            this.shadowRadius = 1.0f;
        }
        
        @Override
        public Identifier getTexture(CreeperEntityRenderState state) {
            return GIANT_CREEPER_TEXTURE;
        }
        
        @Override
        protected void scale(CreeperEntityRenderState state, MatrixStack matrices) {
            matrices.scale(2.0f, 2.0f, 2.0f);
            super.scale(state, matrices);
        }
    }
    
    /**
     * Renderer for Spider Creepers - standard size, can climb
     */
    public static class SpiderCreeperRenderer extends CreeperEntityRenderer {
        
        public SpiderCreeperRenderer(EntityRendererFactory.Context context) {
            super(context);
            this.shadowRadius = 0.5f;
        }
        
        @Override
        public Identifier getTexture(CreeperEntityRenderState state) {
            return SPIDER_CREEPER_TEXTURE;
        }
    }
    
    /**
     * Renderer for Ninja Creepers - slightly smaller, stealthy
     */
    public static class NinjaCreeperRenderer extends CreeperEntityRenderer {
        
        public NinjaCreeperRenderer(EntityRendererFactory.Context context) {
            super(context);
            this.shadowRadius = 0.4f;
        }
        
        @Override
        public Identifier getTexture(CreeperEntityRenderState state) {
            return NINJA_CREEPER_TEXTURE;
        }
        
        @Override
        protected void scale(CreeperEntityRenderState state, MatrixStack matrices) {
            matrices.scale(0.9f, 0.9f, 0.9f);
            super.scale(state, matrices);
        }
    }
    
    /**
     * Renderer for Rainbow Creepers - normal size, fabulous
     */
    public static class RainbowCreeperRenderer extends CreeperEntityRenderer {
        
        public RainbowCreeperRenderer(EntityRendererFactory.Context context) {
            super(context);
            this.shadowRadius = 0.5f;
        }
        
        @Override
        public Identifier getTexture(CreeperEntityRenderState state) {
            return RAINBOW_CREEPER_TEXTURE;
        }
    }
    
    /**
     * Renderer for Bouncy Creepers - slightly squished look
     */
    public static class BouncyCreeperRenderer extends CreeperEntityRenderer {
        
        public BouncyCreeperRenderer(EntityRendererFactory.Context context) {
            super(context);
            this.shadowRadius = 0.5f;
        }
        
        @Override
        public Identifier getTexture(CreeperEntityRenderState state) {
            return BOUNCY_CREEPER_TEXTURE;
        }
        
        @Override
        protected void scale(CreeperEntityRenderState state, MatrixStack matrices) {
            // Slightly wider and shorter - bouncy!
            matrices.scale(1.1f, 0.9f, 1.1f);
            super.scale(state, matrices);
        }
    }
    
    /**
     * Renderer for Jockey Creepers - the mount creeper
     */
    public static class JockeyCreeperRenderer extends CreeperEntityRenderer {
        
        public JockeyCreeperRenderer(EntityRendererFactory.Context context) {
            super(context);
            this.shadowRadius = 0.6f;
        }
        
        @Override
        public Identifier getTexture(CreeperEntityRenderState state) {
            return JOCKEY_CREEPER_TEXTURE;
        }
        
        @Override
        protected void scale(CreeperEntityRenderState state, MatrixStack matrices) {
            // Slightly larger to carry a rider
            matrices.scale(1.1f, 1.1f, 1.1f);
            super.scale(state, matrices);
        }
    }

    /**
     * Renderer for Happy Creepers - Pink and friendly-looking
     */
    public static class HappyCreeperRenderer extends CreeperEntityRenderer {
        
        public HappyCreeperRenderer(EntityRendererFactory.Context context) {
            super(context);
            this.shadowRadius = 0.5f;
        }
        
        @Override
        public Identifier getTexture(CreeperEntityRenderState state) {
            return HAPPY_CREEPER_TEXTURE;
        }
        
        @Override
        protected void scale(CreeperEntityRenderState state, MatrixStack matrices) {
            matrices.scale(0.9f, 0.9f, 0.9f); // Slightly smaller, cuter
            super.scale(state, matrices);
        }
    }

    /**
     * Renderer for Lightning Creepers - Boss size
     */
    public static class LightningCreeperRenderer extends CreeperEntityRenderer {
        
        public LightningCreeperRenderer(EntityRendererFactory.Context context) {
            super(context);
            this.shadowRadius = 0.8f;
        }
        
        @Override
        public Identifier getTexture(CreeperEntityRenderState state) {
            return LIGHTNING_CREEPER_TEXTURE;
        }
        
        @Override
        protected void scale(CreeperEntityRenderState state, MatrixStack matrices) {
            // Set thread-local flag for super charged state (used by mixin to swap aura texture)
            if (state instanceof LightningCreeperRenderState lightningState) {
                RENDERING_SUPER_CHARGED.set(lightningState.superCharged);
            }
            matrices.scale(1.4f, 1.4f, 1.4f); // Boss size
            super.scale(state, matrices);
        }

        @Override
        public CreeperEntityRenderState createRenderState() {
             return new LightningCreeperRenderState();
        }

        @Override
        public void updateRenderState(CreeperEntity entity, CreeperEntityRenderState state, float tickDelta) {
            super.updateRenderState(entity, state, tickDelta);
            if (entity instanceof LightningCreeperEntity lightningCreeper && state instanceof LightningCreeperRenderState lightningState) {
                lightningState.superCharged = lightningCreeper.isSuperCharged();
                // Keep vanilla charged state TRUE for super charged - this renders the aura!
                // The thread-local RENDERING_SUPER_CHARGED flag is set in scale() and read by mixin
                // to swap the aura texture from blue to purple
            }
        }
    }

    // Custom Render State to hold super charged status
    public static class LightningCreeperRenderState extends CreeperEntityRenderState {
        public boolean superCharged = false;
    }

}
