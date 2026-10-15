package com.fpsboost.mixin;

import com.fpsboost.config.CommonConfig;
import com.fpsboost.server.ServerState;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Throttles AI/brain ticks and despawn checks of mobs that no player is anywhere near. */
@Mixin(Mob.class)
public abstract class MobMixin {
    @Unique
    private boolean fpsboost$far = false;

    @Inject(method = "serverAiStep", at = @At("HEAD"), cancellable = true, require = 0)
    private void fpsboost$throttleAi(CallbackInfo ci) {
        if (!CommonConfig.mobThrottleEnabled) return;
        int interval = ServerState.interval();
        if (interval <= 1) return;

        Mob self = (Mob) (Object) this;
        if (self.level().isClientSide) return;

        int phase = self.tickCount + self.getId();

        // Re-evaluate "is anyone near?" once a second, not every tick.
        if (phase % 20 == 0) {
            fpsboost$far = self.level().getNearestPlayer(self, ServerState.range()) == null;
        }
        if (!fpsboost$far) return;

        // Mobs that are busy with something keep full speed.
        if (self.getTarget() != null || self.isPassenger() || self.isVehicle() || self.isLeashed()) return;

        if (phase % interval != 0) {
            ServerState.throttledAiTicks++;
            ci.cancel();
        }
    }

    /** The vanilla despawn check looks up the nearest player every tick; far mobs only need it now and then. */
    @Inject(method = "checkDespawn", at = @At("HEAD"), cancellable = true, require = 0)
    private void fpsboost$throttleDespawn(CallbackInfo ci) {
        if (!CommonConfig.despawnThrottle || !fpsboost$far) return;
        Mob self = (Mob) (Object) this;
        if (self.level().isClientSide) return;
        if ((self.tickCount + self.getId()) % 4 != 0) {
            ci.cancel();
        }
    }
}
