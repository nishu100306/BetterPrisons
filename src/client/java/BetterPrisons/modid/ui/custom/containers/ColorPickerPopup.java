package BetterPrisons.modid.ui.custom.containers;

import BetterPrisons.modid.BetterPrisonsClient;
import BetterPrisons.modid.ui.custom.core.Component;
import BetterPrisons.modid.ui.custom.core.Theme;
import BetterPrisons.modid.ui.custom.rendering.ColorUtils;
import BetterPrisons.modid.ui.custom.rendering.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

/**
 * HSV color picker popup modal.
 * Features saturation-value square, hue bar, hex input, and OK/Cancel buttons.
 */
public class ColorPickerPopup extends Component {
    private static final int POPUP_WIDTH = 280;
    private static final int POPUP_HEIGHT = 320;
    private static final int SV_SIZE = 200;
    private static final int HUE_WIDTH = 20;
    private static final int PADDING = 10;
    private static final int BUTTON_WIDTH = 60;
    private static final int BUTTON_HEIGHT = 20;
    private static final int PIXEL_SIZE = 2; // Render 2x2 blocks instead of 1x1 for performance

    private int currentColor;
    private float hue = 0;
    private float saturation = 1;
    private float value = 1;

    private boolean draggingSV = false;
    private boolean draggingHue = false;
    private String hexInput = "";
    private boolean editingHex = false;

    private Consumer<Integer> onConfirm;
    private Runnable onCancel;

    // Cache for performance - only regenerate when hue changes
    private float cachedHue = -1;
    private int[][] svCache = null;
    private int[] hueCache = null;

    public ColorPickerPopup(int initialColor, Consumer<Integer> onConfirm, Runnable onCancel) {
        this.currentColor = initialColor;
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;

        // Convert initial color to HSV
        float[] hsv = ColorUtils.rgbToHsv(initialColor);
        this.hue = hsv[0];
        this.saturation = hsv[1];
        this.value = hsv[2];

        this.hexInput = ColorUtils.toHex(initialColor, false);

        this.width = POPUP_WIDTH;
        this.height = POPUP_HEIGHT;
    }

    public void centerOn(int screenWidth, int screenHeight) {
        this.x = (screenWidth - POPUP_WIDTH) / 2;
        this.y = (screenHeight - POPUP_HEIGHT) / 2;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!visible) return;

        // Draw backdrop (semi-transparent)
        MinecraftClient client = MinecraftClient.getInstance();
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        RenderUtils.drawRect(context, 0, 0, screenWidth, screenHeight, 0x80000000);

        // Draw popup background
        RenderUtils.drawRect(context, x, y, POPUP_WIDTH, POPUP_HEIGHT, Theme.panelBackground);
        RenderUtils.drawRectOutline(context, x, y, POPUP_WIDTH, POPUP_HEIGHT, Theme.borderPrimary, 2);

        // Title
        context.drawText(client.textRenderer, "Pick Color", x + PADDING, y + PADDING, Theme.textPrimary, false);

        int contentY = y + PADDING + 20;

        // Render SV square
        renderSVSquare(context, x + PADDING, contentY);

        // Render hue bar
        int hueX = x + PADDING + SV_SIZE + 10;
        renderHueBar(context, hueX, contentY);

        // Color preview
        int previewY = contentY + SV_SIZE + 10;
        int previewHeight = 30;
        RenderUtils.drawRect(context, x + PADDING, previewY, SV_SIZE + HUE_WIDTH + 10, previewHeight, currentColor);
        RenderUtils.drawRectOutline(context, x + PADDING, previewY, SV_SIZE + HUE_WIDTH + 10, previewHeight, Theme.borderPrimary, 1);

        // Hex input
        int hexY = previewY + previewHeight + 10;
        context.drawText(client.textRenderer, "Hex:", x + PADDING, hexY + 5, Theme.textPrimary, false);

        int hexInputX = x + PADDING + 30;
        int hexInputWidth = 80;
        int bgColor = editingHex ? Theme.widgetBackgroundHover : Theme.widgetBackground;
        RenderUtils.drawRect(context, hexInputX, hexY, hexInputWidth, 20, bgColor);
        RenderUtils.drawRectOutline(context, hexInputX, hexY, hexInputWidth, 20,
            editingHex ? Theme.borderFocus : Theme.borderPrimary, 1);

