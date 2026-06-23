package BetterPrisons.modid.misc;

import BetterPrisons.modid.BetterPrisonsClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Adds upgrade-cost tooltip lines to CosmicPrisons enchant books.
 *
 * Cost formula: original × lvl² × tierMultiplier(tier, lvl)
 *
 * Derived empirically from in-game data:
 *   - L2 always costs original × 4 regardless of tier
 *   - L3+ costs original × lvl² × tierMultiplier
 *   - tierMultiplier is 1 for COMMON/UNCOMMON/RARE/ULTIMATE
 *   - tierMultiplier is 3 for LEGENDARY (and unknown higher tiers, defensively)
 *
 * Cosmic Prisons tier order: COMMON < UNCOMMON < RARE < ULTIMATE < LEGENDARY
 */
public class EnchantBookTooltip {

    private static final Pattern MAX_LEVEL_PATTERN = Pattern.compile("Max Level:\\s*([IVXLCDM]+)");
    // Current level from the display name, e.g. "Lifesteal I (28%)" -> "I".
    private static final Pattern NAME_LEVEL_PATTERN = Pattern.compile("\\s([IVXLCDM]+)\\s*\\(\\s*\\d+\\s*%\\s*\\)");
    // Energy line, e.g. "Energy: 0 / 211,740" or "(0 / 211,740)" -> second number.
    private static final Pattern ENERGY_LINE_PATTERN = Pattern.compile("(\\d[\\d,]*)\\s*/\\s*(\\d[\\d,]*)");
    private static final int GOLD_RGB = 0xFFAA00; // LEGENDARY enchant name color

    public static void append(ItemStack stack, List<Text> lines) {
        try {
            if (!BetterPrisonsClient.config.enchantBookCostsEnabled) return;
            if (stack == null || stack.isEmpty()) return;

            int currentLevel = 0;
            double currentRequired = 0;
            String tier = "";

            // Preferred source: custom_data NBT (present on inventory items).
            NbtCompound bukkit = getBukkit(stack);
            if (bukkit != null && "gear_enchant_book".equals(bukkit.getString("cosmicprisons:custom_item_id").orElse(""))) {
                currentLevel = bukkit.getInt("cosmicprisons:gear_enchant_level").orElse(0);
                currentRequired = bukkit.getDouble("cosmicprisons:gear_enchant_required").orElse(0.0);
                tier = bukkit.getString("cosmicprisons:gear_enchant_tier").orElse("");
            }

            // Fall back to the visible lore whenever the NBT path didn't yield usable
            // data — covers chat-hovered books and items whose custom_data was trimmed.
            if (currentLevel < 1 || currentRequired <= 0) {
                if (!looksLikeEnchantBook(stack)) return;
                currentLevel = parseCurrentLevelFromName(stack);
                currentRequired = parseRequiredEnergyFromLore(stack);
                // Tier isn't in the lore; a gold enchant name means LEGENDARY (the only ×3 tier).
                if (tier.isEmpty()) tier = isGoldName(stack) ? "LEGENDARY" : "RARE";
            }

            if (currentLevel < 1 || currentRequired <= 0) return;

            int maxLevel = parseMaxLevel(stack);
            if (maxLevel <= currentLevel) return;

            // Back-calculate the L1 base cost from the current level's required energy.
            // required(L) = base × L² × tierMultiplier(tier, L)
            long currentMult = tierMultiplier(tier, currentLevel);
            double divisor = (double) currentLevel * currentLevel * currentMult;
            long baseCost = Math.round(currentRequired / divisor);

            int rgb = BetterPrisonsClient.config.enchantBookCostsColor & 0xFFFFFF;
            Style style = Style.EMPTY.withColor(TextColor.fromRgb(rgb)).withItalic(false);

            lines.add(Text.literal("[BP] Base cost (L1): " + formatNumber(baseCost))
                .setStyle(style));

            long total = 0;
            for (int lvl = currentLevel + 1; lvl <= maxLevel; lvl++) {
                long applyCost = baseCost * (long) lvl * (long) lvl * tierMultiplier(tier, lvl);
                total += applyCost;
                lines.add(Text.literal("[BP] → L" + lvl
                        + " Apply " + formatNumber(applyCost)
                        + " | Total " + formatNumber(total))
                    .setStyle(style));
            }
        } catch (Exception e) {
            // Tooltip code must never crash the game — silently ignore
        }
    }

    /**
     * Returns the deep-level cost multiplier for a given tier and target level.
     * L2 is always 1× regardless of tier; L3+ varies.
     */
    private static int tierMultiplier(String tier, int lvl) {
        if (lvl <= 2) return 1;
        switch (tier.toUpperCase()) {
            case "COMMON":
            case "UNCOMMON":
            case "RARE":
            case "ELITE":
            case "ULTIMATE":
                return 1;
            case "LEGENDARY":
                return 3;
            default:
                // Unknown / future tiers — default to 3 (high-tier pattern)
                return 3;
        }
    }

