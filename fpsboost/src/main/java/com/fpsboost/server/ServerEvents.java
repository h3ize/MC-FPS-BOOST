package com.fpsboost.server;

import com.fpsboost.FpsBoost;
import com.fpsboost.config.CommonConfig;
import com.mojang.brigadier.Command;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = FpsBoost.MODID)
public final class ServerEvents {
    private static long startNanos = 0L;

    private ServerEvents() {}

    @SubscribeEvent
    public static void onServerTickPre(ServerTickEvent.Pre event) {
        startNanos = System.nanoTime();
    }

    /** Tracks average tick time and tightens/relaxes the mob throttle accordingly. */
    @SubscribeEvent
    public static void onServerTickPost(ServerTickEvent.Post event) {
        double ms = (System.nanoTime() - startNanos) / 1_000_000.0;
        ServerState.avgMspt = ServerState.avgMspt * 0.98 + ms * 0.02;

        double goalRange = 1.0;
        double goalInterval = 1.0;
        if (CommonConfig.adaptiveServer) {
            double high = CommonConfig.adaptiveHighMspt;
            if (ServerState.avgMspt > high) {
                goalRange = 0.5;
                goalInterval = 2.0;
            } else if (ServerState.avgMspt > high * 0.75) {
                goalRange = 0.75;
                goalInterval = 1.5;
            }
        }
        ServerState.rangeScale += (goalRange - ServerState.rangeScale) * 0.01;
        ServerState.intervalMult += (goalInterval - ServerState.intervalMult) * 0.01;
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("fpsboostserver")
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.literal(
                            "[FPS Boost] avg MSPT: " + String.format("%.1f", ServerState.avgMspt)
                                    + " | far range: " + ServerState.range() + " blocks"
                                    + " | far AI interval: " + ServerState.interval() + " ticks"
                                    + " | skipped AI ticks: " + ServerState.throttledAiTicks
                                    + " | skipped collision scans: " + ServerState.throttledCollisionTicks), false);
                    return Command.SINGLE_SUCCESS;
                }));
    }
}
