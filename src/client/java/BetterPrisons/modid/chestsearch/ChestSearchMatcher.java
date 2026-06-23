package BetterPrisons.modid.chestsearch;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Decides the highlight color for a slot's item stack. Filter-rule matches take
 * priority when the sidebar is open and has active rules; otherwise the simple
 * text query is used.
 */
public final class ChestSearchMatcher {
    /** Default highlight color: 50% alpha lime green (ARGB). */
    public static final int DEFAULT_COLOR = 0x8032CD32;
    /** Sentinel meaning "no highlight". */
    public static final int NO_MATCH = 0;

    private ChestSearchMatcher() {}

    public static int matchColor(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return NO_MATCH;

        // No-code sidebar rules take priority when the sidebar is open AND has
        // at least one rule with a non-empty value.
        if (ChestSearchFilterState.sidebarOpen && ChestSearchFilterState.hasActiveRules()) {
            String name = stack.getName().getString();
            return ChestSearchFilterState.evaluate(name, loreLines(stack), bookAttributes(stack));
        }

        String query = ChestSearchState.query;
        if (query == null || query.isEmpty()) return NO_MATCH;
        return matchesSimple(stack, query) ? DEFAULT_COLOR : NO_MATCH;
    }

    private static boolean matchesSimple(ItemStack stack, String query) {
        String q = query.toLowerCase();
        if (stack.getName().getString().toLowerCase().contains(q)) return true;
        for (String line : loreLines(stack)) {
            if (line.toLowerCase().contains(q)) return true;
        }
        return false;
    }

    private static List<String> loreLines(ItemStack stack) {
        List<String> lines = new ArrayList<>();
        LoreComponent lore = stack.get(DataComponentTypes.LORE);
        if (lore != null) {
            for (Text l : lore.lines()) lines.add(l.getString());
        }
        return lines;
    }

    /**
     * Extracts enchant-book attributes (success %, destroy %, energy cost) from
     * the stack's custom data, or null if it isn't a gear enchant book.
     */
    private static ChestSearchFilterRule.BookAttributes bookAttributes(ItemStack stack) {
        try {
            NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);
            if (customData == null) return null;
            NbtCompound bukkit = customData.copyNbt().getCompound("PublicBukkitValues").orElse(null);
            if (bukkit == null || bukkit.isEmpty()) return null;
            if (!"gear_enchant_book".equals(bukkit.getString("cosmicprisons:custom_item_id").orElse(""))) {
                return null;
            }
            double success = bukkit.getDouble("cosmicprisons:gear_enchant_success").orElse(0.0) * 100.0;
            double destroy = bukkit.getDouble("cosmicprisons:gear_enchant_destroy").orElse(0.0) * 100.0;
            double energy = bukkit.getDouble("cosmicprisons:gear_enchant_required").orElse(0.0);
            return new ChestSearchFilterRule.BookAttributes(success, destroy, energy);
        } catch (Exception e) {
            return null;
        }
    }
}
