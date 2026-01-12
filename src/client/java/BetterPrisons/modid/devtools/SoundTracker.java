package BetterPrisons.modid.devtools;

public class SoundTracker {
    private static boolean dragonSoundHeardThisTick = false;
    private static boolean witherShootSoundHeardThisTick = false;

    public static void markDragonSoundHeard() {
        dragonSoundHeardThisTick = true;
    }

    public static boolean wasDragonSoundHeard() {
        return dragonSoundHeardThisTick;
    }

    public static void markWitherShootSoundHeard() {
        witherShootSoundHeardThisTick = true;
    }

    public static boolean wasWitherShootSoundHeard() {
        return witherShootSoundHeardThisTick;
    }

    public static void clearTickCache() {
        dragonSoundHeardThisTick = false;
        witherShootSoundHeardThisTick = false;
    }
}
