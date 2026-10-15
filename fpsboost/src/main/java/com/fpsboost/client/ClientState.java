package com.fpsboost.client;

/** Tiny shared state, only touched from the client/render thread. */
public final class ClientState {
    public static int tick = 0;
    public static int particlesThisTick = 0;
    public static int soundsThisTick = 0;
    public static int entitiesThisFrame = 0;
    /** 1.0 = full configured distances; shrinks when FPS is below target. */
    public static volatile double cullScale = 1.0;

    public static long culledEntities = 0;
    public static long culledBlockEntities = 0;
    public static long droppedParticles = 0;
    public static long droppedSounds = 0;

    private ClientState() {}
}
