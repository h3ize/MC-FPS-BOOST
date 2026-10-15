package com.fpsboost.config;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.HashSet;
import java.util.Set;

/** Rendering-side settings. File: config/fpsboost-client.toml */
public final class ClientConfig {
    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.BooleanValue ENTITY_CULLING;
    private static final ModConfigSpec.DoubleValue ENTITY_DISTANCE;
    private static final ModConfigSpec.DoubleValue ENTITY_SIZE_FACTOR;
    private static final ModConfigSpec.BooleanValue BLOCK_ENTITY_CULLING;
    private static final ModConfigSpec.IntValue BLOCK_ENTITY_DISTANCE;
    private static final ModConfigSpec.BooleanValue PARTICLE_LIMITER;
    private static final ModConfigSpec.IntValue MAX_PARTICLES;
    private static final ModConfigSpec.IntValue PARTICLE_DISTANCE;
    private static final ModConfigSpec.IntValue AMBIENT_THROTTLE;
    private static final ModConfigSpec.IntValue ANIM_THROTTLE;
    private static final ModConfigSpec.BooleanValue BACKGROUND_LIMITER;
    private static final ModConfigSpec.IntValue BACKGROUND_FPS;
    private static final ModConfigSpec.BooleanValue APPLY_SETTINGS;
    private static final ModConfigSpec.BooleanValue FORCE_FAST_GRAPHICS;
    private static final ModConfigSpec.IntValue SIM_DISTANCE;
    private static final ModConfigSpec.BooleanValue ADAPTIVE;
    private static final ModConfigSpec.IntValue ADAPTIVE_TARGET_FPS;
    private static final ModConfigSpec.DoubleValue ADAPTIVE_MIN_SCALE;
    private static final ModConfigSpec.IntValue MENU_FPS;
    private static final ModConfigSpec.BooleanValue SOUND_LIMITER;
    private static final ModConfigSpec.IntValue MAX_SOUNDS;
    private static final ModConfigSpec.BooleanValue GC_ON_DISCONNECT;
    private static final ModConfigSpec.BooleanValue ENTITY_BUDGET;
    private static final ModConfigSpec.IntValue MAX_ENTITIES;
    private static final ModConfigSpec.IntValue BUDGET_NEAR;
    private static final ModConfigSpec.ConfigValue<String> BLOCKED_PARTICLES;
    private static final ModConfigSpec.BooleanValue SKIP_WEATHER;
    private static final ModConfigSpec.BooleanValue SKIP_CLOUDS;
    private static final ModConfigSpec.DoubleValue LOW_FPS_SCALE;

    // Cached values, safe to read from mixins on hot paths.
    public static volatile boolean entityCulling = true;
    public static volatile double entityCullDistance = 56.0;
    public static volatile double entityCullSizeFactor = 8.0;
    public static volatile boolean blockEntityCulling = true;
    public static volatile int blockEntityCullDistance = 64;
    public static volatile boolean particleLimiter = true;
    public static volatile int maxParticlesPerTick = 300;
    public static volatile int particleCullDistance = 40;
    public static volatile int ambientParticleThrottle = 2;
    public static volatile int animatedTextureThrottle = 2;
    public static volatile boolean backgroundLimiter = true;
    public static volatile int backgroundFps = 10;
    public static volatile boolean applyOptimizedSettings = true;
    public static volatile boolean forceFastGraphics = false;
    public static volatile int simulationDistance = 8;
    public static volatile boolean adaptive = true;
    public static volatile int adaptiveTargetFps = 60;
    public static volatile double adaptiveMinScale = 0.5;
    public static volatile int menuFps = 30;
    public static volatile boolean soundLimiter = true;
    public static volatile int maxSoundsPerTick = 48;
    public static volatile boolean gcOnDisconnect = true;
    public static volatile boolean entityBudget = true;
    public static volatile int maxEntitiesPerFrame = 200;
    public static volatile int budgetNearDistance = 24;
    public static volatile Set<ResourceLocation> blockedParticles = Set.of();
    public static volatile boolean skipWeatherWhenLow = true;
    public static volatile boolean skipCloudsWhenLow = true;
    public static volatile double lowFpsScale = 0.85;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.comment("Entity distance culling (item entities, XP orbs, players and bosses are never touched)").push("entity_culling");
        ENTITY_CULLING = b.define("enabled", true);
        ENTITY_DISTANCE = b.comment("Base distance in blocks after which entities are not rendered.")
                .defineInRange("distance", 56.0, 16.0, 256.0);
        ENTITY_SIZE_FACTOR = b.comment("Bigger entities get extra distance: limit = distance + size * factor.")
                .defineInRange("size_factor", 8.0, 0.0, 32.0);
        b.pop();

