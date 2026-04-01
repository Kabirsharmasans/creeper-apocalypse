package com.creeperapocalypse.mixin.client;

import com.creeperapocalypse.CreeperApocalypse;
import com.creeperapocalypse.client.gui.ChallengeSettingsScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to add Creeper Challenge Settings button to the pause menu
 */
@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {

    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    /**
     * Adds the Creeper Challenge Settings button to the pause menu
     */
    @Inject(method = "init", at = @At("TAIL"))
    private void addChallengeButton(CallbackInfo ci) {
        // Add button below existing options
        int buttonWidth = 200;
        int buttonHeight = 20;
        int x = this.width / 2 - buttonWidth / 2;
        int y = this.height / 4 + 120 + 24; // Position below other buttons

        // Create the challenge settings button
        ButtonWidget challengeButton = ButtonWidget.builder(
            Text.literal("§c[!] §eCreeper Challenge Settings §c[!]"),
            button -> {
                if (this.client != null) {
                    this.client.setScreen(new ChallengeSettingsScreen(this));
                }
            }
        ).dimensions(x, y, buttonWidth, buttonHeight).build();

        this.addDrawableChild(challengeButton);
    }
}

