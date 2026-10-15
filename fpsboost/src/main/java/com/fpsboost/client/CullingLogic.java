package com.fpsboost.client;

import com.fpsboost.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;

public final class CullingLogic {
    private CullingLogic() {}

    /** Entities this mod never hides: drops, players, bosses, riders, glowing things, the camera entity. */
    public static boolean isExempt(Entity entity) {
        // Never touch item entities / xp orbs (this mod has nothing to do with drops).
        if (entity instanceof ItemEntity || entity instanceof ExperienceOrb) return true;
        if (entity instanceof Player || entity instanceof EnderDragon
                || entity instanceof WitherBoss || entity instanceof FishingHook) return true;
        if (entity.isVehicle() || entity.isPassenger() || entity.isCurrentlyGlowing()) return true;
        return entity == Minecraft.getInstance().getCameraEntity();
    }

    /** @return true if this entity should NOT be rendered this frame (pure distance cull). */
    public static boolean shouldCull(Entity entity, double distanceSq) {
        if (!ClientConfig.entityCulling) return false;
        if (isExempt(entity)) return false;

        double size = Math.max(entity.getBbWidth(), entity.getBbHeight());
        double limit = (ClientConfig.entityCullDistance + size * ClientConfig.entityCullSizeFactor) * ClientState.cullScale;
        return distanceSq > limit * limit;
    }

    /**
     * Called only for entities that are about to be drawn. Counts them, and once the per-frame budget is used up
     * hides the ones beyond the "near" distance. Near entities are always drawn.
     */
    public static boolean overBudget(Entity entity, double distanceSq) {
        if (!ClientConfig.entityBudget) return false;
        if (isExempt(entity)) return false;

        ClientState.entitiesThisFrame++;
        double near = ClientConfig.budgetNearDistance;
        if (distanceSq <= near * near) return false;

        int max = (int) (ClientConfig.maxEntitiesPerFrame * ClientState.cullScale);
        return ClientState.entitiesThisFrame > max;
    }
}
