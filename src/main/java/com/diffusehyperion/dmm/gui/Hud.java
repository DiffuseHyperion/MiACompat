package com.diffusehyperion.dmm.gui;

import com.diffusehyperion.dmm.DMM;

import com.diffusehyperion.dmm.features.ghostseek.gui.GhostSeekCooldown;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;

import org.jetbrains.annotations.NotNull;

public class Hud implements HudElement {
    public static final RenderPipeline GUI_TEXTURED_MULTIPLY = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
            .withLocation("pipeline/gui_textured_multiply")
            .withBlend(new BlendFunction(
                SourceFactor.DST_COLOR, DestFactor.ZERO
            )).build()
    );

    public static final Identifier BAR_OVERLAY = DMM.id("textures/gui/bar_overlay.png");
    public static final int BAR_OVERLAY_WIDTH = 60;
    public static final int BAR_OVERLAY_HEIGHT = 5;

    private final GhostSeekCooldown ghostSeekCooldown;

    public Hud() {
        ghostSeekCooldown = new GhostSeekCooldown();
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, @NotNull DeltaTracker deltaTracker) {
        if (!DMM.isMiAServer()) return;

        if (DMM.config.showGhostSeekCooldown) ghostSeekCooldown.render(guiGraphics, deltaTracker);
    }
}
