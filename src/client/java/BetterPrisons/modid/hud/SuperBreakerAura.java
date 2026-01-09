package BetterPrisons.modid.hud;

import BetterPrisons.modid.BetterPrisonsClient;
import BetterPrisons.modid.enchants.BaseEnchant;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class SuperBreakerAura {

    public void render(DrawContext ctx, MinecraftClient client) {
        // Get Super Breaker enchant
        if (!BetterPrisonsClient.config.superBreakerAuraEnabled) return;
        BaseEnchant superBreaker = BetterPrisonsClient.enchantTracker.getEnchant("super_breaker");
        if (superBreaker == null) {
            BetterPrisonsClient.LOGGER.warn("Super Breaker enchant is null!");
            return;
        }
        if (!superBreaker.isActive) return;

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        // Center position
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        // Get remaining time
        double remainingSeconds = superBreaker.getRemainingSeconds();
        String timeText = String.format("%.1f", remainingSeconds);

        // Get colors from config and combine with opacity
        int baseColor = (BetterPrisonsClient.config.superBreakerBaseOpacity << 24) | (BetterPrisonsClient.config.superBreakerBaseColor & 0xFFFFFF);
        int lightColor = (BetterPrisonsClient.config.superBreakerLightOpacity << 24) | (BetterPrisonsClient.config.superBreakerLightColor & 0xFFFFFF);

        // Calculate progress (1.0 = full time, 0.0 = no time left)
        double progress = remainingSeconds / superBreaker.durationSeconds;

        int timerColor = 0xFFFFFFFF; // White timer

        // Draw two semicircles (left and right halves)
        int radius = 100;
        int thickness = 10;

        // First, draw full arcs in dark/base color
        drawSemicircle(ctx, centerX, centerY, radius, thickness, baseColor, true, 1.0);
        drawSemicircle(ctx, centerX, centerY, radius, thickness, baseColor, false, 1.0);

        // Then, overlay with lighter color for remaining time portion
        drawSemicircle(ctx, centerX, centerY, radius, thickness, lightColor, true, progress);
        drawSemicircle(ctx, centerX, centerY, radius, thickness, lightColor, false, progress);

        // Draw countdown timer in center
        int timerWidth = client.textRenderer.getWidth(timeText);
        int timerX = centerX - timerWidth / 2;
        int timerY = centerY - 4;

        ctx.drawTextWithShadow(client.textRenderer, Text.literal(timeText), timerX, timerY, timerColor);
    }

    private void drawSemicircle(DrawContext ctx, int centerX, int centerY, int radius, int thickness, int color, boolean isLeft, double fillProgress) {
        // Draw semicircle using filled rectangles for each pixel
        // fillProgress: 1.0 = draw full arc, 0.0 = draw nothing
        for (int r = radius - thickness; r <= radius; r++) {
            for (int angle = 0; angle < 360; angle++) {
                boolean shouldDraw = false;

                if (isLeft) {
                    // Left arc: 120° to 240° (continuous range)
                    if (angle >= 140 && angle <= 220) {
                        if (fillProgress >= 1.0) {
                            shouldDraw = true;
                        } else {
                            int angleInArc = angle - 120;
                            int arcLength = 120;
                            shouldDraw = angleInArc <= (arcLength * fillProgress);
                        }
                    }
                } else {
                    // Right arc: 300° to 360° and 0° to 60° (wraps around)
                    // Fills in OPPOSITE direction (counter-clockwise from end to start)
                    if (angle > 320 || angle < 40) {
                        if (fillProgress >= 1.0) {
                            shouldDraw = true;
                        } else {
                            // Normalize angle to 0-120 range (reversed direction)
                            int normalizedAngle;
                            if (angle < 60) {
                                normalizedAngle = 60 - angle; // 0-60 -> 60-0 (reversed)
                            } else {
                                normalizedAngle = 360 - angle + 60; // 300-360 -> 120-60 (reversed)
                            }
                            int arcLength = 120;
                            shouldDraw = normalizedAngle <= (arcLength * fillProgress);
                        }
                    }
                }

                if (shouldDraw) {
                    double radians = Math.toRadians(angle);
                    int x = centerX + (int)(r * Math.cos(radians));
                    int y = centerY + (int)(r * Math.sin(radians));
                    ctx.fill(x, y, x + 2, y + 2, color);  // Draw 2x2 pixel to eliminate gaps
                }
            }
        }
    }

}
