package BetterPrisons.modid.misc;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Calculates energy costs for pickaxe and satchel upgrades on CosmicPrisons.
 *
 * Pickaxe formula:  energy_needed(level) = slope * level - 4800
 * Satchel ore:      energy_needed(level) = slope * level - 1200
 * Satchel refined:  energy_needed(level) = slope * 1.5 * level - 1800
 *
 * Total energy to reach level n = sum from i=1 to n = slope * n*(n+1)/2 - intercept * n
 * Energy cost from startLevel to endLevel = totalEnergy(endLevel) - totalEnergy(startLevel)
 */
public class EnergyCalculator {

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.US);

    // Pickaxe slopes (energy increment per level)
    public enum PickType {
        WOOD(9_600),
        STONE(10_800),
        GOLD(12_000),
        IRON(13_200),
        DIAMOND(14_400);

        public final long slope;
        PickType(long slope) { this.slope = slope; }
    }

    // Satchel ore slopes
    public enum SatchelType {
        COAL(8_400),
        IRON(9_600),
        LAPIS(10_800),
        REDSTONE(12_000),
        GOLD(13_200),
        DIAMOND(14_400),
        EMERALD(15_600);

        public final long slope;
        SatchelType(long slope) { this.slope = slope; }
    }

    private static long pickTotalEnergy(PickType type, int level) {
        // sum_{i=1}^{n} (slope*i - 4800) = slope * n*(n+1)/2 - 4800 * n
        long n = level;
        return type.slope * n * (n + 1) / 2 - 4_800 * n;
    }

    private static long satchelOreTotalEnergy(SatchelType type, int level) {
        // sum_{i=1}^{n} (slope*i - 1200) = slope * n*(n+1)/2 - 1200 * n
        long n = level;
        return type.slope * n * (n + 1) / 2 - 1_200 * n;
    }

    private static long satchelRefinedTotalEnergy(SatchelType type, int level) {
        // refined slope = ore slope * 1.5, intercept = 1800
        // sum_{i=1}^{n} (slope*1.5*i - 1800) = slope*1.5 * n*(n+1)/2 - 1800 * n
        long n = level;
        // Use integer math: slope * 3/2 * n*(n+1)/2 = slope * 3 * n*(n+1) / 4
        // But n*(n+1) is always even, so slope * 3 * n*(n+1) / 4 is exact
        return type.slope * 3 * n * (n + 1) / 4 - 1_800 * n;
    }

    public static long calcPickEnergy(PickType type, int startLevel, int endLevel) {
        // "level A to B" means completing levels A through B-1
        // e.g. 1->2 = cost of level 1 = Energy_Needed(1)
        return pickTotalEnergy(type, endLevel - 1) - pickTotalEnergy(type, startLevel - 1);
    }

    public static long calcSatchelOreEnergy(SatchelType type, int startLevel, int endLevel) {
        return satchelOreTotalEnergy(type, endLevel - 1) - satchelOreTotalEnergy(type, startLevel - 1);
    }

    public static long calcSatchelRefinedEnergy(SatchelType type, int startLevel, int endLevel) {
        return satchelRefinedTotalEnergy(type, endLevel - 1) - satchelRefinedTotalEnergy(type, startLevel - 1);
    }

    public static String formatEnergy(long energy) {
        return NUMBER_FORMAT.format(energy);
    }
}
