package BetterPrisons.modid.hud;

import BetterPrisons.modid.BetterPrisons;
import BetterPrisons.modid.BetterPrisonsClient;
import BetterPrisons.modid.JsonLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class CooldownHud extends BaseHud {
    // Command definitions loaded from commands.json
    public List<CommandDef> definitions = new ArrayList<>();

    // Active cooldowns (from commands AND enchants)
    public List<ActiveCooldown> activeCooldowns = new ArrayList<>();

    public CooldownHud() {
        super("cooldown");
    }

    public void loadFromJson() {
        definitions = JsonLoader.loadCommands();
    }

    // Called by ChatMixin when a command is sent
    public void onCommandSent(String command) {
        BetterPrisonsClient.LOGGER.info("Command sent: " + command);
        for (CommandDef def : definitions) {
            if (matches(command, def)) {
                BetterPrisonsClient.LOGGER.info("Command matched");
                if (def.activeDuration > 0) {
                    // Command has active phase + cooldown
                    addCooldownWithActive(def.displayName, def.activeDuration, def.cooldownDuration);
                } else {
                    // Command has only cooldown
                    addCooldown(def.displayName, def.cooldown);
                }
                break;
            }
        }
    }

    // Called by enchants or other systems to add a simple cooldown
    public void addCooldown(String name, int durationSeconds) {
        // Don't add if a cooldown with the same name already exists
        for (ActiveCooldown cd : activeCooldowns) {
            if (cd.name.equals(name)) {
                return; // Already exists, don't add duplicate
            }
        }
        // Add the new cooldown
        activeCooldowns.add(new ActiveCooldown(name, 0, durationSeconds, System.currentTimeMillis()));
    }

    // Add a cooldown with active duration
    public void addCooldownWithActive(String name, int activeDuration, int cooldownDuration) {
        // Don't add if a cooldown with the same name already exists
        for (ActiveCooldown cd : activeCooldowns) {
            if (cd.name.equals(name)) {
                return; // Already exists, don't add duplicate
            }
        }
        // Add the new cooldown
        activeCooldowns.add(new ActiveCooldown(name, activeDuration, cooldownDuration, System.currentTimeMillis()));
    }

    @Override
    public void tick() {
        // Remove expired cooldowns
        long now = System.currentTimeMillis();
        activeCooldowns.removeIf(cd -> cd.isExpired(now));
    }

    @Override
    public void render(DrawContext ctx, MinecraftClient client) {
        if (!enabled || activeCooldowns.isEmpty()) return;

        // Calculate maximum width needed
        int maxWidth = 0;
        for (ActiveCooldown cd : activeCooldowns) {
            int nameWidth = client.textRenderer.getWidth(Text.literal(cd.name));
            String timeText = cd.getRemainingSeconds() + "s";
            int timeWidth = client.textRenderer.getWidth(Text.literal(timeText));
            int totalWidth = nameWidth + 10 + timeWidth; // 10px offset between name and time
            maxWidth = Math.max(maxWidth, totalWidth);
        }

        // Draw background with custom styling
        int bgWidth = maxWidth + 4; // Add some padding
        int bgHeight = activeCooldowns.size() * 12;

        // Combine RGB color with opacity to create ARGB
        int bgColor = (BetterPrisonsClient.config.cooldownBgOpacity << 24) | (BetterPrisonsClient.config.cooldownBgColor & 0xFFFFFF);
        int borderColor = (BetterPrisonsClient.config.cooldownBorderOpacity << 24) | (BetterPrisonsClient.config.cooldownBorderColor & 0xFFFFFF);
        int thickness = BetterPrisonsClient.config.cooldownBorderThickness;

        // Draw background
        ctx.fill(x - 2, y - 2, x + bgWidth + 2, y + bgHeight + 2, bgColor);

        // Draw border outside the background (no overlap)
        // Top border
        ctx.fill(x - 2, y - 2 - thickness, x + bgWidth + 2, y - 2, borderColor);
        // Bottom border
        ctx.fill(x - 2, y + bgHeight + 2, x + bgWidth + 2, y + bgHeight + 2 + thickness, borderColor);
        // Left border
        ctx.fill(x - 2 - thickness, y - 2 - thickness, x - 2, y + bgHeight + 2 + thickness, borderColor);
        // Right border
        ctx.fill(x + bgWidth + 2, y - 2 - thickness, x + bgWidth + 2 + thickness, y + bgHeight + 2 + thickness, borderColor);

        int yOffset = 0;
        for (ActiveCooldown cd : activeCooldowns) {
            // Determine if in active or cooldown phase
            boolean isActive = cd.isInActivePhase();
            int textColor = isActive ? 0xFF00FF00 : 0xFFFFFFFF; // Green if active, white if cooldown

            // Draw label
            ctx.drawTextWithShadow(client.textRenderer, Text.literal(cd.name), x, y + yOffset, textColor);

            // Calculate timer position with offset
            int nameWidth = client.textRenderer.getWidth(Text.literal(cd.name));
            int timerX = x + nameWidth + 10; // 10px offset after name

            // Draw remaining time with same color as name
            String timeText = cd.getRemainingSeconds() + "s";
            ctx.drawTextWithShadow(client.textRenderer, Text.literal(timeText), timerX, y + yOffset, textColor);
            yOffset += 12;
        }
    }

    @Override
    public int getHeight() {
        return activeCooldowns.size() * 12;
    }

    // Inner classes
    public static class CommandDef {
        public String command;
        public String matchType; // "exact" or "startsWith"
        public int cooldown; // Simple cooldown (if no active duration)
        public int activeDuration; // Duration command is active (0 if not applicable)
        public int cooldownDuration; // Cooldown after active phase
        public String displayName;
    }

    public static class ActiveCooldown {
        public String name;
        public int activeDuration; // 0 if no active phase
        public int cooldownDuration;
        public long startTime;

        public ActiveCooldown(String name, int activeDuration, int cooldownDuration, long startTime) {
            this.name = name;
            this.activeDuration = activeDuration;
            this.cooldownDuration = cooldownDuration;
            this.startTime = startTime;
        }

        public boolean isInActivePhase() {
            if (activeDuration == 0) return false;
            long elapsed = System.currentTimeMillis() - startTime;
            return elapsed < (activeDuration * 1000L);
        }

        public float getProgress() {
            long elapsed = System.currentTimeMillis() - startTime;
            if (isInActivePhase()) {
                // Active phase progress
                return 1.0f - (float) elapsed / (activeDuration * 1000f);
            } else {
                // Cooldown phase progress
                long cooldownElapsed = elapsed - (activeDuration * 1000L);
                return 1.0f - (float) cooldownElapsed / (cooldownDuration * 1000f);
            }
        }

        public int getRemainingSeconds() {
            long elapsed = System.currentTimeMillis() - startTime;
            if (isInActivePhase()) {
                // Active phase remaining time
                return Math.max(0, activeDuration - (int)(elapsed / 1000));
            } else {
                // Cooldown phase remaining time
                long cooldownElapsed = elapsed - (activeDuration * 1000L);
                return Math.max(0, cooldownDuration - (int)(cooldownElapsed / 1000));
            }
        }

        public boolean isExpired(long now) {
            long totalDuration = (activeDuration + cooldownDuration) * 1000L;
            return now > startTime + totalDuration;
        }
    }

    private boolean matches(String command, CommandDef def) {
        if ("exact".equals(def.matchType)) {
            return command.equalsIgnoreCase(def.command);
        } else {
            return command.toLowerCase().startsWith(def.command.toLowerCase());
        }
    }
}
