package com.diffusehyperion.dmm.mixins;

import com.diffusehyperion.dmm.DMM;

import com.diffusehyperion.dmm.features.ghostseek.GhostSeekFeature;
import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

// this mixin is for the ghost seek feature, in order to capture when the ghost seek triggers
@Mixin(Gui.class)
public abstract class GuiMixin {
    @ModifyVariable(
        method = "setOverlayMessage",
        at = @At("HEAD"),
        argsOnly = true
    )
    public Component miacompat$onOverlayMessage(Component message) {
        return ((GhostSeekFeature) DMM.featureManager.getFeature(GhostSeekFeature.class)).onGhostSeekTriggered(message);
    }
}
