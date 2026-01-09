package BetterPrisons.modid.devtools;

public class SoundTracker {
    private static boolean dragonSoundHeardThisTick = false;

    public static void markDragonSoundHeard() {
        dragonSoundHeardThisTick = true;
    }

    public static boolean wasDragonSoundHeard() {
        return dragonSoundHeardThisTick;
    }

    public static void clearTickCache() {
        dragonSoundHeardThisTick = false;
    }
}
