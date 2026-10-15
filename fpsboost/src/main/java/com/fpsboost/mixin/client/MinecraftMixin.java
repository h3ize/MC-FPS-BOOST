package com.fpsboost.mixin.client;

import com.fpsboost.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Caps FPS while the window is unfocused so a background Minecraft doesn't eat your GPU/CPU. */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow
    public abstract boolean isWindowActive();

    @Shadow
    public Screen screen;

    @Shadow
    public ClientLevel level;

    @Inject(method = "getFramerateLimit", at = @At("RETURN"), cancellable = true, require = 0)
    private void fpsboost$backgroundLimit(CallbackInfoReturnable<Integer> cir) {
        if (ClientConfig.backgroundLimiter && !this.isWindowActive()) {
            cir.setReturnValue(Math.min(cir.getReturnValue(), ClientConfig.backgroundFps));
            return;
        }
        // Title screen, loading screens and the pause menu don't need uncapped FPS.
        if (ClientConfig.menuFps > 0 && (this.level == null || this.screen instanceof PauseScreen)) {
            cir.setReturnValue(Math.min(cir.getReturnValue(), ClientConfig.menuFps));
        }
    }
}
