package com.fpsboost.server;

import com.fpsboost.config.CommonConfig;

/** Live (adaptive) server throttle values. */
public final class ServerState {
    public static volatile double rangeScale = 1.0;
    public static volatile double intervalMult = 1.0;
    public static volatile double avgMspt = 0.0;
    public static long throttledAiTicks = 0;
    public static long throttledCollisionTicks = 0;

    private ServerState() {}

    public static int range() {
        return Math.max(24, (int) (CommonConfig.mobThrottleRange * rangeScale));
    }

    public static int interval() {
        return Math.max(1, Math.min(20, (int) Math.round(CommonConfig.mobThrottleInterval * intervalMult)));
    }
}
