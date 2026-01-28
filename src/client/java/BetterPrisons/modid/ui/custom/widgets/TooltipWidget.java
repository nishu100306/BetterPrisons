package BetterPrisons.modid.ui.custom.widgets;

import BetterPrisons.modid.ui.custom.core.Theme;
import BetterPrisons.modid.ui.custom.rendering.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Static utility for rendering tooltips.
 * Tooltips appear after hovering for 0.5 seconds and wrap text at 200px width.
 */
public class TooltipWidget {
    private static final int MAX_WIDTH = 200;
    private static final int PADDING = 6;
    private static final int LINE_HEIGHT = 10;
    private static final long HOVER_DELAY_MS = 500;

    private static String currentTooltip = null;
    private static long hoverStartTime = 0;
    private static int lastMouseX = 0;
    private static int lastMouseY = 0;

    /**
     * Updates the current tooltip state.
     * Should be called every frame with the tooltip text or null.
     */
    public static void setTooltip(String tooltip, int mouseX, int mouseY) {
        if (tooltip == null || tooltip.isEmpty()) {
            currentTooltip = null;
            hoverStartTime = 0;
            return;
        }

        // Check if mouse moved significantly
        if (Math.abs(mouseX - lastMouseX) > 2 || Math.abs(mouseY - lastMouseY) > 2) {
            hoverStartTime = System.currentTimeMillis();
            lastMouseX = mouseX;
            lastMouseY = mouseY;
        }

        currentTooltip = tooltip;
    }

    /**
     * Renders the tooltip if the hover delay has elapsed.
     */
    public static void render(DrawContext context, int mouseX, int mouseY) {
        if (currentTooltip == null || currentTooltip.isEmpty()) {
            return;
        }

        long hoverDuration = System.currentTimeMillis() - hoverStartTime;
        if (hoverDuration < HOVER_DELAY_MS) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        // Wrap text
        List<String> lines = wrapText(client, currentTooltip, MAX_WIDTH);

        // Calculate tooltip size
        int tooltipWidth = 0;
        for (String line : lines) {
            int lineWidth = client.textRenderer.getWidth(line);
            if (lineWidth > tooltipWidth) {
                tooltipWidth = lineWidth;
            }
        }
        tooltipWidth += PADDING * 2;
        int tooltipHeight = lines.size() * LINE_HEIGHT + PADDING * 2;

        // Position tooltip near cursor
        int tooltipX = mouseX + 12;
        int tooltipY = mouseY - 12;

        // Reposition if clipping screen bounds
        if (tooltipX + tooltipWidth > screenWidth) {
            tooltipX = mouseX - tooltipWidth - 12;
        }
        if (tooltipY + tooltipHeight > screenHeight) {
            tooltipY = screenHeight - tooltipHeight;
        }
        if (tooltipX < 0) {
            tooltipX = 0;
        }
        if (tooltipY < 0) {
            tooltipY = 0;
        }

        // Draw tooltip background
        RenderUtils.drawRect(context, tooltipX, tooltipY, tooltipWidth, tooltipHeight, Theme.tooltipBackground);

        // Draw tooltip border
        RenderUtils.drawRectOutline(context, tooltipX, tooltipY, tooltipWidth, tooltipHeight, Theme.tooltipBorder, 1);

        // Draw text lines
        for (int i = 0; i < lines.size(); i++) {
            int textX = tooltipX + PADDING;
            int textY = tooltipY + PADDING + i * LINE_HEIGHT;
            context.drawText(client.textRenderer, lines.get(i), textX, textY, Theme.tooltipText, false);
        }
    }

    /**
     * Wraps text to fit within the maximum width.
     */
    private static List<String> wrapText(MinecraftClient client, String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            int testWidth = client.textRenderer.getWidth(testLine);

            if (testWidth > maxWidth && currentLine.length() > 0) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            } else {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    /**
     * Clears the current tooltip.
     */
    public static void clear() {
        currentTooltip = null;
        hoverStartTime = 0;
    }
}
