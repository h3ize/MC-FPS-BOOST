package com.fpsboost.mixin;

import com.fpsboost.config.CommonConfig;
import com.fpsboost.server.ServerState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Every living entity scans for neighbours to push (and checks cramming) each tick; far ones can do it less often. */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Unique
    private boolean fpsboost$farPush = false;

    @Inject(method = "pushEntities", at = @At("HEAD"), cancellable = true, require = 0)
    private void fpsboost$throttlePush(CallbackInfo ci) {
        if (!CommonConfig.farCollisionThrottle) return;

        LivingEntity self = (LivingEntity) (Object) this;
        if (self.level().isClientSide || self instanceof Player) return;

        int phase = self.tickCount + self.getId();
        if (phase % 20 == 0) {
            fpsboost$farPush = self.level().getNearestPlayer(self, ServerState.range()) == null;
        }
        if (fpsboost$farPush && phase % 4 != 0) {
            ServerState.throttledCollisionTicks++;
            ci.cancel();
        }
    }
}
