package com.diffusehyperion.dmm.mixins;

import com.diffusehyperion.dmm.DMM;

import net.minecraft.client.renderer.GameRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "close", at = @At("RETURN"))
    private void miacompat$onGameRendererClose(CallbackInfo ci) {
        DMM.close();
    }
}
