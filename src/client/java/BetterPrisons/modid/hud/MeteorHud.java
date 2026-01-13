package BetterPrisons.modid.hud;

import BetterPrisons.modid.BetterPrisonsClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MeteorHud extends BaseHud {
    private List<MeteorInfo> activeMeteors = new ArrayList<>();
    private static final long METEOR_TIMEOUT_MS = 20 * 60 * 1000; // 20 minutes in milliseconds

    // Pattern to match coordinates like "-620x, 109y, 44z"
    private static final Pattern COORDS_PATTERN = Pattern.compile("(-?\\d+)x,\\s*(-?\\d+)y,\\s*(-?\\d+)z");

    public MeteorHud() {
        super("meteor");
    }

    /**
     * Called when a meteor falling message is detected
     * Line 1: "(!) A meteor is falling from the sky at:"
     * Line 2: "-620x, 109y, 44z"
     */
    public void onMeteorFalling(String coordsLine) {
        Matcher matcher = COORDS_PATTERN.matcher(coordsLine);
        if (matcher.find()) {
            try {
                int x = Integer.parseInt(matcher.group(1));
                int y = Integer.parseInt(matcher.group(2));
                int z = Integer.parseInt(matcher.group(3));

                // Check if meteor with same coordinates already exists
                for (MeteorInfo meteor : activeMeteors) {
                    if (meteor.x == x && meteor.y == y && meteor.z == z) {
                        return; // Duplicate, don't add
                    }
                }

                // Create meteor icon from config
                ItemStack iconStack = createMeteorIcon();

                // Add new meteor
                activeMeteors.add(new MeteorInfo(x, y, z, System.currentTimeMillis(), iconStack));
                BetterPrisonsClient.LOGGER.info("Meteor detected at: " + x + ", " + y + ", " + z);
            } catch (NumberFormatException e) {
                BetterPrisonsClient.LOGGER.warn("Failed to parse meteor coordinates: " + coordsLine);
            }
        }
    }

    private ItemStack createMeteorIcon() {
        ItemStack stack;
        try {
            String itemId = BetterPrisonsClient.config.meteorIconItemId;
            Identifier identifier = Identifier.tryParse(itemId);
            if (identifier != null) {
                Item item = Registries.ITEM.get(identifier);
                if (item != null) {
                    stack = new ItemStack(item);
                } else {
                    // Fallback to nether quartz ore
                    stack = new ItemStack(Registries.ITEM.get(Identifier.of("minecraft", "nether_quartz_ore")));
                }
            } else {
                // Fallback to nether quartz ore
                stack = new ItemStack(Registries.ITEM.get(Identifier.of("minecraft", "nether_quartz_ore")));
            }
        } catch (Exception e) {
            BetterPrisonsClient.LOGGER.warn("Failed to create meteor icon: " + e.getMessage());
            // Fallback to nether quartz ore
            stack = new ItemStack(Registries.ITEM.get(Identifier.of("minecraft", "nether_quartz_ore")));
        }

        // Add enchantment glint
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        return stack;
    }

    /**
     * Called when a meteor crashed message is detected
     * Line 1: "(!) A meteor has crashed at:"
     * Line 2: "-620x, 109y, 44z"
     */
    public void onMeteorCrashed(String coordsLine) {
        Matcher matcher = COORDS_PATTERN.matcher(coordsLine);
        if (matcher.find()) {
            try {
                int x = Integer.parseInt(matcher.group(1));
                int y = Integer.parseInt(matcher.group(2));
                int z = Integer.parseInt(matcher.group(3));

                // Remove meteor with matching coordinates
                activeMeteors.removeIf(meteor -> meteor.x == x && meteor.y == y && meteor.z == z);
                BetterPrisonsClient.LOGGER.info("Meteor removed at: " + x + ", " + y + ", " + z);
            } catch (NumberFormatException e) {
                BetterPrisonsClient.LOGGER.warn("Failed to parse meteor crash coordinates: " + coordsLine);
            }
        }
    }

    @Override
    public void tick() {
        // Remove expired meteors (older than 20 minutes)
        long now = System.currentTimeMillis();
        activeMeteors.removeIf(meteor -> now - meteor.spawnTime > METEOR_TIMEOUT_MS);
    }

    @Override
    public void render(DrawContext ctx, MinecraftClient client) {
        this.scale = BetterPrisonsClient.config.meteorHudScale;
        this.scale = this.scale / 100.0f;

        boolean showTitle = BetterPrisonsClient.config.showMeteorHudTitle;
        boolean hasContent = !activeMeteors.isEmpty();

        // Don't render if HUD is disabled and there's nothing to show
        if (!enabled || (!showTitle && !hasContent)) return;

        // Calculate title dimensions
        int titleHeight = 0;
        int titleWidth = 0;
        if (showTitle) {
            Text titleText = Text.literal("Meteor HUD").setStyle(Style.EMPTY.withUnderline(true).withBold(true));
            titleWidth = (int)(client.textRenderer.getWidth(titleText) * scale);
            titleHeight = scaled(12); // Text height + spacing
        }

        // Calculate maximum width needed for content (scaled)
        // Layout: [Icon 16px] [spacing 4px] [text content]
        int maxTextWidth = titleWidth;
        if (hasContent) {
            for (MeteorInfo meteor : activeMeteors) {
                String coordsText = String.format("%dx, %dy, %dz", meteor.x, meteor.y, meteor.z);
                int textWidth = (int)(client.textRenderer.getWidth(Text.literal(coordsText)) * scale);
                maxTextWidth = Math.max(maxTextWidth, textWidth);
            }
        }

        // Draw background with custom styling (with scaling applied)
        int bgWidth = hasContent ? (scaled(16 + 4) + maxTextWidth) : maxTextWidth; // icon + spacing + text
        int contentHeight = hasContent ? activeMeteors.size() * scaled(20) : 0;
        int bgHeight = titleHeight + contentHeight;

        // Combine RGB color with opacity to create ARGB
        int bgColor = (BetterPrisonsClient.config.meteorBgOpacity << 24) | (BetterPrisonsClient.config.meteorBgColor & 0xFFFFFF);
        int borderColor = (BetterPrisonsClient.config.meteorBorderOpacity << 24) | (BetterPrisonsClient.config.meteorBorderColor & 0xFFFFFF);
        int thickness = scaled(BetterPrisonsClient.config.meteorBorderThickness);
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
            Text titleText = Text.literal("Meteor HUD").setStyle(Style.EMPTY.withUnderline(true).withBold(true));
            int titleColor = 0xFF000000 | BetterPrisonsClient.config.meteorHudTitleColor;
            matrices.pushMatrix();
            matrices.scale(scale);
            matrices.translate(x/scale, y/scale);
            ctx.drawTextWithShadow(client.textRenderer, titleText, 0, 0, titleColor);
            matrices.popMatrix();
            yOffset += titleHeight;
        }

        // Draw meteor coordinates with icons
        if (hasContent) {
            int textColor = 0xFF000000 | BetterPrisonsClient.config.meteorTextColor;
            int iconSpacing = scaled(20); // 16px icon + 4px gap
            int rowHeight = scaled(20); // Space for icon + padding

            for (MeteorInfo meteor : activeMeteors) {
                // Draw meteor icon on the left
                if (meteor.iconStack != null) {
                    matrices.pushMatrix();
                    matrices.scale(scale);
                    matrices.translate(x/scale, (y + yOffset)/scale);
                    ctx.drawItem(meteor.iconStack, 0, 0);
                    matrices.popMatrix();
                }

                // Draw coordinates text next to the icon
                String coordsText = String.format("%dx, %dy, %dz", meteor.x, meteor.y, meteor.z);
                matrices.pushMatrix();
                matrices.scale(scale);
                matrices.translate((x + iconSpacing)/scale, (y + yOffset + scaled(4))/scale);
                ctx.drawTextWithShadow(client.textRenderer, Text.literal(coordsText), 0, 0, textColor);
                matrices.popMatrix();

                yOffset += rowHeight;
            }
        }
    }

    @Override
    public int getWidth() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.textRenderer == null) return scaled(130);

        boolean showTitle = BetterPrisonsClient.config.showMeteorHudTitle;
        boolean hasContent = !activeMeteors.isEmpty();

        // Calculate title width
        int titleWidth = 0;
        if (showTitle) {
            Text titleText = Text.literal("Meteor HUD").setStyle(Style.EMPTY.withUnderline(true).withBold(true));
            titleWidth = (int)(client.textRenderer.getWidth(titleText) * scale);
        }

        // Calculate maximum text width
        int maxTextWidth = titleWidth;
        if (hasContent) {
            for (MeteorInfo meteor : activeMeteors) {
                String coordsText = String.format("%dx, %dy, %dz", meteor.x, meteor.y, meteor.z);
                int textWidth = (int)(client.textRenderer.getWidth(Text.literal(coordsText)) * scale);
                maxTextWidth = Math.max(maxTextWidth, textWidth);
            }
        }

        int bgWidth = hasContent ? (scaled(16 + 4) + maxTextWidth) : maxTextWidth;

        // Add padding (same logic as render method)
        int padding = 4;
        if (scale < 1) padding = scaled(padding);

        return bgWidth + (padding * 2); // padding on both sides
    }

    @Override
    public int getHeight() {
        int titleHeight = BetterPrisonsClient.config.showMeteorHudTitle ? scaled(10) : 0;
        int contentHeight = activeMeteors.size() * scaled(20);
        return titleHeight + contentHeight;
    }

    public static class MeteorInfo {
        public int x;
        public int y;
        public int z;
        public long spawnTime;
        public ItemStack iconStack;

        public MeteorInfo(int x, int y, int z, long spawnTime, ItemStack iconStack) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.spawnTime = spawnTime;
            this.iconStack = iconStack;
        }
    }
}
