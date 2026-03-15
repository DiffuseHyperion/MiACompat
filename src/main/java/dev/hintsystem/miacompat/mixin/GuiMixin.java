package dev.hintsystem.miacompat.mixin;

import dev.hintsystem.miacompat.MiACompat;

import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @ModifyVariable(
        method = "setOverlayMessage",
        at = @At("HEAD"),
        argsOnly = true
    )
    public Component miacompat$onOverlayMessage(Component message) {
        return MiACompat.ghostSeekTracker.modifyActionbarMessage(message);
    }
}
