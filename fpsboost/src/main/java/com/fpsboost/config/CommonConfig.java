package com.fpsboost.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Settings that affect the (integrated or dedicated) server. File: config/fpsboost-common.toml */
public final class CommonConfig {
    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.BooleanValue MOB_THROTTLE;
    private static final ModConfigSpec.IntValue MOB_RANGE;
    private static final ModConfigSpec.IntValue MOB_INTERVAL;
    private static final ModConfigSpec.BooleanValue FAR_COLLISION;
    private static final ModConfigSpec.BooleanValue DESPAWN_THROTTLE;
    private static final ModConfigSpec.BooleanValue ADAPTIVE_SERVER;
    private static final ModConfigSpec.DoubleValue HIGH_MSPT;

    // Cached values, safe to read from mixins on hot paths.
    public static volatile boolean mobThrottleEnabled = true;
    public static volatile int mobThrottleRange = 96;
    public static volatile int mobThrottleInterval = 4;
    public static volatile boolean farCollisionThrottle = true;
    public static volatile boolean despawnThrottle = true;
    public static volatile boolean adaptiveServer = true;
    public static volatile double adaptiveHighMspt = 40.0;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.comment("Mob AI throttling for mobs that are far away from every player").push("mob_ai_throttle");
        MOB_THROTTLE = b.comment("Run the AI/brain of far-away mobs only once every 'interval' ticks.",
                "Big TPS saver in modpacks with lots of villagers/animals. Set false if anything feels off.")
                .define("enabled", true);
        MOB_RANGE = b.comment("A mob counts as 'far' if no player is within this many blocks.")
                .defineInRange("range", 96, 32, 512);
        MOB_INTERVAL = b.comment("Far mobs think once every N ticks (1 = never throttle).")
                .defineInRange("interval", 4, 1, 20);
        b.pop();
        b.comment("More server savings for far-away mobs (same 'far' range as above)").push("far_mobs");
        FAR_COLLISION = b.comment("Far mobs push/collide with neighbours only once every 4 ticks (saves the per-tick entity scan and cramming check).")
                .define("throttle_collisions", true);
        DESPAWN_THROTTLE = b.comment("Far mobs run their despawn check only once every 4 ticks.")
                .define("throttle_despawn_checks", true);
        b.pop();

        b.comment("Adaptive server: when the average tick time (MSPT) climbs, the far range shrinks and the AI interval grows automatically.").push("adaptive_server");
        ADAPTIVE_SERVER = b.define("enabled", true);
        HIGH_MSPT = b.comment("Average MSPT above this = full emergency mode (range x0.5, interval x2). Above 75% of it = mild mode.")
                .defineInRange("high_mspt", 40.0, 20.0, 50.0);
        b.pop();

        SPEC = b.build();
    }

    private CommonConfig() {}

    public static void refresh() {
        if (!SPEC.isLoaded()) return;
        mobThrottleEnabled = MOB_THROTTLE.get();
        mobThrottleRange = MOB_RANGE.get();
        mobThrottleInterval = MOB_INTERVAL.get();
        farCollisionThrottle = FAR_COLLISION.get();
        despawnThrottle = DESPAWN_THROTTLE.get();
        adaptiveServer = ADAPTIVE_SERVER.get();
        adaptiveHighMspt = HIGH_MSPT.get();
    }
}
