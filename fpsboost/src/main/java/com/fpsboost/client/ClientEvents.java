package com.fpsboost.client;

import com.fpsboost.FpsBoost;
import com.mojang.brigadier.Command;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import com.fpsboost.config.ClientConfig;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

@EventBusSubscriber(modid = FpsBoost.MODID, value = Dist.CLIENT)
public final class ClientEvents {
    private ClientEvents() {}

    @SubscribeEvent
    public static void onClientTickPre(ClientTickEvent.Pre event) {
        ClientState.tick++;
        ClientState.particlesThisTick = 0;
        ClientState.soundsThisTick = 0;
    }

    private static long lastFrameNanos = 0L;
    private static double avgFrameMs = 16.0;

    /** Measures frame time and adapts the culling scale to hit the target FPS. */
    @SubscribeEvent
    public static void onRenderFrame(RenderFrameEvent.Pre event) {
        ClientState.entitiesThisFrame = 0;
        long now = System.nanoTime();
        if (lastFrameNanos != 0L) {
            double ms = (now - lastFrameNanos) / 1_000_000.0;
            if (ms < 1000.0) { // ignore huge hitches (world load, alt-tab)
                avgFrameMs = avgFrameMs * 0.97 + ms * 0.03;
            }
        }
        lastFrameNanos = now;

        Minecraft mc = Minecraft.getInstance();
        double goal = 1.0;
        if (ClientConfig.adaptive && mc.level != null && mc.isWindowActive()) {
            double targetMs = (1000.0 / ClientConfig.adaptiveTargetFps) * 1.1;
            if (avgFrameMs > targetMs) {
                goal = Math.max(ClientConfig.adaptiveMinScale, targetMs / avgFrameMs);
            }
        }
        ClientState.cullScale += (goal - ClientState.cullScale) * 0.02;
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientState.cullScale = 1.0;
        if (ClientConfig.gcOnDisconnect) {
            System.gc();
        }
    }

    @SubscribeEvent
    public static void onClientTickPost(ClientTickEvent.Post event) {
        OptionsTuner.tryApply();
    }

    /** Fallback culling for living entities (the mixin covers every entity type; this still works if it can't apply). */
    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Pre<?, ?> event) {
        LivingEntity entity = event.getEntity();
        Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        if (CullingLogic.shouldCull(entity, entity.distanceToSqr(cam))) {
            ClientState.culledEntities++;
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("fpsboost")
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.literal(
                            "[FPS Boost] skipped entity renders: " + ClientState.culledEntities
                                    + " | skipped block entity renders: " + ClientState.culledBlockEntities
                                    + " | dropped particles: " + ClientState.droppedParticles
                                    + " | dropped sounds: " + ClientState.droppedSounds
                                    + " | adaptive scale: " + String.format("%.2f", ClientState.cullScale)), false);
                    return Command.SINGLE_SUCCESS;
                }));
    }
}
