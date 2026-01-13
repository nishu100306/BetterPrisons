package BetterPrisons.modid.hud;

import BetterPrisons.modid.BetterPrisonsClient;
import BetterPrisons.modid.enchants.BaseEnchant;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;

import java.util.List;

public class EnchantHud extends BaseHud {
    public EnchantHud() {
        super("enchant");
    }

    @Override
    public void tick() {
        // No tick needed - enchant state is managed by EnchantTracker
    }

    @Override
    public void render(DrawContext ctx, MinecraftClient client) {
        this.scale = BetterPrisonsClient.config.enchantHudScale / 100.0f;
        Matrix3x2fStack matrices = ctx.getMatrices();
        if (!enabled) return;

        boolean showTitle = BetterPrisonsClient.config.showEnchantHudTitle;

        // Get active enchants from tracker
        List<BaseEnchant> activeEnchants = BetterPrisonsClient.enchantTracker.getActiveEnchants();
        boolean hasContent = !activeEnchants.isEmpty();

        // Don't render if no title and no content
        if (!showTitle && !hasContent) return;

        // Calculate title dimensions
        int titleHeight = 0;
        int titleWidth = 0;
        if (showTitle) {
            Text titleText = Text.literal("Enchant HUD").setStyle(Style.EMPTY.withUnderline(true).withBold(true));
            titleWidth = (int)(client.textRenderer.getWidth(titleText) * scale);
            titleHeight = scaled(12); // Text height + spacing
        }

        int bgWidth;
        int bgHeight;
        int contentYOffset = titleHeight;

        if (!hasContent) {
            // No active enchants - only show title if enabled
            bgWidth = titleWidth;
            bgHeight = titleHeight;

            // Combine RGB color with opacity to create ARGB
            int bgColor = (BetterPrisonsClient.config.enchantBgOpacity << 24) | (BetterPrisonsClient.config.enchantBgColor & 0xFFFFFF);
            int borderColor = (BetterPrisonsClient.config.enchantBorderOpacity << 24) | (BetterPrisonsClient.config.enchantBorderColor & 0xFFFFFF);
            int thickness = BetterPrisonsClient.config.enchantBorderThickness;

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

            // Draw title if enabled
            if (showTitle) {
                Text titleText = Text.literal("Enchant HUD").setStyle(Style.EMPTY.withUnderline(true).withBold(true));
                int titleColor = 0xFF000000 | BetterPrisonsClient.config.enchantHudTitleColor;
                matrices.pushMatrix();
                matrices.scale(scale);
                matrices.translate(x/scale, y/scale);
                ctx.drawTextWithShadow(client.textRenderer, titleText, 0, 0, titleColor);
                matrices.popMatrix();
            }
        } else {
            // Calculate maximum width needed
            int maxWidth = titleWidth;
            for (BaseEnchant enchant : activeEnchants) {
                Text nameText = enchant.displayText != null ? enchant.displayText : Text.literal(enchant.displayName);
                String timeString = String.format("%.1f", enchant.getRemainingSeconds()) + "s";

                int nameWidth = client.textRenderer.getWidth(nameText);
                int timeWidth = client.textRenderer.getWidth(timeString);
                int totalWidth = (int)((nameWidth + 10 + timeWidth) * scale); // 10px spacing between name and time

                if (totalWidth > maxWidth) {
                    maxWidth = totalWidth;
                }
            }

            // Draw background with custom styling
            bgWidth = scaled((int)(maxWidth/scale));
            int contentHeight = scaled(activeEnchants.size() * 14);
            bgHeight = titleHeight + contentHeight;

            // Combine RGB color with opacity to create ARGB
            int bgColor = (BetterPrisonsClient.config.enchantBgOpacity << 24) | (BetterPrisonsClient.config.enchantBgColor & 0xFFFFFF);
            int borderColor = (BetterPrisonsClient.config.enchantBorderOpacity << 24) | (BetterPrisonsClient.config.enchantBorderColor & 0xFFFFFF);
            int thickness = BetterPrisonsClient.config.enchantBorderThickness;

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

            // Draw title if enabled
            if (showTitle) {
                Text titleText = Text.literal("Enchant HUD").setStyle(Style.EMPTY.withUnderline(true).withBold(true));
                int titleColor = 0xFF000000 | BetterPrisonsClient.config.enchantHudTitleColor;
                matrices.pushMatrix();
                matrices.scale(scale);
                matrices.translate(x/scale, y/scale);
                ctx.drawTextWithShadow(client.textRenderer, titleText, 0, 0, titleColor);
                matrices.popMatrix();
            }

            int yOffset = contentYOffset;
            for (BaseEnchant enchant : activeEnchants) {
                // Draw enchant name (use displayText with its color if available, otherwise use displayName with green)
                Text nameText;
                if (enchant.displayText != null) {
                    nameText = enchant.displayText;

                } else {
                    nameText = Text.literal(enchant.displayName);
                }
                matrices.pushMatrix();
                matrices.scale(scale);
                matrices.translate(x/scale, (y + yOffset)/scale);
                ctx.drawTextWithShadow(client.textRenderer, nameText, 0, 0, 0xFFFFFFFF);

                // Draw remaining time (1 decimal point) - positioned after the name
                int timeColor = 0xFF000000 | BetterPrisonsClient.config.enchantTimeColor;
                String timeString = String.format("%.1f", enchant.getRemainingSeconds()) + "s";
                int nameWidth = client.textRenderer.getWidth(nameText);
                ctx.drawTextWithShadow(client.textRenderer, Text.literal(timeString), nameWidth + 10, 0, timeColor);

                matrices.popMatrix();
                yOffset += scaled(14);
            }
        }
    }

    @Override
    public int getWidth() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.textRenderer == null) return scaled(120);

        boolean showTitle = BetterPrisonsClient.config.showEnchantHudTitle;
        List<BaseEnchant> activeEnchants = BetterPrisonsClient.enchantTracker.getActiveEnchants();
        boolean hasContent = !activeEnchants.isEmpty();

        // Calculate title width
        int titleWidth = 0;
        if (showTitle) {
            Text titleText = Text.literal("Enchant HUD").setStyle(Style.EMPTY.withUnderline(true).withBold(true));
            titleWidth = (int)(client.textRenderer.getWidth(titleText) * scale);
        }

        if (!hasContent) {
            return titleWidth;
        }

        // Calculate maximum width needed
        int maxWidth = titleWidth;
        for (BaseEnchant enchant : activeEnchants) {
            Text nameText = enchant.displayText != null ? enchant.displayText : Text.literal(enchant.displayName);
            String timeText = String.format("%.1f", enchant.getRemainingSeconds()) + "s";
            int nameWidth = client.textRenderer.getWidth(nameText);
            int timeWidth = client.textRenderer.getWidth(timeText);
            int totalWidth = (int)((nameWidth + 10 + timeWidth) * scale);
            maxWidth = Math.max(maxWidth, totalWidth);
        }

        int bgWidth = scaled((int)(maxWidth/scale));

        // Add padding (EnchantHud uses fixed 2px padding)
        return bgWidth + 4; // 2px padding on each side
    }

    @Override
    public int getHeight() {
        int titleHeight = BetterPrisonsClient.config.showEnchantHudTitle ? scaled(10) : 0;
        int contentHeight = BetterPrisonsClient.enchantTracker.getActiveEnchants().size() * scaled(14);
        return titleHeight + contentHeight;
    }
}
