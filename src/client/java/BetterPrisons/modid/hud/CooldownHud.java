package BetterPrisons.modid.hud;

import BetterPrisons.modid.BetterPrisons;
import BetterPrisons.modid.BetterPrisonsClient;
import BetterPrisons.modid.JsonLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2fStack;
import net.minecraft.text.Style;

import java.util.ArrayList;
import java.util.List;

public class CooldownHud extends BaseHud {
    // Command definitions loaded from commands.json
    public List<CommandDef> definitions = new ArrayList<>();

    // Active cooldowns (from commands AND enchants)
    public List<ActiveCooldown> activeCooldowns = new ArrayList<>();

    // Track if peaceful mining was disabled due to combat
    private boolean peacefulMiningDisabledByCombat = false;
    private boolean peacefulMiningStateBeforeCombat = false;

    public CooldownHud() {
        super("cooldown");
    }

    public void loadFromJson() {
        definitions = JsonLoader.loadCommands();
    }

    // Helper method to check if a command is enabled based on its display name
    private boolean isCommandEnabled(String displayName) {
        switch (displayName) {
            case "Home": return BetterPrisonsClient.config.homeEnabled;
            case "Jet": return BetterPrisonsClient.config.jetEnabled;
            case "Feed": return BetterPrisonsClient.config.feedEnabled;
            case "Fix": return BetterPrisonsClient.config.fixEnabled;
            case "Combat": return BetterPrisonsClient.config.combatEnabled;
            default: return true; // Default to enabled for unknown commands
        }
    }

    // Helper method to get the color for a command based on its display name
    private int getCommandColor(String displayName) {
        switch (displayName) {
            case "Home": return BetterPrisonsClient.config.homeColor;
            case "Jet": return BetterPrisonsClient.config.jetColor;
            case "Feed": return BetterPrisonsClient.config.feedColor;
            case "Fix": return BetterPrisonsClient.config.fixColor;
            case "Combat": return BetterPrisonsClient.config.combatColor;
            default: return 0xFFFFFF; // Default to white for unknown commands
        }
    }

    // Called by ChatMixin when a command is sent
    public void onCommandSent(String command) {
        BetterPrisonsClient.LOGGER.info("Command sent: " + command);
        for (CommandDef def : definitions) {
            if (matches(command, def)) {
                BetterPrisonsClient.LOGGER.info("Command matched");

                // Check if command is enabled
                if (!isCommandEnabled(def.displayName)) {
                    BetterPrisonsClient.LOGGER.info("Command is disabled, skipping");
                    return;
                }

                // Special handling for /home and /fix to set chat patterns dynamically
                if (command.startsWith("/home ")) {
                    // Check if cooldown already active, dont set chatPattern if alr active
                    for (ActiveCooldown cd : activeCooldowns) {
                        if (cd.name.equals(def.displayName)) {
                            return; // Cooldown already active, don't set a new one
                        }
                    }
                    String homeName = command.split(" ")[1].toLowerCase();
                    BetterPrisonsClient.LOGGER.info("Home name: " + homeName);
                    if (homeName.isEmpty() ||
                            homeName.contains(" ") ||
                            homeName.equals("delete") ||
                            homeName.equals("list") ||
                            homeName.equals("set"))
                    { return;}
                    def.chatPattern = String.format("§a§l(!) §aTeleported to §a§n%s§a!", homeName);
                }
                if (def.command.equals("/fix")) {
                    // Check if cooldown already active, dont set chatPattern if alr active
                    for (ActiveCooldown cd : activeCooldowns) {
                        if (cd.name.equals(def.displayName)) {
                            return; // Cooldown already active, don't set a new one
                        }
                    }
                    def.chatPattern = "§a§l(!) §aYour item has been restored!";
                }
                if (def.command.equals("/jet")) {
                    // Check if cooldown already active, dont set chatPattern if alr active
                    for (ActiveCooldown cd : activeCooldowns) {
                        if (cd.name.equals(def.displayName)) {
                            return; // Cooldown already active, don't set a new one
                        }
                    }
                    def.chatPattern = "§a§lJETPACK ENGAGED: §f§lprepare for launch!";
                }
                if (def.command.equals("/feed")) {
                    // Check if cooldown already active, dont set chatPattern if alr active
                    for (ActiveCooldown cd : activeCooldowns) {
                        if (cd.name.equals(def.displayName)) {
                            return; // Cooldown already active, don't set a new one
                        }
                    }
                    def.chatPattern = "§a§l(!) §aYou have been satiated.";
                }

                // Only add cooldown if no chat pattern is defined (command-triggered)
                if (def.chatPattern == null || def.chatPattern.isEmpty()) {
                    addCooldown(def.displayName, def.cooldown, def.icon, getCommandColor(def.displayName));
                }
                break;
            }
        }
    }

