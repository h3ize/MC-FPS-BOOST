package com.fpsboost.mixin.client;

import com.fpsboost.client.ClientState;
import com.fpsboost.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {
    @Inject(method = "createParticle", at = @At("HEAD"), cancellable = true, require = 0)
    private void fpsboost$limit(ParticleOptions options, double x, double y, double z,
                                double vx, double vy, double vz, CallbackInfoReturnable<Particle> cir) {
        // Vanilla's firework code casts this method's result to SparkParticle and calls setTrail() on it
        // without a null check, so returning null for FIREWORK sparks crashes the game (NullPointerException).
        // Never drop, block or budget-count them.
        if (options.getType() == ParticleTypes.FIREWORK) return;

        // Blocklist first: blocked particles don't even use up budget.
        Set<ResourceLocation> blocked = ClientConfig.blockedParticles;
        if (!blocked.isEmpty()) {
            ResourceLocation key = BuiltInRegistries.PARTICLE_TYPE.getKey(options.getType());
            if (key != null && blocked.contains(key)) {
                ClientState.droppedParticles++;
                cir.setReturnValue(null);
                return;
            }
        }

        if (!ClientConfig.particleLimiter) return;

        if (ClientState.particlesThisTick++ >= ClientConfig.maxParticlesPerTick * ClientState.cullScale) {
            ClientState.droppedParticles++;
            cir.setReturnValue(null);
            return;
        }

        Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        double dx = x - cam.x, dy = y - cam.y, dz = z - cam.z;
        double limit = ClientConfig.particleCullDistance * ClientState.cullScale;
        if (dx * dx + dy * dy + dz * dz > limit * limit) {
            ClientState.droppedParticles++;
            cir.setReturnValue(null);
        }
    }
}
