package BetterPrisons.modid.hud;

import BetterPrisons.modid.BetterPrisonsClient;
import BetterPrisons.modid.enchants.BaseEnchant;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
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

        // Get active enchants from tracker
        List<BaseEnchant> activeEnchants = BetterPrisonsClient.enchantTracker.getActiveEnchants();

        int bgWidth;
        int bgHeight;

        if (activeEnchants.isEmpty()) {
            // Show "No Active Enchants" when there are none
            Text noEnchantsText = Text.literal("No Active Enchants");
            bgWidth = scaled(client.textRenderer.getWidth(noEnchantsText));
            bgHeight = scaled(14);

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

            // Draw text
            matrices.pushMatrix();
            matrices.scale(scale);
            matrices.translate(x/scale, y/scale);
            ctx.drawTextWithShadow(client.textRenderer, noEnchantsText, 0, 0, 0xFF888888);
            matrices.popMatrix();
        } else {
            // Calculate maximum width needed
            int maxWidth = 0;
            for (BaseEnchant enchant : activeEnchants) {
                Text nameText = enchant.displayText != null ? enchant.displayText : Text.literal(enchant.displayName);
                String timeText = String.format("%.1f", enchant.getRemainingSeconds()) + "s";

                int nameWidth = client.textRenderer.getWidth(nameText);
                int timeWidth = client.textRenderer.getWidth(timeText);
                int totalWidth = nameWidth + 10 + timeWidth; // 10px spacing between name and time

                if (totalWidth > maxWidth) {
                    maxWidth = totalWidth;
                }
            }

            // Draw background with custom styling
            bgWidth = scaled(maxWidth);
            bgHeight = scaled(activeEnchants.size() * 14);

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

            int yOffset = 0;
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
                String timeText = String.format("%.1f", enchant.getRemainingSeconds()) + "s";
                int nameWidth = client.textRenderer.getWidth(nameText);
                ctx.drawTextWithShadow(client.textRenderer, Text.literal(timeText), x + nameWidth + 10, y + yOffset, 0xFFAAAAAA);
                yOffset += 14;
                matrices.popMatrix();
            }
        }
    }

    @Override
    public int getHeight() {
        return BetterPrisonsClient.enchantTracker.getActiveEnchants().size() * 14;
    }
}
