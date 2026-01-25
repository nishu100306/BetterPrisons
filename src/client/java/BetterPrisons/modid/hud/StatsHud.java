package BetterPrisons.modid.hud;

import BetterPrisons.modid.BetterPrisonsClient;
import BetterPrisons.modid.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class StatsHud extends BaseHud {
    // Current values (parsed from scoreboard)
    public long currentXP = 0;
    public long currentEnergy = 0;
    public long xpNeededForNextLevel = 0;
    public int targetLevel = 0;

    // Session tracking
    public long sessionStartXP = 0;
    public long sessionStartEnergy = 0;
    public long sessionStartTime = 0;
    public boolean trackingActive = false;
    public boolean paused = false;
    public long totalPauseDuration = 0; // Total time spent paused (in milliseconds)
    public long pauseStartTime = 0; // When the current pause started

    // XP gain tracking (past minute for xp/hr calculation)
    private List<XPGain> xpGainHistory = new ArrayList<>();
    private long lastTickXP = 0;
    public long totalSessionXPGained = 0;

    // Energy reading tracking (past minute for energy/hr calculation)
    private List<EnergyReading> energyReadings = new ArrayList<>();
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

        // Get current pickaxe and parse energy
        ItemStack currentPickaxe = client.player.getMainHandStack();
        long currentEnergyFromPickaxe = parseEnergyFromPickaxe(currentPickaxe);
        if (currentEnergyFromPickaxe > 0) {
            currentEnergy = currentEnergyFromPickaxe;
        }

        // Auto-start tracking on first valid data (XP or Energy)
        if (!trackingActive && (currentXP > 0 || currentEnergy > 0)) {
            startTracking();
        }

        if (trackingActive) {
            if (paused) {
                // If paused, skip tracking updates
                return;
            }
            boolean pickaxeChanged = !ItemStack.areEqual(currentPickaxe, lastPickaxe);

            // Track XP gains (only if pickaxe hasn't changed and XP increased)
            if (!pickaxeChanged && lastTickXP > 0 && currentXP > lastTickXP) {
                long xpGain = currentXP - lastTickXP;
                long now = System.currentTimeMillis();
                xpGainHistory.add(new XPGain(xpGain, now));
                totalSessionXPGained += xpGain;
            }

            // Track energy readings
            if (currentEnergyFromPickaxe > 0) {
                long now = System.currentTimeMillis();

                // Check if energy decreased compared to last reading
                boolean energyDecreased = false;
                if (!energyReadings.isEmpty()) {
                    EnergyReading lastReading = energyReadings.get(energyReadings.size() - 1);
                    if (currentEnergy < lastReading.energy) {
                        energyDecreased = true;
                        // Energy went down - discard all prior data but keep session totals
                        energyReadings.clear();
                        // Don't add the current reading - skip this tick's data point
                    }
                }

                // Only add reading if energy didn't decrease
                if (!energyDecreased) {
                    // Add new reading
                    energyReadings.add(new EnergyReading(currentEnergy, now));

                    // Remove readings older than 1 minute
                    long oneMinuteAgo = now - 60000;
                    energyReadings.removeIf(reading -> reading.timestamp < oneMinuteAgo);

                    // Track session energy gain if we have previous data
                    if (energyReadings.size() >= 2) {
                        EnergyReading previousReading = energyReadings.get(energyReadings.size() - 2);
                        EnergyReading currentReading = energyReadings.get(energyReadings.size() - 1);
                        if (currentReading.energy > previousReading.energy) {
                            // Energy increased, track gain for session stats
                            long energyGain = currentReading.energy - previousReading.energy;
                            totalSessionEnergyGained += energyGain;
                        }
                    }
                }
            }

            // Remove XP gains older than 1 minute
            long oneMinuteAgo = System.currentTimeMillis() - 60000;
            xpGainHistory.removeIf(gain -> gain.timestamp < oneMinuteAgo);

            // Update tracking variables
            lastTickXP = currentXP;
            lastPickaxe = currentPickaxe.copy();
        }
    }

    /**
     * Parse energy from pickaxe lore
     * Looks for "Cosmic Energy" line, then reads the value 2 lines below
     * Format: "(3,079 / 859,200)" where first number is current energy
     * Returns -1 if not found or error parsing
     */
    private long parseEnergyFromPickaxe(ItemStack pickaxe) {
        if (pickaxe == null || pickaxe.isEmpty()) return -1;

        try {
            net.minecraft.component.type.LoreComponent lore = pickaxe.get(net.minecraft.component.DataComponentTypes.LORE);
            if (lore == null || lore.lines().isEmpty()) return -1;

            // Convert lore to list of plain text strings
            List<String> loreLines = new ArrayList<>();
            for (Text text : lore.lines()) {
                String line = text.getString().replaceAll("§.", "");
                loreLines.add(line);
            }

            // Find "Cosmic Energy" line
            for (int i = 0; i < loreLines.size(); i++) {
                String line = loreLines.get(i);
                if (line.contains("Cosmic Energy")) {
                    // Look 2 lines below
                    int energyLineIndex = i + 2;
                    if (energyLineIndex < loreLines.size()) {
                        String energyLine = loreLines.get(energyLineIndex);

                        // Parse format: "(3,079 / 859,200)"
                        // Extract the first number
                        if (energyLine.contains("(") && energyLine.contains("/")) {
                            int startIdx = energyLine.indexOf("(");
                            int slashIdx = energyLine.indexOf("/");
                            if (startIdx != -1 && slashIdx != -1 && slashIdx > startIdx) {
                                String energyText = energyLine.substring(startIdx + 1, slashIdx).trim();
                                // Parse using the existing number parser
                                long parsed = BetterPrisonsClient.enchantParsing.parseFormattedNumber(energyText);
                                if (parsed > 0) {
                                    return parsed;
                                }
                            }
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            // Ignore parse errors
        }

        return -1;
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

                if (stripped.trim().startsWith("Level")) {
                    levelValue = entry.value();
                    break;
                }
            } catch (Exception e) {
                // Ignore errors for this entry
            }
        }

        // Parse each line looking for XP
        for (ScoreboardEntry entry : entries) {
            try {
                // Get the full display text (including team formatting)
                Text displayText = getDisplayText(scoreboard, entry);
                String fullText = displayText.getString();
                String stripped = fullText.replaceAll("§.", "");

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

        // Find "Progress" line
        Integer progressValue = null;
        for (ScoreboardEntry entry : entries) {
            try {
                Text displayText = getDisplayText(scoreboard, entry);
                String stripped = displayText.getString().replaceAll("§.", "");

                if (stripped.trim().equals("Progress")) {
                    progressValue = entry.value();
                    break;
                }
            } catch (Exception e) {
                // Ignore errors
            }
        }

        // Parse XP needed and target level (line with value = progressValue + 1)
        if (progressValue != null) {

            for (ScoreboardEntry entry : entries) {
                try {
                    if (entry.value() == progressValue - 1) {
                        Text displayText = getDisplayText(scoreboard, entry);
                        String stripped = displayText.getString().replaceAll("§.", "").strip();
                        //BetterPrisonsClient.LOGGER.info(stripped);

                        // Format: "71,110,189 (§a0%§7) to §f81§"
                        // Extract first number (XP needed)
                        String[] parts = stripped.split(" ");
                        BetterPrisonsClient.LOGGER.info(Arrays.deepToString(parts));
                        if (parts.length > 0) {
                            long xpNeeded = BetterPrisonsClient.enchantParsing.parseFormattedNumber(parts[0]);
                            if (xpNeeded > 0) {
                                xpNeededForNextLevel = xpNeeded;
                            }
                        }

                        // Extract target level (last number after "to")
                        if (stripped.contains("to")) {
                            String afterTo = stripped.substring(stripped.indexOf("to") + 2).trim();
                            String levelStr = afterTo.replaceAll("[^0-9]", "");
                            if (!levelStr.isEmpty()) {
                                targetLevel = Integer.parseInt(levelStr);
                            }
                        }
                        break;
                    }
                } catch (Exception e) {
                    // Ignore parse errors
                }
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
        energyReadings.clear();
        lastTickXP = currentXP;
        totalSessionXPGained = 0;
        totalSessionEnergyGained = 0;
        lastStatsUpdateTime = 0;
        cachedXPPerHour = 0;
        cachedEnergyPerHour = 0;
        cachedXPPerMinute = 0;
        cachedEnergyPerMinute = 0;
        totalPauseDuration = 0;
        pauseStartTime = 0;
        trackingActive = true;
    }

    public void resetTracking() {
        sessionStartXP = currentXP;
        sessionStartEnergy = currentEnergy;
        sessionStartTime = System.currentTimeMillis();
        xpGainHistory.clear();
        energyReadings.clear();
        lastTickXP = currentXP;
        totalSessionXPGained = 0;
        totalSessionEnergyGained = 0;
        lastStatsUpdateTime = sessionStartTime; // Set to sessionStartTime instead of 0 to avoid negative elapsed time when paused
        cachedXPPerHour = 0;
        cachedEnergyPerHour = 0;
        cachedXPPerMinute = 0;
        cachedEnergyPerMinute = 0;
        totalPauseDuration = 0;
        pauseStartTime = 0;
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
        if (energyReadings.size() < 2) return 0;

        // Get oldest and newest readings
        EnergyReading oldestReading = energyReadings.get(0);
        EnergyReading newestReading = energyReadings.get(energyReadings.size() - 1);

        // Calculate energy gained
        long energyGained = newestReading.energy - oldestReading.energy;
        if (energyGained <= 0) return 0;

        // Calculate time elapsed in seconds
        double elapsedSeconds = (newestReading.timestamp - oldestReading.timestamp) / 1000.0;

        // Need at least 5 seconds of data for reasonable accuracy
        if (elapsedSeconds < 5.0) return 0;

        // Calculate rate per minute
        return (long) ((energyGained / elapsedSeconds) * 60.0);
    }

    public String getSessionDuration() {
        if (sessionStartTime == 0) return "0:00:00";

        // Calculate total elapsed time minus pause duration
        long totalElapsed = System.currentTimeMillis() - sessionStartTime;
        long elapsed = totalElapsed - totalPauseDuration;

        // If currently paused, don't include the current pause duration yet
        if (paused && pauseStartTime > 0) {
            long currentPauseDuration = System.currentTimeMillis() - pauseStartTime;
            elapsed -= currentPauseDuration;
        }

        long millis = elapsed % 1000;
        long seconds = (elapsed / 1000) % 60;
        long minutes = (elapsed / 60000) % 60;
        long hours = elapsed / 3600000;
        if (BetterPrisonsClient.config.statsShowMillisOnSessionDuration) {
            return String.format("%d:%02d:%02d.%03d", hours, minutes, seconds, millis);
        }
        return String.format("%d:%02d:%02d", hours, minutes, seconds);
    }

    public void togglePause() {
        if (paused) {
            // Unpausing: calculate how long we were paused and add to total
            if (pauseStartTime > 0) {
                long pauseDuration = System.currentTimeMillis() - pauseStartTime;
                totalPauseDuration += pauseDuration;
                pauseStartTime = 0;
            }
            paused = false;
        } else {
            // Pausing: record when pause started
            pauseStartTime = System.currentTimeMillis();
            paused = true;
        }
    }

    public String getTimeTillLevelUp() {
        if (!trackingActive || cachedXPPerMinute == 0 || xpNeededForNextLevel == 0) {
            return "Time until lvl " + targetLevel + ": --:--";
        }

        // Calculate minutes needed
        double minutesNeeded = (double) xpNeededForNextLevel / cachedXPPerMinute;

        // Convert to hours:minutes:seconds
        long totalSeconds = (long) (minutesNeeded * 60);
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        // Only show hours if >= 1 hour
        if (hours >= 1) {
            return String.format("Time until lvl %d: %d:%02d:%02d", targetLevel, hours, minutes, seconds);
        } else {
            return String.format("Time until lvl %d: %02d:%02d", targetLevel, minutes, seconds);
        }
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
            if (!paused) {
                cachedXPPerHour = getXPPerHour();
                cachedEnergyPerHour = getEnergyPerHour();
                cachedXPPerMinute = getXPPerMinute();
                cachedEnergyPerMinute = getEnergyPerMinute();
                lastStatsUpdateTime = now;
            }
        }

        boolean showTitle = BetterPrisonsClient.config.showStatsHudTitle;

        // Build list of visible elements
        java.util.List<String> visibleElements = new java.util.ArrayList<>();

        if (BetterPrisonsClient.config.statsShowCurrentXP) {
            visibleElements.add("XP: " + formatNumber(currentXP));
        }
        if (BetterPrisonsClient.config.statsShowTimeTillLevelUp) {
            visibleElements.add(getTimeTillLevelUp());
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
            String sessionText = "Session: ";
            if (paused) {
                sessionText += "(P) ";
            }
            sessionText += getSessionDuration();
            visibleElements.add(sessionText);
        }

        boolean hasContent = !visibleElements.isEmpty();

        // If no title and no elements are visible, don't render
        if (!showTitle && !hasContent) return;

        // Calculate title dimensions
        int titleHeight = 0;
        int titleWidth = 0;
        if (showTitle) {
            Text titleText = Text.literal("Stats HUD").setStyle(Style.EMPTY.withUnderline(true).withBold(true));
            titleWidth = (int)(client.textRenderer.getWidth(titleText) * scale);
            titleHeight = scaled(12); // Text height + spacing
        }

        // Calculate maximum text width from visible elements
        int maxWidth = titleWidth;
        if (hasContent) {
            for (String text : visibleElements) {
                int textWidth = (int)(client.textRenderer.getWidth(Text.literal(text)) * scale);
                maxWidth = Math.max(maxWidth, textWidth);
            }
        }

        // Draw background with custom styling and dynamic width/height
        int bgWidth = scaled((int)(maxWidth/scale) + 4);
        int contentHeight = hasContent ? scaled(visibleElements.size() * 12 - 2) : 0; // 12px per line, -2 for bottom padding
        int bgHeight = titleHeight + contentHeight;

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

        int yOffset = 0;

        // Draw title if enabled
        if (showTitle) {
            Text titleText = Text.literal("Stats HUD").setStyle(Style.EMPTY.withUnderline(true).withBold(true));
            int titleColor = 0xFF000000 | BetterPrisonsClient.config.statsHudTitleColor;
            matrices.pushMatrix();
            matrices.scale(scale);
            matrices.translate(x/scale, y/scale);
            ctx.drawTextWithShadow(client.textRenderer, titleText, 0, 0, titleColor);
            matrices.popMatrix();
            yOffset += titleHeight;
        }

        // Render all visible elements
        if (hasContent) {
            for (int i = 0; i < visibleElements.size(); i++) {
                String text = visibleElements.get(i);

                // Determine color based on element type using config
                int color;
                if (text.startsWith("XP:")) {
                    color = 0xFF000000 | BetterPrisonsClient.config.statsCurrentXPColor;
                } else if (text.startsWith("XP/hr:")) {
                    color = 0xFF000000 | BetterPrisonsClient.config.statsXPPerHourColor;
                } else if (text.startsWith("XP/min:")) {
                    color = 0xFF000000 | BetterPrisonsClient.config.statsXPPerMinuteColor;
                } else if (text.startsWith("Session XP:")) {
                    color = 0xFF000000 | BetterPrisonsClient.config.statsSessionXPColor;
                } else if (text.startsWith("CE:")) {
                    color = 0xFF000000 | BetterPrisonsClient.config.statsCurrentCEColor;
                } else if (text.startsWith("CE/hr:")) {
                    color = 0xFF000000 | BetterPrisonsClient.config.statsCEPerHourColor;
                } else if (text.startsWith("CE/min:")) {
                    color = 0xFF000000 | BetterPrisonsClient.config.statsCEPerMinuteColor;
                } else if (text.startsWith("Session CE:")) {
                    color = 0xFF000000 | BetterPrisonsClient.config.statsSessionCEColor;
                } else if (text.startsWith("Session:")) {
                    color = 0xFF000000 | BetterPrisonsClient.config.statsSessionDurationColor;
                } else if (text.startsWith("Time until lvl")) {
                    color = 0xFF000000 | BetterPrisonsClient.config.statsTimeTillLevelUpColor;
                } else {
                    color = 0xFFFFFFFF; // Default to white
                }
                matrices.pushMatrix();
                matrices.scale(scale);
                matrices.translate(x/scale, (y + yOffset)/scale);
                ctx.drawTextWithShadow(client.textRenderer, Text.literal(text), 0, 0, color);
                matrices.popMatrix();
                yOffset += scaled(12);
            }
        }
    }

    @Override
    public int getWidth() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.textRenderer == null) return scaled(150);

        boolean showTitle = BetterPrisonsClient.config.showStatsHudTitle;

        // Calculate title width
        int titleWidth = 0;
        if (showTitle) {
            Text titleText = Text.literal("Stats HUD").setStyle(Style.EMPTY.withUnderline(true).withBold(true));
            titleWidth = (int)(client.textRenderer.getWidth(titleText) * scale);
        }

        // Build visible elements list (same logic as render method)
        List<String> visibleElements = new ArrayList<>();
        if (BetterPrisonsClient.config.statsShowCurrentXP) {
            visibleElements.add("XP: " + formatNumber(currentXP));
        }
        if (BetterPrisonsClient.config.statsShowTimeTillLevelUp) {
            visibleElements.add(getTimeTillLevelUp());
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
            String sessionText = "Session: ";
            if (paused) {
                sessionText += "(P) ";
            }
            sessionText += getSessionDuration();
            visibleElements.add(sessionText);
        }

        // Calculate maximum width
        int maxWidth = titleWidth;
        for (String text : visibleElements) {
            int textWidth = (int)(client.textRenderer.getWidth(Text.literal(text)) * scale);
            maxWidth = Math.max(maxWidth, textWidth);
        }

        int bgWidth = scaled((int)(maxWidth/scale) + 4);

        // Add padding (StatsHud uses fixed 2px padding)
        return bgWidth + 4; // 2px padding on each side
    }

    @Override
    public int getHeight() {
        int titleHeight = BetterPrisonsClient.config.showStatsHudTitle ? scaled(10) : 0;

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
        if (BetterPrisonsClient.config.statsShowTimeTillLevelUp) visibleCount++;

        return titleHeight + (visibleCount * 12);
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

    // Helper class to track energy readings with timestamps
    private static class EnergyReading {
        long energy;
        long timestamp;

        EnergyReading(long energy, long timestamp) {
            this.energy = energy;
            this.timestamp = timestamp;
        }
    }
}
