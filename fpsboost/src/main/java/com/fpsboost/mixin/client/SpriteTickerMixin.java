package com.fpsboost.mixin.client;

import com.fpsboost.client.ClientState;
import com.fpsboost.config.ClientConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Animated textures (water, lava, fire, modded) upload to the GPU every tick; advance them less often. */
@Mixin(targets = "net.minecraft.client.renderer.texture.SpriteContents$Ticker")
public abstract class SpriteTickerMixin {
    @Inject(method = "tickAndUpload", at = @At("HEAD"), cancellable = true, require = 0)
    private void fpsboost$throttleAnimations(int x, int y, CallbackInfo ci) {
        int n = ClientConfig.animatedTextureThrottle;
        if (n > 1 && ClientState.tick % n != 0) {
            ci.cancel();
        }
    }
}
