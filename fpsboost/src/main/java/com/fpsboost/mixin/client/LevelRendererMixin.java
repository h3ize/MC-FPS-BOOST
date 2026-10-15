package com.fpsboost.mixin.client;

import com.fpsboost.client.ClientState;
import com.fpsboost.config.ClientConfig;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    /** Rain/snow splash particles are spawned every tick around the camera; run that less often. */
    @Inject(method = "tickRain", at = @At("HEAD"), cancellable = true, require = 0)
    private void fpsboost$throttleRain(CallbackInfo ci) {
        int n = ClientConfig.ambientParticleThrottle;
        if (n > 1 && ClientState.tick % n != 0) {
            ci.cancel();
        }
    }

    /** Skip drawing rain/snow while the adaptive system says FPS is low. */
    @Inject(method = "renderSnowAndRain", at = @At("HEAD"), cancellable = true, require = 0)
    private void fpsboost$skipWeather(CallbackInfo ci) {
        if (ClientConfig.skipWeatherWhenLow && ClientState.cullScale < ClientConfig.lowFpsScale) {
            ci.cancel();
        }
    }

    /** Skip drawing clouds while the adaptive system says FPS is low. */
    @Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true, require = 0)
    private void fpsboost$skipClouds(CallbackInfo ci) {
        if (ClientConfig.skipCloudsWhenLow && ClientState.cullScale < ClientConfig.lowFpsScale) {
            ci.cancel();
        }
    }
}