        b.comment("Block entity (chests, signs, banners, modded machines...) distance culling").push("block_entity_culling");
        BLOCK_ENTITY_CULLING = b.define("enabled", true);
        BLOCK_ENTITY_DISTANCE = b.comment("Distance in blocks. Beacons and end portals/gateways are always rendered.")
                .defineInRange("distance", 64, 16, 256);
        b.pop();

        b.comment("Particle limiter").push("particles");
        PARTICLE_LIMITER = b.define("enabled", true);
        MAX_PARTICLES = b.comment("Hard budget of new particles per client tick. Extras are dropped.")
                .defineInRange("max_per_tick", 300, 20, 5000);
        PARTICLE_DISTANCE = b.comment("Particles spawning farther than this many blocks from the camera are dropped.")
                .defineInRange("distance", 40, 8, 128);
        AMBIENT_THROTTLE = b.comment("Ambient block particles (torch smoke, lava drips, etc.) run once every N ticks.")
                .defineInRange("ambient_throttle", 2, 1, 8);
        b.pop();

        b.comment("Animated texture throttling (water, lava, fire, modded animated blocks)").push("animated_textures");
        ANIM_THROTTLE = b.comment("Animated textures advance once every N client ticks (1 = vanilla).")
                .defineInRange("throttle", 2, 1, 8);
        b.pop();

        b.comment("FPS limiter while the game window is not focused").push("background");
        BACKGROUND_LIMITER = b.define("enabled", true);
        BACKGROUND_FPS = b.defineInRange("fps", 10, 1, 60);
        b.pop();

        b.comment("One-time video setting optimisation").push("settings");
        APPLY_SETTINGS = b.comment("On first launch, apply lighter video settings once (biome blend 1, fast clouds,",
                "no entity shadows, decreased particles, entity distance 75%).",
                "Delete config/fpsboost.applied to apply again.")
                .define("apply_once", true);
        FORCE_FAST_GRAPHICS = b.comment("Also switch Graphics to 'Fast' during that one-time step.")
                .define("fast_graphics", false);
        SIM_DISTANCE = b.comment("During that one-time step, lower simulation distance to this value if it is higher (0 = leave alone).",
                "Helps a lot when hosting a LAN/singleplayer world with many mods.")
                .defineInRange("simulation_distance", 8, 0, 32);
        b.pop();

        b.comment("Adaptive culling: when your FPS drops below the target, all culling distances and the particle budget shrink automatically (and grow back when FPS recovers)").push("adaptive");
        ADAPTIVE = b.define("enabled", true);
        ADAPTIVE_TARGET_FPS = b.defineInRange("target_fps", 60, 20, 240);
        ADAPTIVE_MIN_SCALE = b.comment("Distances never shrink below this fraction.")
                .defineInRange("min_scale", 0.5, 0.2, 1.0);
        b.pop();

        b.comment("Misc").push("misc");
        MENU_FPS = b.comment("FPS cap on the title screen, loading screens and pause menu (0 = vanilla).")
                .defineInRange("menu_fps", 30, 0, 260);
        SOUND_LIMITER = b.comment("Limit how many new sounds can start per tick (UI/music never limited).")
                .define("sound_limiter", true);
        MAX_SOUNDS = b.defineInRange("max_sounds_per_tick", 48, 8, 500);
        GC_ON_DISCONNECT = b.comment("Run one garbage collection when you leave a world, so the next world starts with free memory.")
                .define("gc_on_disconnect", true);
        b.pop();

