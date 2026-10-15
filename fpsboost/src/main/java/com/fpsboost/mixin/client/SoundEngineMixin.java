package com.fpsboost.mixin.client;

import com.fpsboost.client.ClientState;
import com.fpsboost.config.ClientConfig;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Caps how many new in-world sounds can start per tick (huge farms/explosions/modded machines can spam hundreds). */
@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin {
    @Inject(method = "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void fpsboost$limitSounds(SoundInstance sound, CallbackInfo ci) {
        if (!ClientConfig.soundLimiter) return;
        if (sound.isRelative()) return; // UI, music, other non-positional sounds are never dropped
        if (ClientState.soundsThisTick++ >= ClientConfig.maxSoundsPerTick) {
            ClientState.droppedSounds++;
            ci.cancel();
        }
    }
}
