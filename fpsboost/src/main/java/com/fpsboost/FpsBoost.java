package com.fpsboost;

import com.fpsboost.config.ClientConfig;
import com.fpsboost.config.CommonConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.config.ModConfigEvent;

@Mod(FpsBoost.MODID)
public class FpsBoost {
    public static final String MODID = "fpsboost";

    public FpsBoost(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        container.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
        modBus.addListener(this::onConfigLoad);
        modBus.addListener(this::onConfigReload);
    }

    private void onConfigLoad(ModConfigEvent.Loading event) {
        ClientConfig.refresh();
        CommonConfig.refresh();
    }

    private void onConfigReload(ModConfigEvent.Reloading event) {
        ClientConfig.refresh();
        CommonConfig.refresh();
    }
}
