package BetterPrisons.modid.misc;

import BetterPrisons.modid.BetterPrisonsClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Adds a localized expiry countdown to CosmicPrisons gang point notes.
 *
 * The server-side lore prints expiry in EDT (e.g. "Expires at May. 15, 5:58 AM EDT").
 * We parse that line and append two extra tooltip lines:
 *   - Time remaining until expiry (e.g. "Expires in: 1d 25m")
 *   - Expiry localised to the user's system timezone
 */
public class GangPointTooltip {

    // Matches "Expires at May. 15, 5:58 AM EDT" — the period after month and timezone abbreviation are required.
    private static final Pattern EXPIRY_PATTERN = Pattern.compile(
        "Expires at\\s+(\\w+)\\.?\\s+(\\d+),\\s+(\\d+):(\\d+)\\s+(AM|PM)\\s+([A-Za-z]+)"
    );

    private static final Map<String, Integer> MONTHS = new HashMap<>();
    static {
        MONTHS.put("Jan", 1); MONTHS.put("Feb", 2); MONTHS.put("Mar", 3); MONTHS.put("Apr", 4);
        MONTHS.put("May", 5); MONTHS.put("Jun", 6); MONTHS.put("Jul", 7); MONTHS.put("Aug", 8);
        MONTHS.put("Sep", 9); MONTHS.put("Oct", 10); MONTHS.put("Nov", 11); MONTHS.put("Dec", 12);
    }

    private static final DateTimeFormatter LOCAL_FORMAT =
        DateTimeFormatter.ofPattern("MMM d, h:mm a zzz", Locale.US);

    public static void append(ItemStack stack, List<Text> lines) {
        try {
            if (!BetterPrisonsClient.config.gangPointExpiryEnabled) return;
            if (stack == null || stack.isEmpty()) return;

            NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);
            if (customData == null) return;

            NbtCompound nbt = customData.copyNbt();
            NbtCompound bukkit = nbt.getCompound("PublicBukkitValues").orElse(null);
            if (bukkit == null || bukkit.isEmpty()) return;

            String customItemId = bukkit.getString("cosmicprisons:custom_item_id").orElse("");
            if (!"gang_point_note".equals(customItemId)) return;

            Instant expiry = findExpiryInLore(stack);
            if (expiry == null) return;

            ZoneId localZone = ZoneId.systemDefault();
            ZonedDateTime expiryLocal = expiry.atZone(localZone);

            int rgb = BetterPrisonsClient.config.gangPointExpiryColor & 0xFFFFFF;
            Style style = Style.EMPTY.withColor(TextColor.fromRgb(rgb)).withItalic(false);

            // Line 1: countdown
            long remainingMs = expiry.toEpochMilli() - System.currentTimeMillis();
            String remainingStr = remainingMs <= 0 ? "Expired" : formatDuration(remainingMs);
            lines.add(Text.literal("[BP] Expires in: " + remainingStr).setStyle(style));

            // Line 2: localized expiry timestamp
            lines.add(Text.literal("[BP] Local: " + expiryLocal.format(LOCAL_FORMAT)).setStyle(style));
        } catch (Exception e) {
            // Tooltips must never crash the game
        }
    }

    private static Instant findExpiryInLore(ItemStack stack) {
        LoreComponent lore = stack.get(DataComponentTypes.LORE);
        if (lore == null) return null;
        for (Text line : lore.lines()) {
            Matcher m = EXPIRY_PATTERN.matcher(line.getString());
            if (m.find()) {
                return parseExpiry(m);
            }
        }
        return null;
    }

    private static Instant parseExpiry(Matcher m) {
        String monthStr = m.group(1);
        int day, hour, minute;
        try {
            day = Integer.parseInt(m.group(2));
            hour = Integer.parseInt(m.group(3));
            minute = Integer.parseInt(m.group(4));
        } catch (NumberFormatException e) {
            return null;
        }
        String ampm = m.group(5);
        String tzAbbrev = m.group(6).toUpperCase();

        Integer month = MONTHS.get(monthStr);
        if (month == null) return null;

        if ("PM".equals(ampm) && hour < 12) hour += 12;
        else if ("AM".equals(ampm) && hour == 12) hour = 0;

        // Map known US Eastern timezone abbreviations to America/New_York (handles DST automatically)
        ZoneId zone;
        switch (tzAbbrev) {
            case "EDT":
            case "EST":
                zone = ZoneId.of("America/New_York");
                break;
            case "PDT":
            case "PST":
                zone = ZoneId.of("America/Los_Angeles");
                break;
            case "CDT":
            case "CST":
                zone = ZoneId.of("America/Chicago");
                break;
            case "MDT":
            case "MST":
                zone = ZoneId.of("America/Denver");
                break;
            case "UTC":
            case "GMT":
                zone = ZoneId.of("UTC");
                break;
            default:
                zone = ZoneId.of("America/New_York"); // assume Eastern as Cosmic's default
                break;
        }

        int year = LocalDate.now(zone).getYear();
        ZonedDateTime candidate;
        try {
            candidate = ZonedDateTime.of(year, month, day, hour, minute, 0, 0, zone);
        } catch (Exception e) {
            return null;
        }

        // If parsed date is more than a day in the past, assume it's actually next year
        if (candidate.toInstant().isBefore(Instant.now().minusSeconds(86400))) {
            candidate = candidate.plusYears(1);
        }
        return candidate.toInstant();
    }

    private static String formatDuration(long ms) {
        long totalSecs = ms / 1000;
        long days = totalSecs / 86400;
        long hours = (totalSecs % 86400) / 3600;
        long minutes = (totalSecs % 3600) / 60;
        long seconds = totalSecs % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0 || days > 0) sb.append(hours).append("h ");
        if (minutes > 0 || hours > 0 || days > 0) sb.append(minutes).append("m ");
        sb.append(seconds).append("s");
        return sb.toString();
    }
}
