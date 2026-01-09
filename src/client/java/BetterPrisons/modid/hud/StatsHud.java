package BetterPrisons.modid.hud;

import BetterPrisons.modid.BetterPrisonsClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StatsHud extends BaseHud {
    // Current values (parsed from scoreboard)
    public long currentXP = 0;
    public long currentEnergy = 0;

    // Session tracking
    public long sessionStartXP = 0;
    public long sessionStartEnergy = 0;
    public long sessionStartTime = 0;
    public boolean trackingActive = false;

    // XP gain tracking (past minute for xp/hr calculation)
    private List<XPGain> xpGainHistory = new ArrayList<>();
    private long lastTickXP = 0;
    public long totalSessionXPGained = 0;

    // Energy gain tracking (past minute for energy/hr calculation)
    private List<EnergyGain> energyGainHistory = new ArrayList<>();
    private long lastTickEnergy = 0;
    private ItemStack lastPickaxe = ItemStack.EMPTY;
    public long totalSessionEnergyGained = 0;

    // Cached per-hour stats (updated once per second)
    private long cachedXPPerHour = 0;
    private long cachedEnergyPerHour = 0;
    private long cachedXPPerMinute = 0;
    private long cachedEnergyPerMinute = 0;
    private long lastStatsUpdateTime = 0;

    public StatsHud() {
        super("stats");
    }

    @Override
    public void tick(MinecraftClient client) {
        if (client.world == null || client.player == null) return;

        // Read scoreboard sidebar
        Scoreboard scoreboard = client.world.getScoreboard();
        parseScoreboard(scoreboard);

        // Auto-start tracking on first valid data
        if (!trackingActive && currentXP > 0) {
            startTracking();
        }

        if (trackingActive) {
            // Get current pickaxe
            ItemStack currentPickaxe = client.player.getMainHandStack();
            boolean pickaxeChanged = !ItemStack.areEqual(currentPickaxe, lastPickaxe);

            // Track XP gains (only if pickaxe hasn't changed and XP increased)
            if (!pickaxeChanged && lastTickXP > 0 && currentXP > lastTickXP) {
                long xpGain = currentXP - lastTickXP;
                long now = System.currentTimeMillis();
                xpGainHistory.add(new XPGain(xpGain, now));
                totalSessionXPGained += xpGain;
            }

            // Track energy gains (only if pickaxe hasn't changed and energy increased)
            if (!pickaxeChanged && lastTickEnergy > 0 && currentEnergy > lastTickEnergy) {
                long energyGain = currentEnergy - lastTickEnergy;
                long now = System.currentTimeMillis();
                energyGainHistory.add(new EnergyGain(energyGain, now));
                totalSessionEnergyGained += energyGain;
            }

            // Remove gains older than 1 minute
            long oneMinuteAgo = System.currentTimeMillis() - 60000;
            xpGainHistory.removeIf(gain -> gain.timestamp < oneMinuteAgo);
            energyGainHistory.removeIf(gain -> gain.timestamp < oneMinuteAgo);

            // Update tracking variables
            lastTickXP = currentXP;
            lastTickEnergy = currentEnergy;
            lastPickaxe = currentPickaxe.copy();
        }
    }

    private void parseScoreboard(Scoreboard scoreboard) {
        if (scoreboard == null) return;

        // Get the sidebar objective
        ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (objective == null) return;

        // Get all entries in the sidebar
        Collection<ScoreboardEntry> entries = scoreboard.getScoreboardEntries(objective);

        // First pass: find the "Level" line to get its value
        Integer levelValue = null;
        for (ScoreboardEntry entry : entries) {
            try {
                // Get the full display text (including team formatting)
                Text displayText = getDisplayText(scoreboard, entry);
                String stripped = displayText.getString().replaceAll("§.", "");

                if (stripped.trim().equalsIgnoreCase("Level")) {
                    levelValue = entry.value();
                    break;
                }
            } catch (Exception e) {
                // Ignore errors for this entry
            }
        }

        // Parse each line looking for XP and Energy
        for (ScoreboardEntry entry : entries) {
            try {
                // Get the full display text (including team formatting)
                Text displayText = getDisplayText(scoreboard, entry);
                String fullText = displayText.getString();
                String stripped = fullText.replaceAll("§.", "");

                // Look for Energy pattern: "(153,116 / 295,200)"
                // Pattern: (number / number) - use the first number
                if (stripped.matches(".*\\(.*\\s*/\\s*.*\\).*")) {
                    try {
                        // Extract the first number from the pattern
                        String[] parts = stripped.split("/");
                        if (parts.length >= 1) {
                            // Get the part before the slash and extract the number
                            String firstPart = parts[0].replaceAll("[^0-9,]", "");
                            long parsed = BetterPrisonsClient.enchantParsing.parseFormattedNumber(firstPart);
                            if (parsed > 0) {
                                currentEnergy = parsed;
                            }
                        }
                    } catch (Exception e) {
                        // Ignore parse errors for energy
                    }
                }

                // Look for XP pattern: "   100 (118,686,166 XP)"
                // This line has value = (Level line value - 1)
                if (levelValue != null && entry.value() == levelValue - 1) {
                    try {
                        // Extract number from pattern "(number XP)"
                        if (stripped.contains("(") && stripped.contains("XP")) {
                            // Get text between ( and XP
                            int startIdx = stripped.indexOf("(");
                            int endIdx = stripped.indexOf("XP");
                            if (startIdx != -1 && endIdx != -1 && endIdx > startIdx) {
                                String xpText = stripped.substring(startIdx + 1, endIdx).trim();
                                // Remove commas and parse
                                long parsed = BetterPrisonsClient.enchantParsing.parseFormattedNumber(xpText);
                                if (parsed > 0) {
                                    currentXP = parsed;
                                }
                            }
                        }
                    } catch (Exception e) {
                        // Ignore parse errors for XP
                    }
                }
            } catch (Exception e) {
                // Ignore errors for this entry and continue with the next one
            }
        }
    }

    private Text getDisplayText(Scoreboard scoreboard, ScoreboardEntry entry) {
        String owner = entry.owner();

        // Check for direct display override
        Text directDisplay = entry.display();
        if (directDisplay != null) {
            return directDisplay;
        }

        // Check for team formatting
        var team = scoreboard.getScoreHolderTeam(owner);
        if (team != null) {
            return Team.decorateName(team, Text.literal(owner));
        }

        return Text.literal(owner);
    }

    private long extractNumberFromLine(String line) {
        // Remove color codes (§x)
        line = line.replaceAll("§.", "");

        // Try to find numbers with common formats:
        // - "1,234,567"
        // - "1.2M"
        // - "5K"
        // - "123"

        // Split by common delimiters and look for number-like strings
        String[] words = line.split("[\\s:()\\[\\]]+");
        for (String word : words) {
            word = word.trim();
            if (word.isEmpty()) continue;

            // Check if this looks like a number (contains digits)
            if (word.matches(".*\\d+.*")) {
                // Try to parse it
                long parsed = BetterPrisonsClient.enchantParsing.parseFormattedNumber(word);
                if (parsed > 0) {
                    return parsed;
                }
            }
        }

        return 0;
    }

    public void startTracking() {
        sessionStartXP = currentXP;
        sessionStartEnergy = currentEnergy;
        sessionStartTime = System.currentTimeMillis();
        xpGainHistory.clear();
        energyGainHistory.clear();
        lastTickXP = currentXP;
        lastTickEnergy = currentEnergy;
        totalSessionXPGained = 0;
        totalSessionEnergyGained = 0;
        lastStatsUpdateTime = 0;
        cachedXPPerHour = 0;
        cachedEnergyPerHour = 0;
        cachedXPPerMinute = 0;
        cachedEnergyPerMinute = 0;
        trackingActive = true;
    }

    public void resetTracking() {
        sessionStartXP = currentXP;
        sessionStartEnergy = currentEnergy;
        sessionStartTime = System.currentTimeMillis();
        xpGainHistory.clear();
        energyGainHistory.clear();
        lastTickXP = currentXP;
        lastTickEnergy = currentEnergy;
        totalSessionXPGained = 0;
        totalSessionEnergyGained = 0;
        lastStatsUpdateTime = 0;
        cachedXPPerHour = 0;
        cachedEnergyPerHour = 0;
        cachedXPPerMinute = 0;
        cachedEnergyPerMinute = 0;
        // Keep trackingActive = true so it continues from new baseline
    }

    public long getXPPerHour() {
        return getXPPerMinute() * 60;
    }

    public long getXPPerMinute() {
        if (!trackingActive) return 0;
        if (xpGainHistory.isEmpty()) return 0;

        // Sum up all gains in history
        long totalGains = 0;
        for (XPGain gain : xpGainHistory) {
            totalGains += gain.amount;
        }

        if (totalGains == 0) return 0;

        // Calculate the actual time span of the data we have
        long now = System.currentTimeMillis();
        long oldestTimestamp = xpGainHistory.get(0).timestamp;

        // Use the time from oldest gain to now (not sessionStartTime!)
        double elapsedSeconds = (now - oldestTimestamp) / 1000.0;

        // Need at least 5 seconds of data for reasonable accuracy
        if (elapsedSeconds < 5.0) return 0;

        // Calculate rate per minute
        return (long) ((totalGains / elapsedSeconds) * 60.0);
    }

    public long getEnergyPerHour() {
        return getEnergyPerMinute() * 60;
    }

    public long getEnergyPerMinute() {
        if (!trackingActive) return 0;
        if (energyGainHistory.isEmpty()) return 0;

        // Sum up all gains in history
        long totalGains = 0;
        for (EnergyGain gain : energyGainHistory) {
            totalGains += gain.amount;
        }

        if (totalGains == 0) return 0;

        // Calculate the actual time span of the data we have
        long now = System.currentTimeMillis();
        long oldestTimestamp = energyGainHistory.get(0).timestamp;

        // Use the time from oldest gain to now (not sessionStartTime!)
        double elapsedSeconds = (now - oldestTimestamp) / 1000.0;

        // Need at least 5 seconds of data for reasonable accuracy
        if (elapsedSeconds < 5.0) return 0;

        // Calculate rate per minute
        return (long) ((totalGains / elapsedSeconds) * 60.0);
    }

    public long getTotalXPPerMinute() {
        if (!trackingActive || sessionStartTime == 0) return 0;
        long elapsed = System.currentTimeMillis() - sessionStartTime;
        if (elapsed < 1000) return 0;
        double minutes = elapsed / 60000.0;
        return (long) (totalSessionXPGained / minutes);
    }

    public long getTotalEnergyPerMinute() {
        if (!trackingActive || sessionStartTime == 0) return 0;
        long elapsed = System.currentTimeMillis() - sessionStartTime;
        if (elapsed < 1000) return 0;
        double minutes = elapsed / 60000.0;
        return (long) (totalSessionEnergyGained / minutes);
    }

    public String getSessionDuration() {
        if (sessionStartTime == 0) return "0:00:00";
        long elapsed = System.currentTimeMillis() - sessionStartTime;
        long millis = elapsed % 1000;
        long seconds = (elapsed / 1000) % 60;
        long minutes = (elapsed / 60000) % 60;
        long hours = elapsed / 3600000;
        if (BetterPrisonsClient.config.statsShowMillisOnSessionDuration) {
            return String.format("%d:%02d:%02d.%03d", hours, minutes, seconds, millis);
        }
        return String.format("%d:%02d:%02d", hours, minutes, seconds);
    }

    @Override
    public void render(DrawContext ctx, MinecraftClient client) {
        if (!enabled) return;
        Matrix3x2fStack matrices = ctx.getMatrices();
        this.scale = BetterPrisonsClient.config.statsHudScale / 100.0f;

        // Only display if player is holding a pickaxe
        if (client.player == null) return;

        boolean holdingPickaxe = false;
        String mainHandName = client.player.getMainHandStack().getName().getString().toLowerCase();
        String offHandName = client.player.getOffHandStack().getName().getString().toLowerCase();

        if (client.player.getMainHandStack().getItem().getTranslationKey().contains("pickaxe")){
            holdingPickaxe = true;
        }

        if (!holdingPickaxe) return;

        // Update cached stats once per second
        long now = System.currentTimeMillis();
        if (now - lastStatsUpdateTime >= 1000) {
            cachedXPPerHour = getXPPerHour();
            cachedEnergyPerHour = getEnergyPerHour();
            cachedXPPerMinute = getXPPerMinute();
            cachedEnergyPerMinute = getEnergyPerMinute();
            lastStatsUpdateTime = now;
        }

        // Build list of visible elements
        java.util.List<String> visibleElements = new java.util.ArrayList<>();

        if (BetterPrisonsClient.config.statsShowCurrentXP) {
            visibleElements.add("XP: " + formatNumber(currentXP));
        }
        if (BetterPrisonsClient.config.statsShowXPPerHour) {
            visibleElements.add("XP/hr: " + formatNumber(cachedXPPerHour));
        }
        if (BetterPrisonsClient.config.statsShowXPPerMinute) {
            visibleElements.add("XP/min: " + formatNumber(cachedXPPerMinute));
        }
        if (BetterPrisonsClient.config.statsShowSessionXP) {
            visibleElements.add("Session XP: " + formatNumber(totalSessionXPGained));
        }
        if (BetterPrisonsClient.config.statsShowCurrentCE) {
            visibleElements.add("CE: " + formatNumber(currentEnergy));
        }
        if (BetterPrisonsClient.config.statsShowCEPerHour) {
            visibleElements.add("CE/hr: " + formatNumber(cachedEnergyPerHour));
        }
        if (BetterPrisonsClient.config.statsShowCEPerMinute) {
            visibleElements.add("CE/min: " + formatNumber(cachedEnergyPerMinute));
        }
        if (BetterPrisonsClient.config.statsShowSessionCE) {
            visibleElements.add("Session CE: " + formatNumber(totalSessionEnergyGained));
        }
        if (BetterPrisonsClient.config.statsShowSessionDuration) {
            visibleElements.add("Session: " + getSessionDuration());
        }

        // If no elements are visible, don't render
        if (visibleElements.isEmpty()) return;

        // Calculate maximum text width from visible elements
        int maxWidth = 0;
        for (String text : visibleElements) {
            maxWidth = Math.max(maxWidth, client.textRenderer.getWidth(Text.literal(text)));
        }

        // Draw background with custom styling and dynamic width/height
        int bgWidth = scaled(maxWidth + 4);
        int bgHeight = scaled(visibleElements.size() * 12 - 2); // 12px per line, -2 for bottom padding

        // Combine RGB color with opacity to create ARGB
        int bgColor = (BetterPrisonsClient.config.statsBgOpacity << 24) | (BetterPrisonsClient.config.statsBgColor & 0xFFFFFF);
        int borderColor = (BetterPrisonsClient.config.statsBorderOpacity << 24) | (BetterPrisonsClient.config.statsBorderColor & 0xFFFFFF);
        int thickness = BetterPrisonsClient.config.statsBorderThickness;

        // Draw background
        ctx.fill(x - 2, y - 2, x + bgWidth + 2, y + bgHeight, bgColor);

        // Draw border outside the background (no overlap)
        // Top border
        ctx.fill(x - 2, y - 2 - thickness, x + bgWidth + 2, y - 2, borderColor);
        // Bottom border
        ctx.fill(x - 2, y + bgHeight, x + bgWidth + 2, y + bgHeight + thickness, borderColor);
        // Left border
        ctx.fill(x - 2 - thickness, y - 2 - thickness, x - 2, y + bgHeight + thickness, borderColor);
        // Right border
        ctx.fill(x + bgWidth + 2, y - 2 - thickness, x + bgWidth + 2 + thickness, y + bgHeight + thickness, borderColor);

        // Render all visible elements
        int yOffset = 0;
        for (int i = 0; i < visibleElements.size(); i++) {
            String text = visibleElements.get(i);

            // Determine color based on element type
            int color;
            if (text.startsWith("XP:") || text.startsWith("CE:")) {
                color = 0xFFFFFFFF; // White for current values
            } else if (text.startsWith("Session:")) {
                color = 0xFF888888; // Gray for session duration
            } else {
                color = 0xFFAAAAAA; // Light gray for other stats
            }
            matrices.pushMatrix();
            matrices.scale(scale);
            matrices.translate(x/scale, (y + yOffset)/scale);
            ctx.drawTextWithShadow(client.textRenderer, Text.literal(text), 0, 0, color);
            matrices.popMatrix();
            yOffset += scaled(12);
        }
    }

    @Override
    public int getHeight() {
        // Count visible elements
        int visibleCount = 0;
        if (BetterPrisonsClient.config.statsShowCurrentXP) visibleCount++;
        if (BetterPrisonsClient.config.statsShowXPPerHour) visibleCount++;
        if (BetterPrisonsClient.config.statsShowXPPerMinute) visibleCount++;
        if (BetterPrisonsClient.config.statsShowSessionXP) visibleCount++;
        if (BetterPrisonsClient.config.statsShowCurrentCE) visibleCount++;
        if (BetterPrisonsClient.config.statsShowCEPerHour) visibleCount++;
        if (BetterPrisonsClient.config.statsShowCEPerMinute) visibleCount++;
        if (BetterPrisonsClient.config.statsShowSessionCE) visibleCount++;
        if (BetterPrisonsClient.config.statsShowSessionDuration) visibleCount++;

        return visibleCount * 12;
    }

    private String formatNumber(long num) {
        // Check config to determine formatting style
        if (BetterPrisonsClient.config.useCommaFormatting) {
            return formatWithCommas(num);
        } else {
            return formatCompact(num);
        }
    }

    private String formatCompact(long num) {
        if (num >= 1_000_000) {
            return String.format("%.1fM", num / 1_000_000.0);
        } else if (num >= 1_000) {
            return String.format("%.1fK", num / 1_000.0);
        }
        return String.valueOf(num);
    }

    private String formatWithCommas(long num) {
        return String.format("%,d", num);
    }

    // Helper class to track XP gains with timestamps
    private static class XPGain {
        long amount;
        long timestamp;

        XPGain(long amount, long timestamp) {
            this.amount = amount;
            this.timestamp = timestamp;
        }
    }

    // Helper class to track energy gains with timestamps
    private static class EnergyGain {
        long amount;
        long timestamp;

        EnergyGain(long amount, long timestamp) {
            this.amount = amount;
            this.timestamp = timestamp;
        }
    }
}
