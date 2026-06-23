package BetterPrisons.modid.chestsearch;

import java.util.ArrayList;
import java.util.List;

/**
 * Static state for the no-code filter sidebar. When {@link #sidebarOpen} is
 * true and at least one rule has a non-empty value, these rules drive the
 * highlight color (taking priority over the inline search-bar query).
 *
 * Each rule is auto-assigned a distinct color from {@link #COLOR_VALUES} so
 * multiple rules stay visually distinguishable — there is no UI to change it.
 */
public final class ChestSearchFilterState {
    public static final String[] COLOR_NAMES = {
            "lime", "red", "orange", "yellow", "cyan", "blue", "purple", "pink", "white"
    };
    public static final int[] COLOR_VALUES = {
            0x8032CD32, // lime
            0x80FF3030, // red
            0x80FFA500, // orange
            0x80FFFF40, // yellow
            0x8030E0E0, // cyan
            0x804080FF, // blue
            0x80A040E0, // purple
            0x80FF80C0, // pink
            0x80FFFFFF  // white
    };
    /** Maximum simultaneous rules (UI cap). */
    public static final int MAX_RULES = 8;

    public static boolean sidebarOpen = false;
    /** false = OR (any active rule matches), true = AND (all active rules must match). */
    public static boolean matchAll = false;
    public static final List<ChestSearchFilterRule> rules = new ArrayList<>();

    private ChestSearchFilterState() {}

    public static boolean hasActiveRules() {
        for (ChestSearchFilterRule r : rules) if (r.isActive()) return true;
        return false;
    }

    public static void addRule() {
        if (rules.size() >= MAX_RULES) return;
        ChestSearchFilterRule r = new ChestSearchFilterRule();
        r.color = COLOR_VALUES[rules.size() % COLOR_VALUES.length];
        rules.add(r);
    }

    public static void removeRule(int index) {
        if (index < 0 || index >= rules.size()) return;
        rules.remove(index);
    }

    public static int nextColor(int current) {
        for (int i = 0; i < COLOR_VALUES.length; i++) {
            if (COLOR_VALUES[i] == current) {
                return COLOR_VALUES[(i + 1) % COLOR_VALUES.length];
            }
        }
        return COLOR_VALUES[0];
    }

    public static String colorName(int color) {
        for (int i = 0; i < COLOR_VALUES.length; i++) {
            if (COLOR_VALUES[i] == color) return COLOR_NAMES[i];
        }
        return "?";
    }

    /**
     * Returns a highlight color or 0 for no match.
     * <p>OR mode: the first matching active rule's color.
     * <p>AND mode: the first active rule's color, but only if EVERY active rule
     * matches; otherwise 0.
     */
    public static int evaluate(String name, List<String> lore, ChestSearchFilterRule.BookAttributes book) {
        if (matchAll) {
            int firstColor = 0;
            boolean anyActive = false;
            for (ChestSearchFilterRule r : rules) {
                if (!r.isActive()) continue;
                anyActive = true;
                if (!r.matches(name, lore, book)) return 0; // one active rule fails → no match
                if (firstColor == 0) firstColor = r.color;
            }
            return anyActive ? firstColor : 0;
        }
        for (ChestSearchFilterRule r : rules) {
            if (r.matches(name, lore, book)) return r.color;
        }
        return 0;
    }
}