    // Called by ChatReceiveMixin when a chat message is received
    public void onChatReceived(String message) {
        // Check for jet cancellation message
        if (message.equals("§c§l(!) §c/jet cancelled.")) {
            // Remove Jet cooldown if it exists
            activeCooldowns.removeIf(cd -> cd.name.equals("Jet"));
            BetterPrisonsClient.LOGGER.info("Jet cooldown removed due to cancellation");
            return;
        }

        for (CommandDef def : definitions) {
            // Only check if this command uses chat pattern detection
            if (def.chatPattern != null && !def.chatPattern.isEmpty()) {
                if (message.equals(def.chatPattern)) {
                    //BetterPrisonsClient.LOGGER.info("Cooldown triggered by chat pattern: " + def.displayName);
                    if (isCommandEnabled(def.displayName)) {
                        addCooldown(def.displayName, def.cooldown, def.icon, getCommandColor(def.displayName));
                    }
                    if (def.command.equals("/fix") || def.command.equals("/home") || def.command.equals("/jet") || def.command.equals("/feed")) {
                        def.chatPattern = null; // Reset after use
                    }
                    break;
                }
            }
        }
    }

    // Called by enchants or other systems to add a simple cooldown
    public void addCooldown(String name, int durationSeconds) {
        addCooldown(name, durationSeconds, null, 0xFFFFFF);
    }

    // Add cooldown with icon
    public void addCooldown(String name, int durationSeconds, String icon) {
        addCooldown(name, durationSeconds, icon, 0xFFFFFF);
    }

    // Add cooldown with icon and color
    public void addCooldown(String name, int durationSeconds, String icon, int color) {
        // Don't add if a cooldown with the same name already exists
        for (ActiveCooldown cd : activeCooldowns) {
            if (cd.name.equals(name)) {
                return; // Already exists, don't add duplicate
            }
        }
        // Add the new cooldown
        activeCooldowns.add(new ActiveCooldown(name, durationSeconds, System.currentTimeMillis(), icon, color));

        // If this is a combat cooldown and auto-disable on combat is enabled
        if (name.equals("Combat") && BetterPrisonsClient.config.peacefulMiningDisableOnCombat) {
            // Save the current state of peaceful mining
            peacefulMiningStateBeforeCombat = BetterPrisonsClient.config.peacefulMiningEnabled;
            // Only disable if it was enabled
            if (peacefulMiningStateBeforeCombat) {
                BetterPrisonsClient.config.peacefulMiningEnabled = false;
                BetterPrisonsClient.config.save();
                peacefulMiningDisabledByCombat = true;
                BetterPrisonsClient.LOGGER.info("Peaceful Mining auto-disabled due to combat");
            }
        }
    }

    @Override
    public void tick() {
        // Check if combat cooldown is still active before removing expired cooldowns
        boolean combatWasActive = activeCooldowns.stream().anyMatch(cd -> cd.name.equals("Combat"));

        // Remove expired cooldowns
        long now = System.currentTimeMillis();
        activeCooldowns.removeIf(cd -> cd.isExpired(now));

        // Check if combat cooldown is now gone (expired)
        boolean combatIsActive = activeCooldowns.stream().anyMatch(cd -> cd.name.equals("Combat"));

        // If combat ended and we disabled peaceful mining due to combat, re-enable it
        if (combatWasActive && !combatIsActive && peacefulMiningDisabledByCombat) {
            // Restore the previous state
            BetterPrisonsClient.config.peacefulMiningEnabled = peacefulMiningStateBeforeCombat;
            BetterPrisonsClient.config.save();
            peacefulMiningDisabledByCombat = false;
            BetterPrisonsClient.LOGGER.info("Peaceful Mining auto-enabled after combat ended");
        }
    }

