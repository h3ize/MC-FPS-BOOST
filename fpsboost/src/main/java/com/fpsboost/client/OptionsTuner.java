package com.fpsboost.client;

import com.fpsboost.config.ClientConfig;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;

/** Applies lighter vanilla video settings exactly once (first time the title screen shows). */
public final class OptionsTuner {
    private static boolean checked = false;

    private OptionsTuner() {}

    public static void tryApply() {
        if (checked) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null || !(mc.screen instanceof TitleScreen)) return;
        checked = true;

        if (!ClientConfig.applyOptimizedSettings) return;
        Path marker = FMLPaths.CONFIGDIR.get().resolve("fpsboost.applied");
        if (Files.exists(marker)) return;

        try {
            Options o = mc.options;
            o.biomeBlendRadius().set(1);
            o.cloudStatus().set(CloudStatus.FAST);
            o.entityShadows().set(false);
            o.particles().set(ParticleStatus.DECREASED);
            o.entityDistanceScaling().set(0.75);
            if (ClientConfig.forceFastGraphics) {
                o.graphicsMode().set(GraphicsStatus.FAST);
            }
            int sim = ClientConfig.simulationDistance;
            if (sim > 0 && o.simulationDistance().get() > sim) {
                o.simulationDistance().set(sim);
            }
            o.save();
            Files.writeString(marker, "FPS Boost applied its one-time video settings. Delete this file to apply again.\n");
        } catch (Exception ignored) {
            // Never crash the game over a settings tweak.
        }
    }
}