    /**
     * Appends cost lines to a chat hover's SHOW_TEXT body when it looks like an enchant
     * book. Chat hovers carry no item — only pre-rendered text — so everything is parsed
     * from the hover text. Tier (for the ×3 LEGENDARY multiplier) is inferred from a gold
     * color on the first line (the enchant name); other tiers use ×1.
     */
    public static Text appendChatHoverCost(Text hoverText) {
        try {
            if (hoverText == null || !BetterPrisonsClient.config.enchantBookCostsEnabled) return hoverText;
            String full = hoverText.getString();
            if (!full.contains("Success Rate") || !full.contains("Max Level:")) return hoverText;

            String[] lines = full.split("\n");
            int currentLevel = 0;
            double currentRequired = 0;
            int maxLevel = 0;
            for (String l : lines) {
                if (currentLevel == 0) {
                    Matcher m = NAME_LEVEL_PATTERN.matcher(l);
                    if (m.find()) currentLevel = parseRoman(m.group(1));
                }
                if (currentRequired <= 0) {
                    Matcher m = ENERGY_LINE_PATTERN.matcher(l);
                    if (m.find()) {
                        try { currentRequired = Double.parseDouble(m.group(2).replace(",", "")); }
                        catch (NumberFormatException ignored) {}
                    }
                }
                if (maxLevel == 0) {
                    Matcher m = MAX_LEVEL_PATTERN.matcher(l);
                    if (m.find()) maxLevel = parseRoman(m.group(1));
                }
            }
            if (currentLevel < 1 || currentRequired <= 0 || maxLevel <= currentLevel) return hoverText;

            String tier = firstLineGold(hoverText) ? "LEGENDARY" : "RARE";
            long currentMult = tierMultiplier(tier, currentLevel);
            long baseCost = Math.round(currentRequired / ((double) currentLevel * currentLevel * currentMult));

            int rgb = BetterPrisonsClient.config.enchantBookCostsColor & 0xFFFFFF;
            Style style = Style.EMPTY.withColor(TextColor.fromRgb(rgb)).withItalic(false);

            MutableText result = hoverText.copy();
            result.append(Text.literal("\n[BP] Base cost (L1): " + formatNumber(baseCost)).setStyle(style));
            long total = 0;
            for (int lvl = currentLevel + 1; lvl <= maxLevel; lvl++) {
                long applyCost = baseCost * (long) lvl * (long) lvl * tierMultiplier(tier, lvl);
                total += applyCost;
                result.append(Text.literal("\n[BP] → L" + lvl
                        + " Apply " + formatNumber(applyCost)
                        + " | Total " + formatNumber(total)).setStyle(style));
            }
            return result;
        } catch (Exception e) {
            return hoverText;
        }
    }

    /** True if a gold-colored segment appears on the first line (the enchant name → LEGENDARY). */
    private static boolean firstLineGold(Text text) {
        Optional<Boolean> result = text.visit((style, str) -> {
            String seg = str;
            boolean endOfLine = false;
            int nl = seg.indexOf('\n');
            if (nl >= 0) { seg = seg.substring(0, nl); endOfLine = true; }
            TextColor c = style.getColor();
            if (c != null && c.getRgb() == GOLD_RGB && !seg.trim().isEmpty()) return Optional.of(Boolean.TRUE);
            if (endOfLine) return Optional.of(Boolean.FALSE);
            return Optional.empty();
        }, Style.EMPTY);
        return result.orElse(false);
    }

    private static NbtCompound getBukkit(ItemStack stack) {
        NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData == null) return null;
        NbtCompound bukkit = customData.copyNbt().getCompound("PublicBukkitValues").orElse(null);
        return (bukkit == null || bukkit.isEmpty()) ? null : bukkit;
    }

    /** True if the stack is an enchanted book whose lore looks like a Cosmic gear enchant. */
    private static boolean looksLikeEnchantBook(ItemStack stack) {
        if (!stack.isOf(Items.ENCHANTED_BOOK)) return false;
        LoreComponent lore = stack.get(DataComponentTypes.LORE);
        if (lore == null) return false;
        boolean hasSuccess = false, hasMax = false;
        for (Text line : lore.lines()) {
            String s = line.getString();
            if (s.contains("Success Rate")) hasSuccess = true;
            if (s.contains("Max Level:")) hasMax = true;
        }
        return hasSuccess && hasMax;
    }

    private static int parseCurrentLevelFromName(ItemStack stack) {
        Matcher m = NAME_LEVEL_PATTERN.matcher(stack.getName().getString());
        return m.find() ? parseRoman(m.group(1)) : 0;
    }

    private static double parseRequiredEnergyFromLore(ItemStack stack) {
        LoreComponent lore = stack.get(DataComponentTypes.LORE);
        if (lore == null) return 0;
        for (Text line : lore.lines()) {
            Matcher m = ENERGY_LINE_PATTERN.matcher(line.getString());
            if (m.find()) {
                try {
                    return Double.parseDouble(m.group(2).replace(",", ""));
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }
        return 0;
    }

    /** True if the enchant name (or one of its parts) is gold-colored (LEGENDARY tier). */
    private static boolean isGoldName(ItemStack stack) {
        Text name = stack.getName();
        TextColor c = name.getStyle().getColor();
        if (c != null && c.getRgb() == GOLD_RGB) return true;
        for (Text sibling : name.getSiblings()) {
            TextColor sc = sibling.getStyle().getColor();
            if (sc != null && sc.getRgb() == GOLD_RGB) return true;
        }
        return false;
    }

    private static int parseMaxLevel(ItemStack stack) {
        LoreComponent lore = stack.get(DataComponentTypes.LORE);
        if (lore == null) return 0;
        for (Text line : lore.lines()) {
            Matcher m = MAX_LEVEL_PATTERN.matcher(line.getString());
            if (m.find()) {
                return parseRoman(m.group(1));
            }
        }
        return 0;
    }

    private static int parseRoman(String s) {
        int result = 0, prev = 0;
        for (int i = s.length() - 1; i >= 0; i--) {
            int val = romanValue(s.charAt(i));
            if (val == 0) return 0;
            if (val < prev) result -= val;
            else result += val;
            prev = val;
        }
        return result;
    }

    private static int romanValue(char c) {
        switch (c) {
            case 'I': return 1;
            case 'V': return 5;
            case 'X': return 10;
            case 'L': return 50;
            case 'C': return 100;
            case 'D': return 500;
            case 'M': return 1000;
            default:  return 0;
        }
    }

    private static String formatNumber(long n) {
        return String.format("%,d", n);
    }
}