    @Override
    public void render(DrawContext ctx, MinecraftClient client) {
        this.scale = BetterPrisonsClient.config.cooldownHudScale;
        this.scale = this.scale / 100.0f;

        boolean showTitle = BetterPrisonsClient.config.showCooldownHudTitle;
        boolean hasContent = !activeCooldowns.isEmpty();

        // Don't render if HUD is disabled and there's nothing to show
        if (!enabled || (!showTitle && !hasContent)) return;

        // Calculate title dimensions
        int titleHeight = 0;
        int titleWidth = 0;
        if (showTitle) {
            Text titleText = Text.literal("Cooldown HUD").setStyle(Style.EMPTY.withUnderline(true).withBold(true));
            titleWidth = (int)(client.textRenderer.getWidth(titleText) * scale);
            titleHeight = scaled(12); // Text height + spacing
        }

        // Calculate maximum width needed for content (scaled)
        int iconSpace = scaled(20); // 16px icon + 4px spacing
        int maxWidth = titleWidth;
        if (hasContent) {
            for (ActiveCooldown cd : activeCooldowns) {
                int nameWidth = client.textRenderer.getWidth(Text.literal(cd.name));
                nameWidth = (int)(nameWidth * scale); // Apply scaling
                String timeText = cd.getRemainingSeconds() + "s";
                int timeWidth = client.textRenderer.getWidth(Text.literal(timeText));
                timeWidth = (int)(timeWidth * scale); // Apply scaling
                int totalWidth = iconSpace + nameWidth + scaled(10) + timeWidth; // icon + name + spacing + time
                maxWidth = Math.max(maxWidth, totalWidth);
            }
        }

        // Draw background with custom styling (with scaling applied)
        int bgWidth = maxWidth; // Width already includes padding from maxWidth calculation
        int contentHeight = hasContent ? activeCooldowns.size() * scaled(18) : 0;
        int bgHeight = titleHeight + contentHeight;

        // Combine RGB color with opacity to create ARGB
        int bgColor = (BetterPrisonsClient.config.cooldownBgOpacity << 24) | (BetterPrisonsClient.config.cooldownBgColor & 0xFFFFFF);
        int borderColor = (BetterPrisonsClient.config.cooldownBorderOpacity << 24) | (BetterPrisonsClient.config.cooldownBorderColor & 0xFFFFFF);
        int thickness = scaled(BetterPrisonsClient.config.cooldownBorderThickness);
        int padding = 4;
        if (scale < 1) padding = scaled(padding);

        // Draw background
        ctx.fill(x - padding, y - padding, x + bgWidth + padding, y + bgHeight + padding, bgColor);

        // Draw border outside the background (no overlap)
        // Top border
        ctx.fill(x - padding, y - padding - thickness, x + bgWidth + padding, y - padding, borderColor);
        // Bottom border
        ctx.fill(x - padding, y + bgHeight + padding, x + bgWidth + padding, y + bgHeight + padding + thickness, borderColor);
        // Left border
        ctx.fill(x - padding - thickness, y - padding - thickness, x - padding, y + bgHeight + padding + thickness, borderColor);
        // Right border
        ctx.fill(x + bgWidth + padding, y - padding - thickness, x + bgWidth + padding + thickness, y + bgHeight + padding + thickness, borderColor);

        Matrix3x2fStack matrices = ctx.getMatrices();
        int yOffset = 0;

        // Draw title if enabled
        if (showTitle) {
            Text titleText = Text.literal("Cooldown HUD").setStyle(Style.EMPTY.withUnderline(true).withBold(true));
            int titleColor = 0xFF000000 | BetterPrisonsClient.config.cooldownHudTitleColor;
            matrices.pushMatrix();
            matrices.scale(scale);
            matrices.translate(x/scale, y/scale);
            ctx.drawTextWithShadow(client.textRenderer, titleText, 0, 0, titleColor);
            matrices.popMatrix();


            yOffset += titleHeight;
        }

        // Draw cooldown content
        if (hasContent) {
            int rowHeight = scaled(18);
            int iconYOffset = scaled(1);
            int textYOffset = scaled(4);

            for (ActiveCooldown cd : activeCooldowns) {
                int textColor = 0xFF000000 | cd.color; // Use configured color with full alpha

                // Draw icon if available
                if (cd.icon != null && !cd.icon.isEmpty()) {
                    try {
                        Identifier itemId = Identifier.of(cd.icon);
                        ItemStack iconStack = new ItemStack(Registries.ITEM.get(itemId));
                        if (!iconStack.isEmpty()) {
                            matrices.pushMatrix();
                            matrices.scale(scale);
                            matrices.translate(x/scale, (y + yOffset + iconYOffset)/scale);
                            ctx.drawItem(iconStack, 0, 0);
                            matrices.popMatrix();
                        }
                    } catch (Exception e) {
                        BetterPrisonsClient.LOGGER.warn("Failed to render icon for cooldown: " + cd.icon);
                    }
                }

                // Draw label (positioned after icon)
                matrices.pushMatrix();
                matrices.scale(scale);
                matrices.translate((x + iconSpace)/scale, (y + yOffset + textYOffset)/scale);
                ctx.drawTextWithShadow(client.textRenderer, Text.literal(cd.name), 0, 0, textColor);
                matrices.popMatrix();

                // Calculate timer position with offset
                int nameWidth = client.textRenderer.getWidth(Text.literal(cd.name));
                int timerX = iconSpace + (int)(nameWidth * scale) + scaled(10);

                // Draw remaining time
                String timeText = cd.getRemainingSeconds() + "s";
                matrices.pushMatrix();
                matrices.scale(scale);
                matrices.translate((x + timerX)/scale, (y + yOffset + textYOffset)/scale);
                ctx.drawTextWithShadow(client.textRenderer, Text.literal(timeText), 0, 0, textColor);
                matrices.popMatrix();

                yOffset += rowHeight;
            }
        }
    }