        String displayHex = "#" + hexInput + (editingHex && System.currentTimeMillis() % 1000 < 500 ? "_" : "");
        context.drawText(client.textRenderer, displayHex, hexInputX + 4, hexY + 6, Theme.textPrimary, false);

        // Buttons
        int buttonY = hexY + 5;
        int cancelX = x + POPUP_WIDTH - PADDING - BUTTON_WIDTH;
        int okX = cancelX - BUTTON_WIDTH - 10;

        // OK button
        boolean okHovered = isMouseOverRect(mouseX, mouseY, okX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT);
        RenderUtils.drawRect(context, okX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT,
            okHovered ? Theme.accentPrimary : Theme.widgetBackground);
        RenderUtils.drawRectOutline(context, okX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT, Theme.borderPrimary, 1);
        int okTextWidth = client.textRenderer.getWidth("OK");
        context.drawText(client.textRenderer, "OK", okX + (BUTTON_WIDTH - okTextWidth) / 2, buttonY + 6, Theme.textPrimary, false);

        // Cancel button
        boolean cancelHovered = isMouseOverRect(mouseX, mouseY, cancelX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT);
        RenderUtils.drawRect(context, cancelX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT,
            cancelHovered ? Theme.widgetBackgroundHover : Theme.widgetBackground);
        RenderUtils.drawRectOutline(context, cancelX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT, Theme.borderPrimary, 1);
        int cancelTextWidth = client.textRenderer.getWidth("Cancel");
        context.drawText(client.textRenderer, "Cancel", cancelX + (BUTTON_WIDTH - cancelTextWidth) / 2, buttonY + 6, Theme.textPrimary, false);
    }

    private void renderSVSquare(DrawContext context, int x, int y) {
        // Generate cache if needed (hue changed or first time)
        int cacheSize = SV_SIZE / PIXEL_SIZE;
        if (svCache == null || Math.abs(cachedHue - hue) > 0.1f) {
            cachedHue = hue;
            svCache = new int[cacheSize][cacheSize];
            for (int row = 0; row < cacheSize; row++) {
                float v = 1.0f - (float) row / cacheSize;
                for (int col = 0; col < cacheSize; col++) {
                    float s = (float) col / cacheSize;
                    svCache[row][col] = ColorUtils.hsvToRgb(hue, s, v);
                }
            }
        }

        // Render cached gradient using larger blocks for performance
        for (int row = 0; row < cacheSize; row++) {
            for (int col = 0; col < cacheSize; col++) {
                RenderUtils.drawRect(context, x + col * PIXEL_SIZE, y + row * PIXEL_SIZE,
                    PIXEL_SIZE, PIXEL_SIZE, svCache[row][col]);
            }
        }

        // Draw border
        RenderUtils.drawRectOutline(context, x, y, SV_SIZE, SV_SIZE, Theme.borderPrimary, 1);

        // Draw selector circle
        int selectorX = x + (int) (saturation * SV_SIZE);
        int selectorY = y + (int) ((1.0f - value) * SV_SIZE);
        RenderUtils.drawCircle(context, selectorX, selectorY, 5, 0xFFFFFFFF);
        RenderUtils.drawCircle(context, selectorX, selectorY, 3, currentColor);
    }

    private void renderHueBar(DrawContext context, int x, int y) {
        // Generate hue bar cache once (never changes)
        int hueCacheSize = SV_SIZE / PIXEL_SIZE;
        if (hueCache == null) {
            hueCache = new int[hueCacheSize];
            for (int row = 0; row < hueCacheSize; row++) {
                float h = (float) row / hueCacheSize * 360;
                hueCache[row] = ColorUtils.hsvToRgb(h, 1, 1);
            }
        }

        // Render cached hue bar using larger blocks
        for (int row = 0; row < hueCacheSize; row++) {
            RenderUtils.drawRect(context, x, y + row * PIXEL_SIZE, HUE_WIDTH, PIXEL_SIZE, hueCache[row]);
        }

        // Draw border
        RenderUtils.drawRectOutline(context, x, y, HUE_WIDTH, SV_SIZE, Theme.borderPrimary, 1);

        // Draw selector
        int selectorY = y + (int) (hue / 360 * SV_SIZE);
        RenderUtils.drawRect(context, x - 2, selectorY - 2, HUE_WIDTH + 4, 4, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;

        int contentY = y + PADDING + 20;
        int svX = x + PADDING;
        int svY = contentY;
        int hueX = x + PADDING + SV_SIZE + 10;
        int hueY = contentY;

        // Check SV square
        if (isMouseOverRect(mouseX, mouseY, svX, svY, SV_SIZE, SV_SIZE)) {
            draggingSV = true;
            updateSVFromMouse(mouseX, mouseY, svX, svY);
            return true;
        }

        // Check hue bar
        if (isMouseOverRect(mouseX, mouseY, hueX, hueY, HUE_WIDTH, SV_SIZE)) {
            draggingHue = true;
            updateHueFromMouse(mouseY, hueY);
            return true;
        }

        // Check hex input
        int hexY = contentY + SV_SIZE + 10 + 30 + 10;
        int hexInputX = x + PADDING + 30;
        if (isMouseOverRect(mouseX, mouseY, hexInputX, hexY, 80, 20)) {
            editingHex = true;
            return true;
        } else {
            editingHex = false;
        }

        // Check buttons (use same calculation as render method)
        int buttonY = hexY + 30;
        int cancelX = x + POPUP_WIDTH - PADDING - BUTTON_WIDTH;
        int okX = cancelX - BUTTON_WIDTH - 10;

        // Check OK button
        if (isMouseOverRect(mouseX, mouseY, okX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT)) {
            if (onConfirm != null) {
                onConfirm.accept(currentColor);
            }
            return true;
        }

        // Check Cancel button
        if (isMouseOverRect(mouseX, mouseY, cancelX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT)) {
            if (onCancel != null) {
                onCancel.run();
            }
            return true;
        }

        // Click outside popup closes it
        if (!isMouseOverRect(mouseX, mouseY, x, y, POPUP_WIDTH, POPUP_HEIGHT)) {
            if (onCancel != null) {
                onCancel.run();
            }
            return true;
        }

        return true; // Consume all clicks when popup is open
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingSV = false;
        draggingHue = false;
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        int contentY = y + PADDING + 20;

        if (draggingSV) {
            int svX = x + PADDING;
            int svY = contentY;
            updateSVFromMouse(mouseX, mouseY, svX, svY);
            return true;
        }

        if (draggingHue) {
            int hueY = contentY;
            updateHueFromMouse(mouseY, hueY);
            return true;
        }

        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (editingHex && hexInput.length() < 6) {
            if (Character.isDigit(chr) || (chr >= 'a' && chr <= 'f') || (chr >= 'A' && chr <= 'F')) {
                hexInput += Character.toUpperCase(chr);
                updateColorFromHex();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (editingHex) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !hexInput.isEmpty()) {
                hexInput = hexInput.substring(0, hexInput.length() - 1);
                updateColorFromHex();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER) {
                editingHex = false;
                updateColorFromHex();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                editingHex = false;
                return true;
            }
        }
        return false;
    }

    private void updateSVFromMouse(double mouseX, double mouseY, int svX, int svY) {
        saturation = Math.max(0, Math.min(1, (float) (mouseX - svX) / SV_SIZE));
        value = Math.max(0, Math.min(1, 1.0f - (float) (mouseY - svY) / SV_SIZE));
        updateCurrentColor();
    }

    private void updateHueFromMouse(double mouseY, int hueY) {
        hue = Math.max(0, Math.min(360, (float) (mouseY - hueY) / SV_SIZE * 360));
        updateCurrentColor();
    }

    private void updateCurrentColor() {
        currentColor = ColorUtils.hsvToRgb(hue, saturation, value);
        hexInput = ColorUtils.toHex(currentColor, false);
    }

    private void updateColorFromHex() {
        if (hexInput.length() == 6) {
            try {
                currentColor = ColorUtils.parseHex(hexInput);
                float[] hsv = ColorUtils.rgbToHsv(currentColor);
                hue = hsv[0];
                saturation = hsv[1];
                value = hsv[2];
            } catch (Exception e) {
                // Invalid hex, ignore
            }
        }
    }

    private boolean isMouseOverRect(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
