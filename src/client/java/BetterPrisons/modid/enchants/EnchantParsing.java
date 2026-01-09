package BetterPrisons.modid.enchants;

import BetterPrisons.modid.BetterPrisonsClient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EnchantParsing {
    // Shared parsing utilities that individual enchants can use
    private static final Pattern SECONDS_PATTERN = Pattern.compile("(\\d+)\\s*(?:second|sec|s)", Pattern.CASE_INSENSITIVE);
    private static final Pattern NUMBER_PATTERN = Pattern.compile("([0-9,]+\\.?[0-9]*)\\s*([KMBT]?)", Pattern.CASE_INSENSITIVE);

    // Parse a number from a chat message (e.g., "Super Breaker activated for 5 seconds!")
    public int parseSecondsFromMessage(String message, String pattern) {
        // Try to find a number followed by "seconds", "sec", or "s"
        Matcher matcher = SECONDS_PATTERN.matcher(message);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                BetterPrisonsClient.LOGGER.warn("Failed to parse seconds from: " + message);
            }
        }
        return 0;
    }

    // Start a cooldown for an enchant (adds to CooldownHud)
    public void startCooldown(String enchantName, int durationSeconds) {
        BetterPrisonsClient.cooldownHud.addCooldown(enchantName, durationSeconds);
    }

    // Check if a chat message matches a pattern (supports simple wildcards)
    public boolean messageMatches(String message, String pattern) {
        if (pattern.isEmpty()) return false;
        // Simple contains check, or regex if pattern starts with "regex:"
        if (pattern.startsWith("regex:")) {
            try {
                return message.matches(pattern.substring(6));
            } catch (Exception e) {
                BetterPrisonsClient.LOGGER.warn("Invalid regex pattern: " + pattern);
                return false;
            }
        }
        return message.toLowerCase().contains(pattern.toLowerCase());
    }

    // Parse common formats like "1,234" or "1.2M"
    public long parseFormattedNumber(String text) {
        if (text == null || text.isEmpty()) return 0;

        // Remove whitespace
        text = text.trim();

        Matcher matcher = NUMBER_PATTERN.matcher(text);
        if (!matcher.find()) return 0;

        try {
            // Parse the base number (remove commas)
            String numberPart = matcher.group(1).replace(",", "");
            double baseNumber = Double.parseDouble(numberPart);

            // Apply multiplier suffix
            String suffix = matcher.group(2).toUpperCase();
            switch (suffix) {
                case "K":
                    return (long) (baseNumber * 1_000);
                case "M":
                    return (long) (baseNumber * 1_000_000);
                case "B":
                    return (long) (baseNumber * 1_000_000_000);
                case "T":
                    return (long) (baseNumber * 1_000_000_000_000L);
                default:
                    return (long) baseNumber;
            }
        } catch (NumberFormatException e) {
            BetterPrisonsClient.LOGGER.warn("Failed to parse number from: " + text);
            return 0;
        }
    }
}
