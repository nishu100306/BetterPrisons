package BetterPrisons.modid.chestsearch;

import BetterPrisons.modid.BetterPrisonsClient;

import java.util.List;

/**
 * One row in the no-code filter sidebar. A rule pairs a filter type with a
 * user-supplied value and an auto-assigned highlight color.
 *
 * Types:
 *   NAME          — item name/lore contains the value (text)
 *   SUCCESS_RATE  — enchant book success % comparison (e.g. ">=50")
 *   DESTROY_RATE  — enchant book destroy % comparison (e.g. "<5")
 *   ENERGY_COST   — enchant book energy cost ceiling (matches cost <= value;
 *                   value accepts k/m/b/t suffixes, e.g. "100k", "2.5m")
 *
 * Success/destroy values accept an optional leading operator (>=, <=, >, <, =);
 * with no operator they default to >=.
 */
public class ChestSearchFilterRule {

    public enum Type {
        NAME("name"),
        SUCCESS_RATE("succ% >"),
        DESTROY_RATE("dest% <"),
        ENERGY_COST("nrg cost <");

        public final String label;
        Type(String label) { this.label = label; }

        public Type next() {
            Type[] vs = values();
            return vs[(ordinal() + 1) % vs.length];
        }
    }

    public Type type = Type.NAME;
    public String value = "";
    public int color = 0x8032CD32; // lime (auto-assigned on creation)

    public boolean isActive() {
        return value != null && !value.isEmpty();
    }

    /**
     * @param book book attributes, or null if the stack is not an enchant book.
     */
    public boolean matches(String name, List<String> lore, BookAttributes book) {
        if (!isActive()) return false;
        switch (type) {
            case NAME: {
                String v = value.toLowerCase();
                if (name.toLowerCase().contains(v)) return true;
                for (String l : lore) {
                    if (l.toLowerCase().contains(v)) return true;
                }
                return false;
            }
            case SUCCESS_RATE:
                // Lower limit: match books with at least this success %.
                return book != null && compare(value, book.successPercent, ">=");
            case DESTROY_RATE:
                // Upper limit: match books with at most this destroy %.
                return book != null && compare(value, book.destroyPercent, "<=");
            case ENERGY_COST: {
                if (book == null) return false;
                // Ceiling: match books whose energy cost is at most the typed value.
                // Accepts k/m/b/t suffixes via the shared parser.
                long ceiling = BetterPrisonsClient.enchantParsing.parseFormattedNumber(value);
                if (ceiling <= 0) return false;
                return book.energyCost <= ceiling;
            }
            default:
                return false;
        }
    }

    /**
     * Parses {@code raw} as an optional comparison operator followed by a number
     * and compares it against {@code actual}. Uses {@code defaultOp} when no
     * operator is given. Commas in the number are ignored.
     */
    private static boolean compare(String raw, double actual, String defaultOp) {
        raw = raw.trim();
        if (raw.isEmpty()) return false;
        String op = defaultOp;
        int idx = 0;
        if (raw.startsWith(">=") || raw.startsWith("<=")) { op = raw.substring(0, 2); idx = 2; }
        else if (raw.startsWith(">") || raw.startsWith("<") || raw.startsWith("=")) { op = raw.substring(0, 1); idx = 1; }
        String numStr = raw.substring(idx).trim().replace(",", "").replace("%", "");
        double target;
        try { target = Double.parseDouble(numStr); }
        catch (NumberFormatException e) { return false; }
        switch (op) {
            case ">":  return actual > target;
            case "<":  return actual < target;
            case "<=": return actual <= target;
            case "=":  return Math.abs(actual - target) < 0.5;
            case ">=":
            default:   return actual >= target;
        }
    }

    /** Snapshot of an enchant book's searchable numeric attributes. */
    public static final class BookAttributes {
        public final double successPercent; // 0–100
        public final double destroyPercent; // 0–100
        public final double energyCost;     // raw required energy

        public BookAttributes(double successPercent, double destroyPercent, double energyCost) {
            this.successPercent = successPercent;
            this.destroyPercent = destroyPercent;
            this.energyCost = energyCost;
        }
    }
}