    @Override
    public int getWidth() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.textRenderer == null) return scaled(120);

        boolean showTitle = BetterPrisonsClient.config.showCooldownHudTitle;
        boolean hasContent = !activeCooldowns.isEmpty();

        // Calculate title width
        int titleWidth = 0;
        if (showTitle) {
            Text titleText = Text.literal("Cooldown HUD");
            titleWidth = (int)(client.textRenderer.getWidth(titleText) * scale);
        }

        // Calculate maximum width needed for content
        int iconSpace = scaled(20);
        int maxWidth = titleWidth;
        if (hasContent) {
            for (ActiveCooldown cd : activeCooldowns) {
                int nameWidth = (int)(client.textRenderer.getWidth(Text.literal(cd.name)) * scale);
                String timeText = cd.getRemainingSeconds() + "s";
                int timeWidth = (int)(client.textRenderer.getWidth(Text.literal(timeText)) * scale);
                int totalWidth = iconSpace + nameWidth + scaled(10) + timeWidth;
                maxWidth = Math.max(maxWidth, totalWidth);
            }
        }

        // Add padding (same logic as render method)
        int padding = 4;
        if (scale < 1) padding = scaled(padding);

        return maxWidth + (padding * 2); // padding on both sides
    }

    @Override
    public int getHeight() {
        int titleHeight = BetterPrisonsClient.config.showCooldownHudTitle ? scaled(10) : 0;
        int contentHeight = activeCooldowns.size() * scaled(18);
        return titleHeight + contentHeight;
    }

    // Inner classes
    public static class CommandDef {
        public String command;
        public String matchType; // "exact" or "startsWith"
        public int cooldown; // Cooldown duration in seconds
        public String displayName;
        public String chatPattern; // Optional: server message pattern to trigger cooldown
        public List<String> aliases; // Optional: alternative commands that trigger the same cooldown
        public String icon; // Optional: item ID for icon display (e.g., "minecraft:blaze_powder")
    }

    public static class ActiveCooldown {
        public String name;
        public int duration;
        public long startTime;
        public String icon; // Item ID for icon display
        public int color; // RGB color for text

        public ActiveCooldown(String name, int duration, long startTime, String icon, int color) {
            this.name = name;
            this.duration = duration;
            this.startTime = startTime;
            this.icon = icon;
            this.color = color;
        }

        public float getProgress() {
            long elapsed = System.currentTimeMillis() - startTime;
            return 1.0f - (float) elapsed / (duration * 1000f);
        }

        public int getRemainingSeconds() {
            long elapsed = System.currentTimeMillis() - startTime;
            return Math.max(0, duration - (int)(elapsed / 1000));
        }

        public boolean isExpired(long now) {
            return now > startTime + (duration * 1000L);
        }
    }

    private boolean matches(String command, CommandDef def) {
        // Check main command
        boolean mainMatch = false;
        if ("exact".equals(def.matchType)) {
            mainMatch = command.equalsIgnoreCase(def.command);
        } else {
            mainMatch = command.toLowerCase().startsWith(def.command.toLowerCase());
        }

        if (mainMatch) return true;

        // Check aliases
        if (def.aliases != null && !def.aliases.isEmpty()) {
            for (String alias : def.aliases) {
                if ("exact".equals(def.matchType)) {
                    if (command.equalsIgnoreCase(alias)) return true;
                } else {
                    if (command.toLowerCase().startsWith(alias.toLowerCase())) return true;
                }
            }
        }

        return false;
    }
}
