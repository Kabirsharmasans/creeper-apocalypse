package com.creeperapocalypse.client.gui;

import com.creeperapocalypse.CreeperApocalypse;
import com.creeperapocalypse.client.CreeperApocalypseClient;
import com.creeperapocalypse.network.ModNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget; // Added for text fields
import net.minecraft.text.Text;

/**
 * Settings screen for configuring the Creeper Apocalypse challenge
 */
public class ChallengeSettingsScreen extends Screen {
    
    private final Screen parent;
    
    private boolean enabled;
    private float maxMultiplier;
    private float escalationSpeed;
    private boolean autoScreenshot;
    private boolean keepInventory;
    private boolean milestonesEnabled;
    private boolean specialVariants;
    private float miniCreeperChance;
    private float giantCreeperChance;
    private float spiderCreeperChance;
    private float ninjaCreeperChance;
    private float rainbowCreeperChance;
    private float bouncyCreeperChance;
    private float jockeyCreeperChance;
    private float happyCreeperChance;
    private float lightningCreeperChance;
    
    // NEW OPTION
    private boolean ignoreDayProgression;
    
    // LIMITS
    private int maxCreepersOverworld;
    private int maxCreepersNether;
    private int maxCreepersEnd;
    
    // UI Elements for limiting types
    private TextFieldWidget overworldLimitField;
    private TextFieldWidget netherLimitField;
    private TextFieldWidget endLimitField;
    
    private int guiScale;
    
    public ChallengeSettingsScreen(Screen parent) {
        super(Text.literal("Creeper Apocalypse Settings"));
        this.parent = parent;
        
        // Load current settings
        enabled = CreeperApocalypse.CONFIG.isEnabled();
        maxMultiplier = CreeperApocalypse.CONFIG.getMaxSpawnMultiplier();
        escalationSpeed = CreeperApocalypse.CONFIG.getEscalationSpeed();
        autoScreenshot = CreeperApocalypse.CONFIG.autoScreenshotOnDeath();
        keepInventory = CreeperApocalypse.CONFIG.keepInventoryOnCreeperDeath();
        milestonesEnabled = CreeperApocalypse.CONFIG.milestonesEnabled();
        specialVariants = CreeperApocalypse.CONFIG.specialVariantsEnabled();
        miniCreeperChance = CreeperApocalypse.CONFIG.getMiniCreeperChance();
        giantCreeperChance = CreeperApocalypse.CONFIG.getGiantCreeperChance();
        spiderCreeperChance = CreeperApocalypse.CONFIG.getSpiderCreeperChance();
        ninjaCreeperChance = CreeperApocalypse.CONFIG.getNinjaCreeperChance();
        rainbowCreeperChance = CreeperApocalypse.CONFIG.getRainbowCreeperChance();
        bouncyCreeperChance = CreeperApocalypse.CONFIG.getBouncyCreeperChance();
        jockeyCreeperChance = CreeperApocalypse.CONFIG.getJockeyCreeperChance();
        happyCreeperChance = CreeperApocalypse.CONFIG.getHappyCreeperChance();
        lightningCreeperChance = CreeperApocalypse.CONFIG.getLightningCreeperChance();
        ignoreDayProgression = CreeperApocalypse.CONFIG.ignoreDayProgression();
        
        maxCreepersOverworld = CreeperApocalypse.CONFIG.getMaxCreepersOverworld();
        maxCreepersNether = CreeperApocalypse.CONFIG.getMaxCreepersNether();
        maxCreepersEnd = CreeperApocalypse.CONFIG.getMaxCreepersEnd();
        
        guiScale = net.minecraft.client.MinecraftClient.getInstance().options.getGuiScale().getValue();
    }
    
