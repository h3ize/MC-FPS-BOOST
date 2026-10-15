package com.fpsboost.mixin.client;

import com.fpsboost.client.ClientState;
import com.fpsboost.config.ClientConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin {
    @Inject(method = "render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void fpsboost$distanceCull(BlockEntity blockEntity, float partialTick, PoseStack poseStack,
                                       MultiBufferSource bufferSource, CallbackInfo ci) {
        if (!ClientConfig.blockEntityCulling) return;
        if (blockEntity instanceof BeaconBlockEntity
                || blockEntity instanceof TheEndGatewayBlockEntity
                || blockEntity instanceof TheEndPortalBlockEntity) return;

        Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        double limit = ClientConfig.blockEntityCullDistance * ClientState.cullScale;
        if (blockEntity.getBlockPos().distToCenterSqr(cam.x, cam.y, cam.z) > limit * limit) {
            ClientState.culledBlockEntities++;
            ci.cancel();
        }
    }
}
