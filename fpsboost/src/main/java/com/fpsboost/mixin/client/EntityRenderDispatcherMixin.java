package com.fpsboost.mixin.client;

import com.fpsboost.client.ClientState;
import com.fpsboost.client.CullingLogic;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true, require = 0)
    private void fpsboost$distanceCull(Entity entity, Frustum frustum, double camX, double camY, double camZ,
                                       CallbackInfoReturnable<Boolean> cir) {
        if (CullingLogic.shouldCull(entity, entity.distanceToSqr(camX, camY, camZ))) {
            ClientState.culledEntities++;
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "shouldRender", at = @At("RETURN"), cancellable = true, require = 0)
    private void fpsboost$budgetCull(Entity entity, Frustum frustum, double camX, double camY, double camZ,
                                     CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) return; // already not drawn (frustum etc.)
        if (CullingLogic.overBudget(entity, entity.distanceToSqr(camX, camY, camZ))) {
            ClientState.culledEntities++;
            cir.setReturnValue(false);
        }
    }
}
