package com.fpsboost.mixin.client;

import com.fpsboost.client.ClientState;
import com.fpsboost.config.ClientConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Ambient block particles (torches, lava, water drips...) are scanned for in ~667 random blocks per tick; run that less often. */
@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
    @Inject(method = "animateTick", at = @At("HEAD"), cancellable = true, require = 0)
    private void fpsboost$throttleAmbient(int x, int y, int z, CallbackInfo ci) {
        int n = ClientConfig.ambientParticleThrottle;
        if (n > 1 && ClientState.tick % n != 0) {
            ci.cancel();
        }
    }
}
