package com.diffusehyperion.dmm.features.ghostseek.gui;

import com.diffusehyperion.dmm.features.ghostseek.GhostSeekItemType;
import com.diffusehyperion.dmm.DMM;

import com.diffusehyperion.dmm.gui.Hud;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class GhostSeekCooldownElement implements HudElement {
    private static final Identifier BAR_BACKGROUND = DMM.id("textures/gui/cooldown_bar.png");
    private static final int BAR_BACKGROUND_WIDTH = 194;
    private static final int BAR_BACKGROUND_HEIGHT = 11;

    private Color colour;
    private float cooldownTicks;
    private float maxCooldownTicks;

    public void onGhostSeekCooldownStart(GhostSeekItemType ghostSeekItemType, Color sequenceColour) {
        colour = sequenceColour;
        cooldownTicks = 0;
        maxCooldownTicks = ghostSeekItemType.pingCooldownSecs * 20;
    }

    public void tick() {
        cooldownTicks++;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, @NotNull DeltaTracker deltaTracker) {
        if (cooldownTicks <= 0 || cooldownTicks > maxCooldownTicks) return;

        int xPos = guiGraphics.guiWidth() / 2;
        int yPos = guiGraphics.guiHeight() - 52;
        int bgY = yPos - BAR_BACKGROUND_HEIGHT;
        int filledY = bgY + 3;

        float progress = cooldownTicks / maxCooldownTicks;
        int halfWidth = (int) (progress * Hud.BAR_OVERLAY_WIDTH);

        int bgCapWidth = 6;
        int bgHalfWidth = halfWidth + bgCapWidth;

        float fadeInPortion = 0.02f;
        float fadeOutPortion = 0.1f;

        float alpha;
        if (progress > 1f - fadeInPortion) {
            float t = (1f - progress) / fadeInPortion;
            alpha = Math.clamp(t, 0f, 1f);
        } else if (progress < fadeOutPortion) {
            float t = progress / fadeOutPortion;
            alpha = Math.clamp(t, 0f, 1f);
        } else {
            alpha = 1f;
        }

        int colorAlpha = ARGB.color(alpha, colour.getRGB());
        int whiteAlpha = ARGB.color(alpha, -1);

        // Left side cap and bar outline
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BAR_BACKGROUND,
            xPos - bgHalfWidth, bgY,
            0, 0,
            bgHalfWidth, BAR_BACKGROUND_HEIGHT,
            BAR_BACKGROUND_WIDTH, BAR_BACKGROUND_HEIGHT,
            whiteAlpha
        );

        // Right side cap and bar outline
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BAR_BACKGROUND,
            xPos, bgY,
            BAR_BACKGROUND_WIDTH - bgHalfWidth, 0,
            bgHalfWidth, BAR_BACKGROUND_HEIGHT,
            BAR_BACKGROUND_WIDTH, BAR_BACKGROUND_HEIGHT,
            whiteAlpha
        );

        guiGraphics.enableScissor(
            xPos - halfWidth, filledY-1,
            xPos + halfWidth, filledY+1 + Hud.BAR_OVERLAY_HEIGHT
        );

        guiGraphics.fill(xPos - halfWidth, filledY, xPos + halfWidth, filledY + Hud.BAR_OVERLAY_HEIGHT, colorAlpha);

        // Right side
        guiGraphics.blit(Hud.GUI_TEXTURED_MULTIPLY, Hud.BAR_OVERLAY,
            xPos, filledY,
            0, 0,
            Hud.BAR_OVERLAY_WIDTH, Hud.BAR_OVERLAY_HEIGHT,
            Hud.BAR_OVERLAY_WIDTH, Hud.BAR_OVERLAY_HEIGHT,
            whiteAlpha
        );

        // Left side
        guiGraphics.blit(Hud.GUI_TEXTURED_MULTIPLY, Hud.BAR_OVERLAY,
            xPos - Hud.BAR_OVERLAY_WIDTH, filledY,
            0, 0,
            Hud.BAR_OVERLAY_WIDTH, Hud.BAR_OVERLAY_HEIGHT,
            -Hud.BAR_OVERLAY_WIDTH, Hud.BAR_OVERLAY_HEIGHT,
            whiteAlpha
        );

        guiGraphics.disableScissor();
    }
}