        b.comment("Per-frame entity budget: once more than N entities are drawn in a frame, entities beyond the 'near' distance are skipped. Nearby entities are always drawn.").push("entity_budget");
        ENTITY_BUDGET = b.define("enabled", true);
        MAX_ENTITIES = b.defineInRange("max_entities_per_frame", 200, 20, 5000);
        BUDGET_NEAR = b.comment("Entities closer than this many blocks never count against the budget cut.")
                .defineInRange("near_distance", 24, 4, 128);
        b.pop();

        b.comment("Particle blocklist").push("particle_blocklist");
        BLOCKED_PARTICLES = b.comment("Comma-separated particle ids that are never spawned (purely cosmetic spam). Clear it to disable.",
                "Item/block-break particles are not on this list.")
                .define("blocked_ids", "minecraft:ambient_entity_effect,minecraft:ash,minecraft:white_ash,minecraft:crimson_spore,minecraft:warped_spore,minecraft:spore_blossom_air,minecraft:falling_spore_blossom,minecraft:underwater");
        b.pop();

        b.comment("Extra savings while adaptive culling says your FPS is low (see [adaptive])").push("low_fps_savings");
        SKIP_WEATHER = b.comment("Skip drawing rain/snow while FPS is low.").define("skip_weather", true);
        SKIP_CLOUDS = b.comment("Skip drawing clouds while FPS is low.").define("skip_clouds", true);
        LOW_FPS_SCALE = b.comment("'Low FPS' means the adaptive scale is below this value (1.0 = at target).")
                .defineInRange("scale_threshold", 0.85, 0.3, 1.0);
        b.pop();

        SPEC = b.build();
    }

    private ClientConfig() {}

    public static void refresh() {
        if (!SPEC.isLoaded()) return;
        entityCulling = ENTITY_CULLING.get();
        entityCullDistance = ENTITY_DISTANCE.get();
        entityCullSizeFactor = ENTITY_SIZE_FACTOR.get();
        blockEntityCulling = BLOCK_ENTITY_CULLING.get();
        blockEntityCullDistance = BLOCK_ENTITY_DISTANCE.get();
        particleLimiter = PARTICLE_LIMITER.get();
        maxParticlesPerTick = MAX_PARTICLES.get();
        particleCullDistance = PARTICLE_DISTANCE.get();
        ambientParticleThrottle = AMBIENT_THROTTLE.get();
        animatedTextureThrottle = ANIM_THROTTLE.get();
        backgroundLimiter = BACKGROUND_LIMITER.get();
        backgroundFps = BACKGROUND_FPS.get();
        applyOptimizedSettings = APPLY_SETTINGS.get();
        forceFastGraphics = FORCE_FAST_GRAPHICS.get();
        simulationDistance = SIM_DISTANCE.get();
        adaptive = ADAPTIVE.get();
        adaptiveTargetFps = ADAPTIVE_TARGET_FPS.get();
        adaptiveMinScale = ADAPTIVE_MIN_SCALE.get();
        menuFps = MENU_FPS.get();
        soundLimiter = SOUND_LIMITER.get();
        maxSoundsPerTick = MAX_SOUNDS.get();
        gcOnDisconnect = GC_ON_DISCONNECT.get();
        entityBudget = ENTITY_BUDGET.get();
        maxEntitiesPerFrame = MAX_ENTITIES.get();
        budgetNearDistance = BUDGET_NEAR.get();
        blockedParticles = parseIds(BLOCKED_PARTICLES.get());
        skipWeatherWhenLow = SKIP_WEATHER.get();
        skipCloudsWhenLow = SKIP_CLOUDS.get();
        lowFpsScale = LOW_FPS_SCALE.get();
    }

    private static Set<ResourceLocation> parseIds(String csv) {
        Set<ResourceLocation> set = new HashSet<>();
        for (String part : csv.split(",")) {
            String t = part.trim();
            if (t.isEmpty()) continue;
            ResourceLocation rl = ResourceLocation.tryParse(t);
            if (rl != null) set.add(rl);
        }
        return set;
    }
}
