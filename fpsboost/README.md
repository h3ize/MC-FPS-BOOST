# FPS Boost (NeoForge 1.21.1)

Pure performance mod. It never touches item entities, XP orbs or drops.

## Build
1. Install JDK 21.
2. This folder has no Gradle wrapper (couldn't download one offline). Either:
   - copy `gradlew`, `gradlew.bat` and the `gradle/` folder from the NeoForge MDK
     (https://github.com/NeoForgeMDK/MDK-1.21.1-ModDevGradle), or
   - run `gradle wrapper --gradle-version 8.10.2` if you have Gradle installed, or
   - just open the folder in IntelliJ IDEA and import it as a Gradle project.
3. Run `./gradlew build` -> jar appears in `build/libs/fpsboost-1.0.0.jar`.
4. Drop the jar in `mods/`. Works client-only, server-only, or both (no network/registry content).

## What it changes
Client (config/fpsboost-client.toml)
- Entity distance culling for every non-item entity (size-aware distance)
- Block entity distance culling (chests, signs, banners, modded machines); beacons/end portals exempt
- Particle budget per tick + distance culling
- Ambient block particle throttle (torch smoke, lava drips, etc.)
- Animated texture throttle (water/lava/fire/modded animated blocks)
- FPS cap while the window is unfocused
- One-time lighter video settings on first launch (biome blend 1, fast clouds, no entity shadows,
  decreased particles, entity distance 75%, simulation distance 8). Delete `config/fpsboost.applied` to re-apply.
- Adaptive culling: if FPS drops below `target_fps` (default 60), entity/block-entity/particle limits shrink
  automatically (down to `min_scale`) and recover when FPS does
- Sound limiter: max new in-world sounds per tick (UI/music never dropped)
- Rain/snow splash particle throttle
- FPS cap (30) on title screen, loading screens and pause menu
- One garbage collection when you leave a world
- Per-frame entity budget: past 200 drawn entities, ones farther than 24 blocks are skipped (nearby always drawn)
- Particle blocklist (config list of purely cosmetic spam particles: ash, spores, underwater, ambient potion swirls...)
- While FPS is low: skip rain/snow and clouds until it recovers
- `/fpsboost` client command shows how many renders/particles were skipped

Server / integrated server (config/fpsboost-common.toml)
- AI/brain throttle for mobs with no player within 96 blocks (runs 1 in 4 ticks).
  Mobs with a target, a leash or riders are exempt. Turn off in the config if anything feels odd.
- Far mobs also do their push/cramming scan and despawn check only every 4 ticks
- Adaptive server: if average MSPT climbs past 30 (75% of `high_mspt`), the far range shrinks and the AI interval grows,
  at 40 it goes to range x0.5 / interval x2, then relaxes back when the server recovers
- `/fpsboostserver` shows average MSPT, the live range/interval and how many AI/collision ticks were skipped

## Recommended JVM args (Java 21, big modpack)
-Xms6G -Xmx6G -XX:+UseZGC -XX:+ZGenerational -XX:+AlwaysPreTouch
