package BetterPrisons.modid.ui.custom.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.Stack;

/**
 * Utility class for rendering UI elements.
 * Provides methods for drawing shapes, managing scissor state, and color utilities.
 */
public class RenderUtils {
    private static final Stack<ScissorRegion> scissorStack = new Stack<>();

    /**
     * Represents a scissor region for clipping.
     */
    private static class ScissorRegion {
        int x, y, width, height;

        ScissorRegion(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    /**
     * Draws a rectangle with the given color.
     */
    public static void drawRect(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + height, color);
    }

    /**
     * Draws a rectangle outline with the given color and thickness.
     */
    public static void drawRectOutline(DrawContext context, int x, int y, int width, int height, int color, int thickness) {
        // Top
        context.fill(x, y, x + width, y + thickness, color);
        // Bottom
        context.fill(x, y + height - thickness, x + width, y + height, color);
        // Left
        context.fill(x, y, x + thickness, y + height, color);
        // Right
        context.fill(x + width - thickness, y, x + width, y + height, color);
    }

    /**
     * Draws a rounded rectangle (approximation using multiple rectangles).
     */
    public static void drawRoundedRect(DrawContext context, int x, int y, int width, int height, int radius, int color) {
        // Main body
        drawRect(context, x + radius, y, width - 2 * radius, height, color);
        drawRect(context, x, y + radius, radius, height - 2 * radius, color);
        drawRect(context, x + width - radius, y + radius, radius, height - 2 * radius, color);

        // Corners (simple approximation)
        drawRect(context, x + radius, y, radius, radius, color);
        drawRect(context, x + width - 2 * radius, y, radius, radius, color);
        drawRect(context, x + radius, y + height - radius, radius, radius, color);
        drawRect(context, x + width - 2 * radius, y + height - radius, radius, radius, color);
    }

    /**
     * Draws a circle (simplified as a filled square in 1.21.8 due to API changes).
     * This is sufficient for UI elements like toggle handles and color picker selectors.
     */
    public static void drawCircle(DrawContext context, int centerX, int centerY, int radius, int color) {
        // Draw as a filled square centered on the point
        // In practice, this looks fine for small UI elements
        int size = radius * 2;
        drawRect(context, centerX - radius, centerY - radius, size, size, color);
    }

    /**
     * Pushes a scissor region onto the stack and enables scissor testing.
     * Coordinates are in screen space.
     */
    public static void pushScissor(int x, int y, int width, int height) {
        MinecraftClient client = MinecraftClient.getInstance();
        int windowHeight = client.getWindow().getHeight();
        double scaleFactor = client.getWindow().getScaleFactor();

        // Convert to framebuffer coordinates
        int scissorX = (int) (x * scaleFactor);
        int scissorY = (int) (windowHeight - (y + height) * scaleFactor);
        int scissorWidth = (int) (width * scaleFactor);
        int scissorHeight = (int) (height * scaleFactor);

        // Intersect with parent scissor region if one exists
        if (!scissorStack.isEmpty()) {
            ScissorRegion parent = scissorStack.peek();
            int parentX = parent.x;
            int parentY = parent.y;
            int parentWidth = parent.width;
            int parentHeight = parent.height;

            // Calculate intersection
            int x1 = Math.max(scissorX, parentX);
            int y1 = Math.max(scissorY, parentY);
            int x2 = Math.min(scissorX + scissorWidth, parentX + parentWidth);
            int y2 = Math.min(scissorY + scissorHeight, parentY + parentHeight);

            scissorX = x1;
            scissorY = y1;
            scissorWidth = Math.max(0, x2 - x1);
            scissorHeight = Math.max(0, y2 - y1);
        }

        scissorStack.push(new ScissorRegion(scissorX, scissorY, scissorWidth, scissorHeight));
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(scissorX, scissorY, scissorWidth, scissorHeight);
    }

    /**
     * Pops the top scissor region from the stack.
     * If the stack becomes empty, disables scissor testing.
     */
    public static void popScissor() {
        if (!scissorStack.isEmpty()) {
            scissorStack.pop();
        }

        if (scissorStack.isEmpty()) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        } else {
            ScissorRegion region = scissorStack.peek();
            GL11.glScissor(region.x, region.y, region.width, region.height);
        }
    }

    /**
     * Blends two colors together.
     * @param color1 First color
     * @param color2 Second color
     * @param ratio Blend ratio (0.0 = color1, 1.0 = color2)
     */
    public static int blendColors(int color1, int color2, float ratio) {
        ratio = Math.max(0, Math.min(1, ratio));

        int a1 = (color1 >> 24) & 0xFF;
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int a2 = (color2 >> 24) & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int a = (int) (a1 + (a2 - a1) * ratio);
        int r = (int) (r1 + (r2 - r1) * ratio);
        int g = (int) (g1 + (g2 - g1) * ratio);
        int b = (int) (b1 + (b2 - b1) * ratio);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /**
     * Adds alpha to a color.
     */
    public static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
    }

    /**
     * Gets the alpha component of a color.
     */
    public static int getAlpha(int color) {
        return (color >> 24) & 0xFF;
    }

    /**
     * Gets the red component of a color.
     */
    public static int getRed(int color) {
        return (color >> 16) & 0xFF;
    }

    /**
     * Gets the green component of a color.
     */
    public static int getGreen(int color) {
        return (color >> 8) & 0xFF;
    }

    /**
     * Gets the blue component of a color.
     */
    public static int getBlue(int color) {
        return color & 0xFF;
    }
}