    @Override
    protected void init() {
        int centerX = width / 2;
        int startY = 45;
        int btnW = 150; // Smaller buttons for 2 columns
        int btnH = 20;
        int spacing = 22;
        int leftX = centerX - btnW - 5;
        int rightX = centerX + 5;
        
        // LEFT COLUMN: General Settings
        int rowL = 0;
        
        // Enable/Disable button
        addDrawableChild(ButtonWidget.builder(
            Text.literal("Challenge: " + (enabled ? "§aENABLED" : "§cDISABLED")),
            button -> {
                enabled = !enabled;
                button.setMessage(Text.literal("Challenge: " + (enabled ? "§aENABLED" : "§cDISABLED")));
            })
            .dimensions(leftX, startY + spacing * rowL++, btnW, btnH)
            .build()
        );
        
        // Max multiplier button
        addDrawableChild(ButtonWidget.builder(
            Text.literal("Max Multiplier: §e" + String.format("%.1fx", maxMultiplier)),
            button -> {
                maxMultiplier = maxMultiplier >= 20.0f ? 2.0f : maxMultiplier + 1.0f;
                button.setMessage(Text.literal("Max Multiplier: §e" + String.format("%.1fx", maxMultiplier)));
            })
            .dimensions(leftX, startY + spacing * rowL++, btnW, btnH)
            .build()
        );
        
        // Escalation speed button
        addDrawableChild(ButtonWidget.builder(
            Text.literal("Escalation: " + getSpeedText()),
            button -> {
                cycleEscalationSpeed();
                button.setMessage(Text.literal("Escalation: " + getSpeedText()));
            })
            .dimensions(leftX, startY + spacing * rowL++, btnW, btnH)
            .build()
        );

    


        // GUI scale
        addDrawableChild(ButtonWidget.builder(
            Text.literal("GUI Scale: §e" + guiScaleText()),
            button -> {
                cycleGuiScale();
                button.setMessage(Text.literal("GUI Scale: §e" + guiScaleText()));
            })
            .dimensions(leftX, startY + spacing * rowL++, btnW, btnH)
            .build()
        );
        
        // Milestones button
        addDrawableChild(ButtonWidget.builder(
            Text.literal("Milestones: " + (milestonesEnabled ? "§aON" : "§7OFF")),
            button -> {
                milestonesEnabled = !milestonesEnabled;
                button.setMessage(Text.literal("Milestones: " + (milestonesEnabled ? "§aON" : "§7OFF")));
            })
            .dimensions(leftX, startY + spacing * rowL++, btnW, btnH)
            .build()
        );
        
        // Auto-screenshot button
        addDrawableChild(ButtonWidget.builder(
            Text.literal("Death Scrshot: " + (autoScreenshot ? "§aON" : "§7OFF")),
            button -> {
                autoScreenshot = !autoScreenshot;
                button.setMessage(Text.literal("Death Scrshot: " + (autoScreenshot ? "§aON" : "§7OFF")));
            })
            .dimensions(leftX, startY + spacing * rowL++, btnW, btnH)
            .build()
        );
        
        // Keep inventory button
        addDrawableChild(ButtonWidget.builder(
            Text.literal("Keep Inv: " + (keepInventory ? "§aON" : "§7OFF")),
            button -> {
                keepInventory = !keepInventory;
                button.setMessage(Text.literal("Keep Inv: " + (keepInventory ? "§aON" : "§7OFF")));
            })
            .dimensions(leftX, startY + spacing * rowL++, btnW, btnH)
            .build()
        );
        
        // NO DAY MODE button
        addDrawableChild(ButtonWidget.builder(
            Text.literal("No Day Mode: " + (ignoreDayProgression ? "§aON" : "§7OFF")),
            button -> {
                ignoreDayProgression = !ignoreDayProgression;
                button.setMessage(Text.literal("No Day Mode: " + (ignoreDayProgression ? "§aON" : "§7OFF")));
            })
            .dimensions(leftX, startY + spacing * rowL++, btnW, btnH)
            .build()
        );
        
        // RESET CHALLENGE button
        addDrawableChild(ButtonWidget.builder(
            Text.literal("§c⚠ RESET CHALLENGE ⚠"),
            button -> {
                ModNetworking.sendResetRequest();
                if (client != null && client.player != null) {
                    client.player.sendMessage(Text.literal("§c§l[CREEPER APOCALYPSE]§r Challenge has been reset to Day 1!"), false);
                }
            })
            .dimensions(leftX, startY + spacing * rowL++, btnW, btnH)
            .build()
        );

        // RIGHT COLUMN: Variant Chances
        int rowR = 0;

        // Special variants toggle
        addDrawableChild(ButtonWidget.builder(
            Text.literal("Variants: " + (specialVariants ? "§aON" : "§7OFF")),
            button -> {
                specialVariants = !specialVariants;
                button.setMessage(Text.literal("Variants: " + (specialVariants ? "§aON" : "§7OFF")));
            })
            .dimensions(rightX, startY + spacing * rowR++, btnW, btnH)
            .build()
        );

        // Variant Chances
        addDrawableChild(ButtonWidget.builder(Text.literal("Mini: §e" + formatPercent(miniCreeperChance)), b -> {
            miniCreeperChance = cycleChance(miniCreeperChance); b.setMessage(Text.literal("Mini: §e" + formatPercent(miniCreeperChance)));
        }).dimensions(rightX, startY + spacing * rowR++, btnW, btnH).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Giant: §e" + formatPercent(giantCreeperChance)), b -> {
            giantCreeperChance = cycleChance(giantCreeperChance); b.setMessage(Text.literal("Giant: §e" + formatPercent(giantCreeperChance)));
        }).dimensions(rightX, startY + spacing * rowR++, btnW, btnH).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Spider: §e" + formatPercent(spiderCreeperChance)), b -> {
            spiderCreeperChance = cycleChance(spiderCreeperChance); b.setMessage(Text.literal("Spider: §e" + formatPercent(spiderCreeperChance)));
        }).dimensions(rightX, startY + spacing * rowR++, btnW, btnH).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Ninja: §e" + formatPercent(ninjaCreeperChance)), b -> {
            ninjaCreeperChance = cycleChance(ninjaCreeperChance); b.setMessage(Text.literal("Ninja: §e" + formatPercent(ninjaCreeperChance)));
        }).dimensions(rightX, startY + spacing * rowR++, btnW, btnH).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Rainbow: §e" + formatPercent(rainbowCreeperChance)), b -> {
            rainbowCreeperChance = cycleChance(rainbowCreeperChance); b.setMessage(Text.literal("Rainbow: §e" + formatPercent(rainbowCreeperChance)));
        }).dimensions(rightX, startY + spacing * rowR++, btnW, btnH).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Bouncy: §e" + formatPercent(bouncyCreeperChance)), b -> {
            bouncyCreeperChance = cycleChance(bouncyCreeperChance); b.setMessage(Text.literal("Bouncy: §e" + formatPercent(bouncyCreeperChance)));
        }).dimensions(rightX, startY + spacing * rowR++, btnW, btnH).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Jockey: §e" + formatPercent(jockeyCreeperChance)), b -> {
            jockeyCreeperChance = cycleChance(jockeyCreeperChance); b.setMessage(Text.literal("Jockey: §e" + formatPercent(jockeyCreeperChance)));
        }).dimensions(rightX, startY + spacing * rowR++, btnW, btnH).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Happy: §e" + formatPercent(happyCreeperChance)), b -> {
            happyCreeperChance = cycleChance(happyCreeperChance); b.setMessage(Text.literal("Happy: §e" + formatPercent(happyCreeperChance)));
        }).dimensions(rightX, startY + spacing * rowR++, btnW, btnH).build());

        // Lightning: Last variant option
        addDrawableChild(ButtonWidget.builder(Text.literal("Lightning: §e" + formatPercent(lightningCreeperChance)), b -> {
            lightningCreeperChance = cycleChance(lightningCreeperChance); b.setMessage(Text.literal("Lightning: §e" + formatPercent(lightningCreeperChance)));
        }).dimensions(rightX, startY + spacing * rowR++, btnW, btnH).build());
        
        // --- SPAWN LIMIT INPUTS (Right Column, after variants) ---
        // Overworld Limit
        addDrawableChild(ButtonWidget.builder(Text.literal("Overworld Limit:"), b -> {})
            .dimensions(rightX, startY + spacing * rowR, btnW / 2 - 2, btnH).build()).active = false;
        
        overworldLimitField = new TextFieldWidget(textRenderer, rightX + btnW / 2 + 2, startY + spacing * rowR++, btnW / 2 - 2, btnH, Text.literal("Overworld Limit"));
        overworldLimitField.setText(String.valueOf(maxCreepersOverworld));
        overworldLimitField.setChangedListener(this::onLimitChanged);
        addDrawableChild(overworldLimitField);
        
        // Nether Limit
        addDrawableChild(ButtonWidget.builder(Text.literal("Nether Limit:"), b -> {})
            .dimensions(rightX, startY + spacing * rowR, btnW / 2 - 2, btnH).build()).active = false;
            
        netherLimitField = new TextFieldWidget(textRenderer, rightX + btnW / 2 + 2, startY + spacing * rowR++, btnW / 2 - 2, btnH, Text.literal("Nether Limit"));
        netherLimitField.setText(String.valueOf(maxCreepersNether));
        netherLimitField.setChangedListener(this::onLimitChanged);
        addDrawableChild(netherLimitField);
        
        // End Limit
        addDrawableChild(ButtonWidget.builder(Text.literal("End Limit:"), b -> {})
            .dimensions(rightX, startY + spacing * rowR, btnW / 2 - 2, btnH).build()).active = false;
            
        endLimitField = new TextFieldWidget(textRenderer, rightX + btnW / 2 + 2, startY + spacing * rowR++, btnW / 2 - 2, btnH, Text.literal("End Limit"));
        endLimitField.setText(String.valueOf(maxCreepersEnd));
        endLimitField.setChangedListener(this::onLimitChanged);
        addDrawableChild(endLimitField);

        // Save button (Centered bottom)
        addDrawableChild(ButtonWidget.builder(
            Text.literal("§aSave & Close"),
            button -> saveAndClose())
            .dimensions(centerX - 100, height - 50, 200, 20)
            .build()
        );
        
        // Cancel button (Centered bottom)
        addDrawableChild(ButtonWidget.builder(
            Text.literal("§cCancel"),
            button -> close())
            .dimensions(centerX - 100, height - 27, 200, 20)
            .build()
        );
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Don't call renderBackground - super.render handles it
        // This fixes "Can only blur once per frame" error
        
        super.render(context, mouseX, mouseY, delta);
        
        context.drawCenteredTextWithShadow(textRenderer, 
            Text.literal("§c☠ §4§lCREEPER APOCALYPSE SETTINGS §c☠"), 
            width / 2, 15, 0xFFFFFF);
        
        // Stats display at bottom
        int statsY = height - 75;
        int currentDay = CreeperApocalypse.CHALLENGE_DATA.getCurrentDay();
        float currentMultiplier = CreeperApocalypse.CHALLENGE_DATA.getSpawnMultiplier();
        long creepersKilled = CreeperApocalypse.CHALLENGE_DATA.getTotalCreepersKilled();
        long creeperDeaths = CreeperApocalypse.CHALLENGE_DATA.getTotalCreeperDeaths();
        
        context.drawCenteredTextWithShadow(textRenderer,
            Text.literal("§7Day: §e" + currentDay + " §7| Multiplier: §6" + 
                String.format("%.1fx", currentMultiplier) + " §7| Killed: §a" + creepersKilled + " §7| Deaths: §c" + creeperDeaths),
            width / 2, statsY, 0xFFFFFF);

        context.drawCenteredTextWithShadow(textRenderer,
            Text.literal("§8Hotkeys: §fK Settings §7| §fJ Stats"),
            width / 2, statsY + 14, 0xCCCCCC);
    }
    
    private String getSpeedText() {
        if (escalationSpeed <= 0.5f) return "§7Slow (0.5x)";
        if (escalationSpeed <= 1.0f) return "§eNormal (1x)";
        return "§cFast (2x)";
    }
    
    private void cycleEscalationSpeed() {
        if (escalationSpeed <= 0.5f) {
            escalationSpeed = 1.0f;
        } else if (escalationSpeed <= 1.0f) {
            escalationSpeed = 2.0f;
        } else {
            escalationSpeed = 0.5f;
        }
    }
    
    private void onLimitChanged(String text) {
        // Just allows typing, validation happens on save
    }

    private void saveAndClose() {
        // Parse and validate limits
        try {
            int ow = Integer.parseInt(overworldLimitField.getText());
            maxCreepersOverworld = Math.max(1, Math.min(1000, ow));
        } catch (NumberFormatException e) {
            // Keep existing valid value
        }
        
        try {
            int ne = Integer.parseInt(netherLimitField.getText());
            maxCreepersNether = Math.max(1, Math.min(1000, ne));
        } catch (NumberFormatException e) { }
        
        try {
            int end = Integer.parseInt(endLimitField.getText());
            maxCreepersEnd = Math.max(1, Math.min(1000, end));
        } catch (NumberFormatException e) { }

        CreeperApocalypse.CONFIG.setEnabled(enabled);
        CreeperApocalypse.CONFIG.setMaxSpawnMultiplier(maxMultiplier);
        CreeperApocalypse.CONFIG.setEscalationSpeed(escalationSpeed);
        CreeperApocalypse.CONFIG.setSpecialVariantsEnabled(specialVariants);
        CreeperApocalypse.CONFIG.setMiniCreeperChance(miniCreeperChance);
        CreeperApocalypse.CONFIG.setGiantCreeperChance(giantCreeperChance);
        CreeperApocalypse.CONFIG.setSpiderCreeperChance(spiderCreeperChance);
        CreeperApocalypse.CONFIG.setNinjaCreeperChance(ninjaCreeperChance);
        CreeperApocalypse.CONFIG.setRainbowCreeperChance(rainbowCreeperChance);
        CreeperApocalypse.CONFIG.setBouncyCreeperChance(bouncyCreeperChance);
        CreeperApocalypse.CONFIG.setJockeyCreeperChance(jockeyCreeperChance);
        CreeperApocalypse.CONFIG.setHappyCreeperChance(happyCreeperChance);
        CreeperApocalypse.CONFIG.setLightningCreeperChance(lightningCreeperChance);
        CreeperApocalypse.CONFIG.setAutoScreenshotOnDeath(autoScreenshot);
        CreeperApocalypse.CONFIG.setKeepInventoryOnDeath(keepInventory);
        CreeperApocalypse.CONFIG.setMilestonesEnabled(milestonesEnabled);
        CreeperApocalypse.CONFIG.setIgnoreDayProgression(ignoreDayProgression);
        
        CreeperApocalypse.CONFIG.setMaxCreepersOverworld(maxCreepersOverworld);
        CreeperApocalypse.CONFIG.setMaxCreepersNether(maxCreepersNether);
        CreeperApocalypse.CONFIG.setMaxCreepersEnd(maxCreepersEnd);
        
        CreeperApocalypse.CONFIG.save();
        
        ModNetworking.sendConfigUpdate(enabled, maxMultiplier, escalationSpeed);
        
        close();
    }
    
    @Override
    public void close() {
        if (client != null) {
            client.setScreen(parent);
        }
    }

    private float cycleChance(float current) {
        float next = current + 0.05f;
        if (next > 1.0f) {
            next = 0.0f;
        }
        return Math.round(next * 100f) / 100f;
    }

    private String formatPercent(float value) {
        return String.format("%.0f%%", value * 100f);
    }

    private void cycleGuiScale() {
        if (client == null) {
            return;
        }
        // 0 = auto, then 1..4
        guiScale = (guiScale + 1) % 5;
        client.options.getGuiScale().setValue(guiScale);
        client.onResolutionChanged();
        client.options.write();
    }

    private String guiScaleText() {
        return guiScale == 0 ? "Auto" : String.valueOf(guiScale);
    }
}
